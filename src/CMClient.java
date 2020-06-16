import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.entity.CMObject;

public class CMClient extends JFrame {

	private JTextPane m_outTextPane;
	private JTextField m_inTextField;

	private JButton loginButton;
	private JButton readButton;
	private JButton writeButton;
	private JButton updateDiaryButton;
	private JButton showUsersButton;

	CMClientStub m_clientStub; 
	CMClientEventHandler m_eventHandler; 

	public CMClient() {

		MyActionListener cmActionListener = new MyActionListener();
		setTitle("PhotoDiary"); 
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		m_outTextPane = new JTextPane();
		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);
		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane centerScroll = new JScrollPane(m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(centerScroll, BorderLayout.CENTER);

		m_inTextField = new JTextField();
		add(m_inTextField, BorderLayout.SOUTH);

		// ��ܿ� ��ġ�� ��ư GUI
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setBackground(new Color(220, 220, 220));
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);

		loginButton = new JButton("login");
		loginButton.addActionListener(cmActionListener);
		topButtonPanel.add(loginButton);

		readButton = new JButton("read");
		readButton.addActionListener(cmActionListener);
		topButtonPanel.add(readButton);

		writeButton = new JButton("write");
		writeButton.addActionListener(cmActionListener);
		topButtonPanel.add(writeButton);

		updateDiaryButton = new JButton("updateDiary");
		updateDiaryButton.addActionListener(cmActionListener);
		topButtonPanel.add(updateDiaryButton);

		showUsersButton = new JButton("showCurrentUsers");
		showUsersButton.addActionListener(cmActionListener);
		topButtonPanel.add(showUsersButton);

