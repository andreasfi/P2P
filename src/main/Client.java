package main;
/**
 * Andreas Fischer
 * P2P
 * 14.05.2016
 * Client.java
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;


public class Client {
	private String serverIp;
	private int serverPort;
	List<SubClient> subClientList = new ArrayList<SubClient>();
	
	InetAddress myAdress;
	private String myIp;
	private String myName;
	private List<File> myPath;
	SubClient subclientMe;
	
	Scanner scanner = new Scanner(System.in);
	PrintWriter pout;
	PrintWriter write;
	
	Socket mySocket;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	OutputStream os;
	InetAddress LocalAddress;
	
	public Client(SubClient subclient){
		subclientMe = subclient;
	}
	
	/*
	 * Method that is connecting the client to the server	
	 */
	public void connectToServer(String serverIp, int serverPort){
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		
		try {
			LocalAddress = InetAddress.getLocalHost();
			System.out.println("My local address is the following one : " +LocalAddress);
			mySocket = new Socket(InetAddress.getByName(serverIp),serverPort); // Connect to server / Open socket
			System.out.println("The client is connected to " + serverIp);
			
			write = new PrintWriter(mySocket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Method that is sending the subclient object to the server
	 * Subclient contains all client informations
	 */
	public void sendObjectToServer(){
		write.println("receiveClient");
		write.flush();
		try {
			os = mySocket.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(subclientMe);
			oos.flush();
			System.out.println("SubClient object sent.");			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	public void getClientFileList(){
		write.println("sendFiles");
		write.flush();
		try {
			ois = new ObjectInputStream(mySocket.getInputStream());
			subClientList = (List<SubClient>) ois.readObject();
			oos.flush();
			System.out.println("Object received.");
			printSubclientList(subClientList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Method that is closing the connection with the server
	 */
	public void closeConnection(){
		write.println("quit");
		write.flush();
		try {
			
			oos.close();
			ois.close();
			mySocket.close();
			System.out.println("Connections now closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * This is the method that is printing the subclient list, received from the server
	 */
	public void printSubclientList(List<SubClient> subclientlist){
		for (int i = 0; i < subclientlist.size(); i++) {
			System.out.print("Name:" + subclientlist.get(i).getName());
			System.out.print("IP:" + subclientlist.get(i).getIP());
			System.out.print(" Filelist: ");
			if(subclientlist.get(i).getList()!= null){
				for (int j = 0; j < subclientlist.get(i).getList().size(); j++) {
					System.out.println(subclientlist.get(i).getList().get(j).toString());
				}
			} else {
				System.out.println("List empty");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		// Create the client
		String myIp = "192.168.108.102";
		String myName = "AndyOrdi";
		File f = new File("/tmp");
		List<File> myPath = new ArrayList<File>(Arrays.asList(f.listFiles())); // Get all files upon this path
		SubClient subclient = new SubClient(myIp, myName, myPath); // Create this client
		
		
		
		Thread client = new Thread(startClient(subclient));
		
		
		client.start();
		Thread server = new Thread(startServer(subclient));
		server.start();	
		
		
	}
	private static Runnable startClient(SubClient subclient){
		Client c = new Client(subclient);
		Scanner sc = new Scanner(System.in);

		
		
		String ip = "192.168.108.10";
		System.out.println("connecting to "+ip);
		c.connectToServer(ip, 45000);
		c.sendObjectToServer();
		c.getClientFileList();
		c.closeConnection();
		
		
		int navigation = 1;
		Scanner scanner = new Scanner(System.in);
		while(navigation != 9){
			System.out.println("Navigate.");
			navigation = scanner.nextInt();
			
			/*
			 * Show all files
			 * Ask for file
			 * 
			 */
			switch (navigation) {
			case 1:
				// ask for file to send
				System.out.println("What file you want?");
				// show file list
				// scanner answer
				c.connectToClient("192.168.108.102", 45002, "test");
				//PrintWriter write = new PrintWriter(arg0)
				//write.println("quit");
				//write.flush();
				break;

			default:
				break;
			}
		}
		
		return null;
	}
	private static Runnable startServer(SubClient subclient){
		InetAddress localAddress = null;
		System.out.println("Starting server listen");
		NetworkInterface ni;
		String interfaceName = "eth1";
		try {
			
			ni = NetworkInterface.getByName(interfaceName);
			Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
			//log = new Log().getLog();
			//log.info("new Connection");
			while(inetAddresses.hasMoreElements()) {
				InetAddress ia = inetAddresses.nextElement();

				if(!ia.isLinkLocalAddress()) {
					if(!ia.isLoopbackAddress()) {
						System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
						localAddress = ia;
					}
				}   
			}
			
			System.out.println(localAddress);
			ServerSocket MySkServer = new ServerSocket(45002,10,localAddress);
			while(true)
			{
				Socket clientSocket = MySkServer.accept();
				System.out.println("connection request received");
				Thread t = new Thread(new ClientServer(clientSocket, subclient));
				t.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null ;
		}
	}
	
	public void connectToClient(String ip, int port, String filepath){
		
		int bytesRead;
	    int current = 0;
	    FileOutputStream fos = null;
	    BufferedOutputStream bos = null;
	    Socket clientsock = null;
	    
	    String downloadPath = "C:/temp/test.java";
	    int FILE_SIZE = 6022386;
	    
	    try{
	    	clientsock = new Socket(ip, port);
	    	System.out.println("Connecting...");
	    	
	    	byte [] mybytearray  = new byte [FILE_SIZE];
		      InputStream is = clientsock.getInputStream();
		      fos = new FileOutputStream(downloadPath);
		      bos = new BufferedOutputStream(fos);
		      bytesRead = is.read(mybytearray,0,mybytearray.length);
		      current = bytesRead;
		      
		      do {
			         bytesRead =
			            is.read(mybytearray, current, (mybytearray.length-current));
			         if(bytesRead >= 0) current += bytesRead;
			      } while(bytesRead > -1);
		      
		      bos.write(mybytearray, 0 , current);
		      bos.flush();
		      System.out.println("File " + downloadPath
		          + " downloaded (" + current + " bytes read)");
	    	
		      
		      if (fos != null) fos.close();
		      if (bos != null) bos.close();
		      if (clientsock != null) clientsock.close();
	    }catch(Exception e){
	    	
	    }
	    
	}
	
	
}



class ClientServer implements Runnable {

	Socket srvSocket = null ;
	Socket clientSocket;
	ServerSocket mySkServer;
	
	InetAddress localAddress = null;
	
	ObjectOutputStream oos;
	ObjectInputStream ois;
	OutputStream os;
	
	SubClient subclient;
	public ClientServer(Socket clientSocket, SubClient subclient){
		this.clientSocket = clientSocket;
		this.subclient = subclient;
	}
	public void acceptConnection(){
		try {
			/*mySkServer = new ServerSocket(4445);
			mySkServer.setSoTimeout(180000);
			
			srvSocket = mySkServer.accept(); 	
			String ipAddress = srvSocket.getRemoteSocketAddress().toString();
			System.out.println(ipAddress + " is connected ");
			*/
			oos =  new ObjectOutputStream(clientSocket.getOutputStream());
			
			BufferedReader buffin = new BufferedReader (new InputStreamReader (clientSocket.getInputStream()));
			
			String filepath = buffin.readLine().trim();
			FileInputStream fis = null;
			BufferedInputStream bis = null;
		    OutputStream os = null;
			// Check if file exists
			if(checkFileExistence(filepath)){
				File myFile = new File (filepath);
				byte [] mybytearray  = new byte [(int)myFile.length()];
				
				fis = new FileInputStream(myFile);
				bis = new BufferedInputStream(fis);
		        bis.read(mybytearray,0,mybytearray.length);
		        os = clientSocket.getOutputStream();
		        System.out.println("Sending " + filepath + "(" + mybytearray.length + " bytes)");
		        os.write(mybytearray,0,mybytearray.length);
		        os.flush();
		        System.out.println("Done.");
				
			} else {
				System.out.println("File does not exists: " + filepath);
			}
			if (bis != null) bis.close();
	        if (os != null) os.close();
	        if (clientSocket!=null) clientSocket.close();
			
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public boolean checkFileExistence(String file){
		if(subclient.getList().contains(file)){
			return true;
		} else {
			return false;	
		}
	}
	@Override
	public void run() {
		acceptConnection();
	}
	
}