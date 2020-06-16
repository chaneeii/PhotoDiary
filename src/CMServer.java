import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServer extends JFrame  {

   
   private static final long serialVersionUID = 1L;
   
   
   private CMServerStub m_serverStub;
   private CMServerEventHandler m_eventHandler;
   private JTextPane m_outTextPane;
   private JTextField m_inTextField;
   

   CMServer()  //객체초기화하면서 서버 실행
   {
      //서버의 GUI부분
      setTitle("PhotoDiaryServer");
      setSize(500, 500);
      
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      m_serverStub = new CMServerStub();   
      m_eventHandler = new CMServerEventHandler(m_serverStub ,this);  

      m_outTextPane = new JTextPane();
      m_outTextPane.setEditable(false);

      StyledDocument doc = m_outTextPane.getStyledDocument();
      addStylesToDocument(doc);
      
      add(m_outTextPane, BorderLayout.CENTER);
      JScrollPane scroll = new JScrollPane (m_outTextPane, 
               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      
      add(scroll);
      
      m_inTextField = new JTextField();
      add(m_inTextField, BorderLayout.SOUTH);
      
      m_serverStub = new CMServerStub();
      m_eventHandler = new CMServerEventHandler(m_serverStub,this);
   
      setVisible(true);
      startCM();
       
   }   
   
   public CMServerStub getServerStub()  
   {   
      return m_serverStub;  
   }    
   
   public CMServerEventHandler getServerEventHandler()  
   {   
      return m_eventHandler;  
   } 
   
   public void startCM()
   {
      boolean bRet = false;
      
      // conf파일로부터 현재 서버정보를 가지고 온다. 
      String strSavedServerAddress = null;
      String strCurServerAddress = null;
      int nSavedServerPort = -1;
      
      strSavedServerAddress = m_serverStub.getServerAddress();
      strCurServerAddress = CMCommManager.getLocalIP();     
      nSavedServerPort = m_serverStub.getServerPort();
      
      // 디폴트서버말고 혹시바꿀거면 서버주소랑 포트번호 입력하게 하는 부분 
      JTextField serverAddressTextField = new JTextField(strCurServerAddress);
      JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
      Object msg[] = {
            "Server Address: ", serverAddressTextField,
            "Server Port: ", serverPortTextField
      };
      int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

      // 사용자가 원하는 경우 서버의 주소 변경을 입력받음
      if (option == JOptionPane.OK_OPTION) 
      {
         String strNewServerAddress = serverAddressTextField.getText();
         int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
         if(!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
            m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
      }
      
      // start cm
      bRet = m_serverStub.startCM();
      if(!bRet)
      {
         printStyledMessage("CM initialization error!\n", "bold");
      }
      else
      {
         printStyledMessage("Server CM starts.\n", "bold");
         printMessage("일기 정보가 저장되어 있습니다.\n");                           
      }

      m_inTextField.requestFocus();

   }
   
   
   public void printMessage(String strText)
   {
      StyledDocument doc = m_outTextPane.getStyledDocument();
      try {
         doc.insertString(doc.getLength(), strText, null);
         m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

      } catch (BadLocationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return;
   }
   
   public void printStyledMessage(String strText, String strStyleName)
   {
      StyledDocument doc = m_outTextPane.getStyledDocument();
      try {
         doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
         m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

      } catch (BadLocationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return;
   }
      
   public void printImage(String strPath)
   {
      int nTextPaneWidth = m_outTextPane.getWidth();
      int nImageWidth;
      int nImageHeight;
      int nNewWidth;
      int nNewHeight;
      ImageIcon icon = new ImageIcon(strPath);
      Image image = icon.getImage();
      nImageWidth = image.getWidth(m_outTextPane);
      nImageHeight = image.getHeight(m_outTextPane);
      
      if(nImageWidth > nTextPaneWidth/2)
      {
         nNewWidth = nTextPaneWidth / 2;
         float fRate = (float)nNewWidth/(float)nImageWidth;
         nNewHeight = (int)(nImageHeight * fRate);
         Image newImage = image.getScaledInstance(nNewWidth, nNewHeight, java.awt.Image.SCALE_SMOOTH);
         icon = new ImageIcon(newImage);
    
      m_outTextPane.insertIcon ( icon );
      printMessage("\n");
      }
   }
   
   private void addStylesToDocument(StyledDocument doc)
   {
      Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

      Style regularStyle = doc.addStyle("regular", defStyle);
      StyleConstants.setFontFamily(regularStyle, "SansSerif");
      
      Style boldStyle = doc.addStyle("bold", defStyle);
      StyleConstants.setBold(boldStyle, true);
   }
   
   
   
    
    public static void main(String[] args) 
    {   
       CMServer server = new CMServer();   
       CMServerStub cmStub = server.getServerStub();   
       cmStub.setAppEventHandler(server.getServerEventHandler());    
   } 
}