		setVisible(true);
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMClientEventHandler(m_clientStub, this);
	}

	private void addStylesToDocument(StyledDocument doc) {
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");

		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);

		Style linkStyle = doc.addStyle("link", defStyle);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);
	}

	public void printMessage(String strText) {
	
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

	public void printStyledMessage(String strText, String strStyleName) {
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

	public void printImage(String strPath) {
		int nTextPaneWidth = m_outTextPane.getWidth();
		int nImageWidth;
		int nImageHeight;
		int nNewWidth;
		int nNewHeight;

		File f = new File(strPath);
		if (!f.exists()) {
			printMessage(strPath + "\n");
			return;
		}

		ImageIcon icon = new ImageIcon(strPath);
		Image image = icon.getImage();
		nImageWidth = image.getWidth(m_outTextPane);
		nImageHeight = image.getHeight(m_outTextPane);

		if (nImageWidth > nTextPaneWidth / 2) {
			nNewWidth = nTextPaneWidth / 2;
			float fRate = (float) nNewWidth / (float) nImageWidth;
			nNewHeight = (int) (nImageHeight * fRate);
			Image newImage = image.getScaledInstance(nNewWidth, nNewHeight, java.awt.Image.SCALE_SMOOTH);
			icon = new ImageIcon(newImage);
		}

		m_outTextPane.insertIcon(icon);
		printMessage("\n");
	}

	public void testLoginDS() {
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;

		printMessage("====== login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = { "User Name:", userNameField, "Password:", passwordField };
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword());

			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();

			if (bRequestResult) {
				printMessage("successfully sent the login request.\n");

			} else {
				printStyledMessage("failed the login request!\n", "bold");
			}
		}

		printMessage("============\n");
	}

	public void readDiary() {// ����ڰ� �Է��� ��ȣ�� �ش��ϴ� �ϱ⸦�����ͼ� ������ �Բ� �����ش�.
		String seqNum = null;
		boolean bRequestResult = false;
		String filePath = null;
		JTextField numField = new JTextField();
		Object[] message = { "Diary number:", numField };
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		int option = JOptionPane.showConfirmDialog(null, message, "read Diary", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {

			//�������� ���� ��û
			boolean bReturn = false;
			String strFileName = null;
			String strFileOwner =interInfo.getDefaultServerInfo().getServerName();
			
			printMessage("========= Diary ==========\n");
			
			strFileName = numField.getText().trim(); //���ϴ� ���̾�� number�� �Է�

			if(strFileName.isEmpty())
			{
				printMessage("File name is empty!\n");
				return;
			}
			
			//�ش� ���̾ ���������� Ŭ���̾�Ʈ ������ �̸������� �������� ����
			CMUser myself = interInfo.getMyself();
			CMDummyEvent due = new CMDummyEvent();
			String strInput = ">"+numField.getText()+myself.getName().trim();
			
			due.setDummyInfo(strInput);
			m_clientStub.send(due, "SERVER");
			
		}
			
	}

	/// ���ο� �ϱ� �ۼ��ϴ� �Լ�
	public void UploadDiary() {
		String strTitle = null;
		String strMessage = null;
		ArrayList<String> filePathList = null;
		int nNumAttachedFiles = 0;
		int nReplyOf = 0;
		int nLevelOfDisclosure = 0;
		File[] files = null;

		printMessage("====== write your PhotoDiary!=== \n");

		JTextField titleField = new JTextField(); // �ϱ�����
		JTextField msgField = new JTextField(); // ���������� ���� ���� �ؽ�Ʈ
		JButton photo = new JButton();
		JCheckBox attachedFilesBox = new JCheckBox(); 

		Object[] message = { "Write your Photo Name", titleField, "Write your diary", msgField,
				"File Attachment: ", attachedFilesBox, };

		int option = JOptionPane.showConfirmDialog(null, message, "Upload Diary", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strTitle = titleField.getText();
			strMessage = msgField.getText();
			if (attachedFilesBox.isSelected()) 
			{
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
				File curDir = new File(confInfo.getTransferedFileHome().toString());
				fc.setCurrentDirectory(curDir);
				int fcRet = fc.showOpenDialog(this);
				if (fcRet == JFileChooser.APPROVE_OPTION) {
					files = fc.getSelectedFiles();
					if (files.length > 0) {
						nNumAttachedFiles = files.length;
						filePathList = new ArrayList<String>();
						for (int i = 0; i < nNumAttachedFiles; i++) {
							String strPath = files[i].getPath();
							filePathList.add(strPath);
						}
					}
				}
			}
			String strUser = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
			m_clientStub.requestSNSContentUpload(strUser, strMessage, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure,
					filePathList);

			// DB�� Į���߰�
			CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
			CMUser myself = interInfo.getMyself();

			String strInput = myself.getName().trim();

			CMDummyEvent due = new CMDummyEvent();
			strInput = "-" + strTitle + ":" + strMessage;
			due.setDummyInfo(strInput);
			m_clientStub.send(due, "SERVER");
			printMessage("\n ["+ strTitle +"] : "+ strMessage);
			printMessage("\n================================================\n");
			due = null;

		}

		return;
	}

	public void all_DownloadNewSNSContent() {

		int nContentOffset = 0;
		String strWriterName = null;
		String strUserName = m_clientStub.getMyself().getName();

		m_clientStub.requestSNSContent(strWriterName, nContentOffset);
		if (CMInfo._CM_DEBUG) {
			printMessage("[" + strUserName + "] requests content of writer[" + strWriterName + "] with offset("
					+ nContentOffset + ").\n");
		}
		
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();

		String strInput = myself.getName().trim();
		// �������� ���̾ ��� ��û 
		CMDummyEvent due = new CMDummyEvent();
		due.setDummyInfo(strInput);
		m_clientStub.send(due, "SERVER");
		due = null;

		
		return;
	}

	public void showUsers() {
		printMessage("<���� ������ ����Ʈ>\n");
		CMMember groupMembers = m_clientStub.getGroupMembers();
		CMUser myself = m_clientStub.getMyself();
		printMessage("���� ����ڴ��� �̸� : " + myself.getName() + "\n");
		if (groupMembers == null || groupMembers.isEmpty()) {
			printMessage(myself.getName() + "�� �ܿ� �ٸ� �����ڰ� ���� �����ϴ�.\n");
			return;
		} else {
			printMessage(groupMembers.toString() + "���� ���ӵǾ� �ֽ��ϴ�. \n");

		}

	}

	// ��ư Ŭ�������� �̺�Ʈ ó���ϴ� �Լ�
	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.getText().equals("login")) {
				testLoginDS(); // �α���

			} else if (button.getText().equals("updateDiary")) {
				// ������� ����� ���̾ ���� ���
				all_DownloadNewSNSContent();

			} else if (button.getText().equals("write")) {
				// ����ڰ� ���ο� �ϱ⸦ �ۼ�
				UploadDiary();
			} else if (button.getText().equals("read")) {
				// ����ڰ� �ϱ��ȣ�� �Է��ϸ� �ش��ϴ� ��ȣ�� �ϱ⸦ �����ͼ� ���̾ ������ ������ �Բ� ���
				readDiary();
			} else if (button.getText().equals("showCurrentUsers")) {
				//���� �������� Ŭ���̾�Ʈ ����� ���
				showUsers();
			}
		}
	}

	public static void main(String[] args) {
		CMClient client = new CMClient();
		client.m_clientStub.setAppEventHandler(client.m_eventHandler);
		client.m_clientStub.startCM();
	}
}