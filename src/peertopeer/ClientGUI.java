/**
 * Andreas Fischer
 * P2PTests
 * 14.05.2016
 * client.java
 */
package peertopeer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.print.attribute.standard.Severity;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.SliderUI;

/**
 * @author Andreas
 *
 */
public class ClientGUI extends JFrame implements ActionListener{
	JTextField textfield_serverip;
	JTextField textfield_serverport;
	JTextField textfield_filepath;
	ServerSocket myServerListen;
	Client client;
	
	JList jlist_files;
	JList jlist_localfiles;
	
	public ClientGUI(){
		super("Client");
		setLayout(new BorderLayout());		
		
		JPanel panel_north = new JPanel();
		panel_north.setLayout(new BorderLayout());
		
		JPanel panel_connect = new JPanel();	
		// Server IP input
		JLabel label_serverip = new JLabel("Server IP:");
		panel_connect.add(label_serverip);
		textfield_serverip = new JTextField();
		textfield_serverip.setColumns(20);
		textfield_serverip.setText("192.168.108.10");
		panel_connect.add(textfield_serverip);
		// Server port input
		JLabel label_serverport = new JLabel("Server Port:");
		panel_connect.add(label_serverport);
		textfield_serverport = new JTextField();
		textfield_serverport.setColumns(20);
		textfield_serverport.setText("45000");
		panel_connect.add(textfield_serverport);
		
		
		// Server connect button
		JButton button_serverconnect = new JButton("Connect");
		button_serverconnect.addActionListener(this);
		panel_connect.add(button_serverconnect);
		// Server disconnect button
		JButton button_serverdisconnect = new JButton("Disconnect");
		button_serverdisconnect.addActionListener(this);
		panel_connect.add(button_serverdisconnect);
		
		panel_north.add(panel_connect, BorderLayout.NORTH);
		
		JPanel panel_folderpath = new JPanel();
		JLabel label_filepath = new JLabel("Folder: ");
		panel_folderpath.add(label_filepath);
		textfield_filepath = new JTextField();
		textfield_filepath.setText("C:/temp");
		textfield_filepath.setColumns(20);
		panel_folderpath.add(textfield_filepath);
		JButton button_choosepath = new JButton("Choose");
		MyFileChooser fc = new MyFileChooser(textfield_filepath);
		panel_folderpath.add(fc);
		panel_folderpath.setBorder(BorderFactory.createEmptyBorder(0,50,0,0));
		panel_north.add(panel_folderpath, BorderLayout.WEST);
		
		panel_north.setPreferredSize(new Dimension(200,100));
		
		add(panel_north, BorderLayout.NORTH);
		
		JPanel panel_files = new JPanel();
	
		JLabel label_serverlisttitle = new JLabel("Server File list");
		panel_files.setLayout(new BorderLayout());
		panel_files.add(label_serverlisttitle, BorderLayout.NORTH);
		
		jlist_files = new JList<Object>();
		panel_files.add(new JScrollPane(jlist_files), BorderLayout.CENTER);
		
		add(panel_files, BorderLayout.WEST);
		
		JPanel panel_action = new JPanel();
		JLabel label_actions = new JLabel("Actions");
		panel_action.setLayout(new BorderLayout());
		panel_action.add(label_actions, BorderLayout.NORTH);
		JButton button_download = new JButton("Download");
		button_download.addActionListener(this);
		panel_action.add(button_download, BorderLayout.CENTER);
		
		JProgressBar progress = new JProgressBar(0, 100);
		int num = 0;
		progress.setValue(0);
		progress.setStringPainted(true);
		panel_action.add(progress, BorderLayout.SOUTH);
		
		add(panel_action, BorderLayout.CENTER);
		
		JPanel panel_localfiles = new JPanel();
		JLabel label_localfilestitle = new JLabel("Local Files");
		panel_localfiles.setLayout(new BorderLayout());
		panel_localfiles.add(label_localfilestitle, BorderLayout.NORTH);
		jlist_localfiles = new JList();
		panel_localfiles.add(jlist_localfiles, BorderLayout.CENTER);
		panel_localfiles.setPreferredSize(new Dimension(300,300));
		add(panel_localfiles, BorderLayout.EAST);
		
	}
	public static void main(String[] args) {
		ClientGUI client = new ClientGUI();
		
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setSize(900, 500);
		client.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		
		JButton action = (JButton)e.getSource();
		String name = action.getActionCommand();
		if (name == "Connect"){
			   System.out.println("Connecting to "+textfield_serverip.getText()+" on port "+textfield_serverport.getText());
			   new Thread(startClient(textfield_serverip.getText(),textfield_serverport.getText(), textfield_filepath.getText())).start();	  
			   new Thread(startSender());
		   } else if (name == "Disconnect"){
		       System.out.println("Disconnecting");
		       client.closeConnection();
		   } else if(name == "Download"){
			   System.out.println("Going to download: "+jlist_files.getSelectedValue().toString() );
			   client.askForFile(jlist_files.getSelectedValue().toString());
			   
		   }
	}
	private Runnable startClient(String serverip, String serverport, String filepath){
		client = new Client(serverip,serverport, filepath);
		jlist_localfiles.setModel(client.getLocalFilenamesAsModel());
		jlist_files.setModel(client.getServerFilenamesAsModel());
		return null;
	}
	public Runnable startSender() {
        (new Thread() {
        	public void run() {
        		System.out.println("Starting server listen");
        		try {
        			myServerListen = new ServerSocket(45001,10,getLocalInetAdress());
        			while(true)
        			{
        				Socket clientSocket = myServerListen.accept();
        				System.out.println("connection request received"+clientSocket.getRemoteSocketAddress().toString());
        				new Thread(new ClientServer(clientSocket, client.getSubclientMe())).start();
        				
        			}
        			
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        	
        }).start();
		return null;
	}
	private Runnable startServer(){
		
		System.out.println("Starting server listen");
		try {
			ServerSocket MySkServer = new ServerSocket(45001,10,getLocalInetAdress());
			while(true)
			{
				Socket clientSocket = MySkServer.accept();
				System.out.println("connection request received"+clientSocket.getRemoteSocketAddress().toString());
				new Thread(new ClientServer(clientSocket, client.getSubclientMe())).start();
				
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null ;
		}
		
	}
	public InetAddress getLocalInetAdress(){
		InetAddress LocalAddress=null;
		try {
			LocalAddress = InetAddress.getLocalHost();
			InetAddress test = InetAddress.getByName("127.0.1.1");
			if(LocalAddress.equals(test)){
				System.out.println("no");
				String interfaceName = "eth1";
				NetworkInterface ni = NetworkInterface.getByName(interfaceName);
				Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
				while(inetAddresses.hasMoreElements()) {
					InetAddress ia = inetAddresses.nextElement();

					if(!ia.isLinkLocalAddress()) {
						if(!ia.isLoopbackAddress()) {
							System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
							LocalAddress = ia;
						}
					}   
				}
			}
			System.out.println("My local address is the following one : " +LocalAddress.getHostAddress().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// InetAddress inetAddress = InetAddress.getByName("192.168.0.105");
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return LocalAddress;
	}
}
class MyFileChooser extends JPanel implements ActionListener{

	JButton button_choos;
	JFileChooser filechooser;
	String foldername;
	JTextField textfield_filepath;
	public MyFileChooser(JTextField textfield_filepath) {
		this.textfield_filepath  = textfield_filepath;
		button_choos = new JButton("Choose");
		button_choos.addActionListener(this);
		add(button_choos);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		filechooser = new JFileChooser();
		filechooser.setCurrentDirectory(new java.io.File("C:/Temp"));
		filechooser.setDialogTitle("Path to share folder");
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		filechooser.setAcceptAllFileFilterUsed(false);
		
		if (filechooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			 System.out.println("getCurrentDirectory(): " +  filechooser.getCurrentDirectory());
			 System.out.println("getSelectedFile() : " +  filechooser.getSelectedFile());
	        
		      textfield_filepath.setText(filechooser.getSelectedFile().toString());
		      }
		    else {
		      System.out.println("No Selection ");
		      }
		     
	}
	
}
