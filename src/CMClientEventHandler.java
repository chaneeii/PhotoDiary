import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

public class CMClientEventHandler implements CMAppEventHandler {
   
   
   private JTextPane m_outTextPane;
   private long m_lStartTime;
   private CMClient m_client;
   private CMClientStub m_clientStub;
   private FileOutputStream m_fos;
   private PrintWriter m_pw;      

   int m_nSimNum ;
   public CMClientEventHandler(CMClientStub clientStub, CMClient client)
   {
      m_client = client;
      m_clientStub = clientStub;
      m_nSimNum = 0;
      m_fos = null;
      m_pw = null;
      
   }
   public void setStartTime(long time)
   {
      m_lStartTime = time;
   }
   
   public long getStartTime()
   {
      return m_lStartTime;
   }
   
    public void printMessage(String strText)
      {
       m_client.printMessage(strText);
         
         return;
      }
    private void printImage(String strPath)
      {
         m_client.printImage(strPath);
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
      case CMInfo.CM_FILE_EVENT:
			processFileEvent(cme);
			break;
      default:
         return;
      }
   }
   
   private void processDummyEvent(CMEvent cme)
   {
	   CMDummyEvent due = (CMDummyEvent) cme;
	   String dummy = due.getDummyInfo();
	 
	   String flag = dummy.substring(0,1);
	   String a ="-";
	   String b =">";
	   dummy = dummy.substring(1,dummy.length());
	   if(flag.equals(a))
	   {
			printImage("C:" + File.separator + "Users" + File.separator + "samsung" + File.separator
					+ "eclipse-workspace" + File.separator + "SimpleCMClient" + File.separator + "client-file-path"
					+ File.separator + dummy.trim() + ".jpg");
       
	   }
	   else if(flag.equals(b))
	   {
		   printMessage(due.getDummyInfo());
	   }
	   else {
		   printMessage("\n==========Diary List=========== "+due.getDummyInfo());

	   }
   }
   
   private void processFileEvent(CMEvent cme)
	{
		CMFileEvent fe = (CMFileEvent) cme;
		int nOption = -1;
		switch(fe.getID())
		{
		
		case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
			StringBuffer strReqBuf = new StringBuffer(); 
			strReqBuf.append("["+fe.getFileSender()+"] wants to send a file.\n");
			strReqBuf.append("file path: "+fe.getFilePath()+"\n");
			strReqBuf.append("file size: "+fe.getFileSize()+"\n");
			System.out.print(strReqBuf.toString());
			nOption = JOptionPane.showConfirmDialog(null, strReqBuf.toString(), 
					"Push File", JOptionPane.YES_NO_OPTION);
			if(nOption == JOptionPane.YES_OPTION)
			{
				m_clientStub.replyEvent(fe, 1);
			}
			else
			{
				m_clientStub.replyEvent(fe, 1);
			}				
			break;
		}
	}

   private void processSessionEvent(CMEvent cme)
   {
      CMSessionEvent se = (CMSessionEvent)cme;
      switch(se.getID())
      {
      case CMSessionEvent.LOGIN_ACK:
         if(se.isValidUser() == 0)
         {
            printMessage("This client fails authentication by the default server!\n");
            
         }
         else if(se.isValidUser() == -1)
         {
            printMessage("This client is already in the login-user list!\n");
         }
         else
         {
            printMessage("This client successfully logs in to the default server.\n");
            CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
            
            m_client.setTitle("Photo Diary ["+interInfo.getMyself().getName()+"]");

         }
         break;
      }
   }
   
   // 서버로부터 현재 sns content 가져오는 부분 
   private void processSNSEvent(CMEvent cme)
   {
      CMSNSInfo snsInfo = m_clientStub.getCMInfo().getSNSInfo();
      CMSNSContentList contentList = snsInfo.getSNSContentList();
      CMSNSEvent se = (CMSNSEvent) cme;
      int i = 0;
      
      switch(se.getID())
      {
      case CMSNSEvent.CONTENT_UPLOAD_RESPONSE:  // 일기를 써서 서버로 넘기면 해당 결과를 서버로부터받아서 클라이언트에게 전달
         if( se.getReturnCode() == 1 )
         {
            printMessage("Content upload succeeded.\n");
         }
         else
         {
            printMessage("Content upload failed.\n");
         }
         
         printMessage("user("+se.getUserName()+"), seqNum("+se.getContentID()+"), time("
               +se.getDate()+").\n");
         break;

         default:
            return;
      }
   }


}