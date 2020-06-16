import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerEventHandler implements CMAppEventHandler {

   private CMServerStub m_serverStub; 
   private CMServer m_server; 
   
   public CMServerEventHandler(CMServerStub serverStub, CMServer server)  
   {   
      m_serverStub = serverStub;  
      m_server = server;
   }
   
   @Override
   public void processEvent(CMEvent cme) {
      switch(cme.getType())   
      {   
      case CMInfo.CM_SESSION_EVENT:    
         processSessionEvent(cme);    
         break;   
    case CMInfo.CM_SNS_EVENT:
         processSNSEvent(cme);
         break;
      case CMInfo.CM_DUMMY_EVENT:
    	  processDummyEvent(cme);
    	  break; 
      default:    
         return;   
      } 
   }

   //로그인, 로그아웃같이 세션에서 일어나는 함수를 처리
   private void processSessionEvent(CMEvent cme)  
   {   
      
      CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
      
      CMSessionEvent se = (CMSessionEvent) cme;   
      switch(se.getID())   
      {   case CMSessionEvent.LOGIN:
         System.out.println("["+se.getUserName()+"] requests login.");
         printMessage("["+se.getUserName()+"] requests login.\n");
         if(confInfo.isLoginScheme())
         {
            boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), 
                  m_serverStub.getCMInfo());
            if(!ret)
            {
               printMessage("["+se.getUserName()+"] authentication fails!\n");
               m_serverStub.replyEvent(cme, 0);
            }
            else
            {
               printMessage("["+se.getUserName()+"] authentication succeeded.\n");
               m_serverStub.replyEvent(cme, 1);
               
               CMUserEvent ue = new CMUserEvent();
               int num = 1;
               String title = "test string";
               System.out.println("====================== test CMUserEvent");
               ue.setStringID("testID");
               ue.setEventField(CMInfo.CM_INT, "intField", String.valueOf(num));
               ue.setEventField(CMInfo.CM_STR, "strField", title);
               m_serverStub.send(ue,se.getUserName());
            }
         }
         break;
      case CMSessionEvent.LOGOUT:
         printMessage("["+se.getUserName()+"] logs out.\n");
         break;
     
      default:
         break;
      }  
   }
   
   private void processDummyEvent(CMEvent cme)
   {
	   /*클라이언트에게 테이블 정보를 요청받음*/
	   CMDummyEvent due = (CMDummyEvent) cme;
	   String dummy = due.getDummyInfo();
	  //printMessage(dummy);
	   String flag = dummy.substring(0,1);

	   dummy = dummy.substring(1,dummy.length());

	   /*디비로 부터 테이블정보 가져옴*/
       String a ="-";
       String b =">";
       String url = "jdbc:mysql://localhost/cmdb";
	   String id = "ccslab";
	   String pw = "ccslab";
       
	   Connection con = null;
	   java.sql.PreparedStatement sta = null;
	   if(!flag.equals(a)&&(!flag.equals(b)))/*클라이언트에게 테이블 정보를 요청받음*/
	   {
		  printMessage("\n클라이언트에게 일기 목록을 보여줍니다\n");
		   
	   
	   CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
       CMUser myself = interInfo.getMyself();
       
		try {
			
			con = DriverManager.getConnection(url,id,pw);
			
			java.sql.Statement st = null;
			ResultSet rs = null;
			String sql;
			st = con.createStatement();
			rs = st.executeQuery("SHOW DATABASES");
	
			if (st.execute("SHOW DATABASES")) {
				rs = st.getResultSet();
			}
			
			sql = "SELECT * FROM diary";
			
			rs = st.executeQuery(sql);
			
			String buf="\nNo."+"      " + "Title\n";
			
			while (rs.next()) {
				int num = rs.getInt(1);
				String str = rs.getString(2);
				
				buf = buf+ Integer.toString(num) +"         " + str+"\n";
			}					
			
			CMDummyEvent replyDue = new CMDummyEvent();
			replyDue.setDummyInfo(buf);
			       
			boolean ret = m_serverStub.send(replyDue,due.getDummyInfo().trim());
			
			replyDue = null;

		} catch (SQLException sqex) {
			printMessage("SQLException: " + sqex.getMessage());
			printMessage("SQLState: " + sqex.getSQLState());
		}
	   }
	   else if(flag.equals(b))/*클라이언트가 일기내용을 요청했을 경우 파일전송과 일기전송*/
	   {
		   String number = dummy.substring(0,1);
		   dummy = dummy.substring(1,dummy.length());
		   int num = Integer.parseInt(number);
	        String title = null;

				try {
					con = DriverManager.getConnection(url, id, pw);

					java.sql.Statement st = null;
					ResultSet rs = null;
					String sql;
					st = con.createStatement();
					rs = st.executeQuery("SHOW DATABASES");

					if (st.execute("SHOW DATABASES")) {
						rs = st.getResultSet();
					}

					sql = "SELECT * FROM diary";

					rs = st.executeQuery(sql);
					String msg = null;
					int index = 0;
					while (rs.next()) {
						index = rs.getInt(1);
						if (index == num) {
							title = rs.getString(2);
							msg = rs.getString(3);
							break;
							
						}
					}
					printMessage("\n클라이언트에게 일기를 보냅니다\n");
					
					String buf = ">No." + index + "  [" + title + "] 일기를 선택하였습니다" + "\n\n 일기내용 : " + msg + "\n\n";
				    CMDummyEvent replyDue = new CMDummyEvent();
					replyDue.setDummyInfo(buf);					       
					boolean ret = m_serverStub.send(replyDue,dummy);
					
					replyDue = null;
				} catch (SQLException sqex) {
					System.out.println("SQLException: " + sqex.getMessage());
					System.out.println("SQLState: " + sqex.getSQLState());
				}
				String strFilePath = null;
				String strReceiver = dummy;
				
				printMessage("====== push a file======\n");
				
				strFilePath = "C:" + File.separator + "Users" + File.separator + "samsung" + File.separator
						+ "eclipse-workspace" + File.separator + "SimpleCMClient" + File.separator + "server-file-path"
						+ File.separator + "ccslab" + File.separator + title + ".jpg";
				
				m_serverStub.pushFile(strFilePath, strReceiver);
				CMDummyEvent replyDue = new CMDummyEvent();
				String strInput = "-"+ title;
				replyDue.setDummyInfo(strInput);

				boolean ret = m_serverStub.send(replyDue,dummy);
				
				replyDue = null;
				
				
	   }
	   else/*클라이언트가 다이어리를 업로드한 경우*/
	   {
		   int idx = dummy.indexOf(":");
		   String title = dummy.substring(0,idx);
		   String msg = dummy.substring(idx+1);
		   try {
   			printMessage("\n클라이언트가 일기를 업로드 했습니다\n");
   			con = DriverManager.getConnection(url,id,pw);
   			
   			java.sql.Statement st = null;
   			ResultSet rs = null;
   			String sql;
   			st = con.createStatement();
   			rs = st.executeQuery("SHOW DATABASES");
   	
   			if (st.execute("SHOW DATABASES")) {
   				rs = st.getResultSet();
   			}
   			
   			sql = "SELECT * FROM diary";
   			int num =0;
   			rs = st.executeQuery(sql);
   			while (rs.next()) {
   				num = rs.getInt(1);
   			}
   			int count = num+1;
   			sql = "INSERT INTO diary VALUES('"+ count + "','" + title +"','"+msg+"')";
   			sta = con.prepareStatement(sql);
   			int return_value = sta.executeUpdate(sql);
   			
   		} catch (SQLException sqex) {
   			printMessage("SQLException: " + sqex.getMessage());
   			printMessage("SQLState: " + sqex.getSQLState());
   		}
		
	   }
	   return;
   }

   private void processSNSEvent(CMEvent cme)
   {
      CMSNSEvent se = (CMSNSEvent) cme;
      switch(se.getID())
      {
      case CMSNSEvent.CONTENT_UPLOAD_REQUEST:
         System.out.println("content upload requested by ("+se.getUserName()+"), attached file path: "
                +se.getFileName()+", message: "+se.getMessage());
         printMessage("content upload requested by ("+se.getUserName()+"), message("+se.getMessage()
               +"), \n#attachement("+se.getNumAttachedFiles()+"), replyID("+se.getReplyOf()
               +"), lod("+se.getLevelOfDisclosure()+")\n");
         break;
      case CMSNSEvent.REQUEST_ATTACHED_FILE:
         printMessage("["+se.getUserName()+"] requests an attached file ["
               +se.getFileName()+"] of SNS content ID["+se.getContentID()+"] written by ["
               +se.getWriterName()+"].\n");
         break;
      }
      return;
   }
 
   
   private void printMessage(String strText)
   {
      m_server.printMessage(strText);
   }
}