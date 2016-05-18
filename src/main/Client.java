package main;
/**
 * Andreas Fischer
 * P2P
 * 14.05.2016
 * Client.java
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
		String myIp = "192.168.1.101";
		String myName = "AndyOrdi";
		File f = new File("C:\\temp");
		List<File> myPath = new ArrayList<File>(Arrays.asList(f.listFiles())); // Get all files upon this path
		SubClient subclient = new SubClient(myIp, myName, myPath); // Create this client
		
		
		
		Thread client = new Thread(startClient(subclient));
		
		Thread server = new Thread(startServer(subclient));
		client.start();
		server.start();	
		
		
	}
	private static Runnable startClient(SubClient subclient){
		Client c = new Client(subclient);
		c.connectToServer("192.168.108.10", 45000);
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
		InetAddress localAddress;
		System.out.println("Starting server listen");
		try {
			localAddress = InetAddress.getLocalHost();
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
			
			
			// Check if file exists
			if(checkFileExistence(filepath)){
				File fileSendToSend = new File(filepath);
				
				byte[] buf = new byte[8192];
				InputStream is = new FileInputStream(fileSendToSend);
				
				int c = 0;
				
				while ((c = is.read(buf, 0, buf.length)) > 0) {
		            oos.write(buf, 0, c);
		            oos.flush();
		        }
				oos.close();
			    System.out.println("stop");
			    is.close();
				
			} else {
				System.out.println("File does not exists");
			}
				
			
			
			
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