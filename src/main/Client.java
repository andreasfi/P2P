package main;
/**
 * Andreas Fischer
 * P2P
 * 14.05.2016
 * Client.java
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Client implements Runnable {
	private String serverIp;
	private int serverPort;
	List<SubClient> subClientList = new ArrayList<SubClient>();
	
	InetAddress myAdress;
	private String myIp;
	private String myName;
	private List<File> myPath;
	SubClient subclientMe;
	
	Scanner scanner = new Scanner(System.in);
	Socket mySocket;
	ObjectOutputStream objOutStr;
	ObjectInputStream objInpStr;
	OutputStream os ;
	InetAddress LocalAddress;
	public Client(){
		myIp = "192.168.1.101";
		myName = "Client name";
	}
	
	
	
	public void connetToServer(String serverIp, int serverPort){
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		
		try {
			// Connect to server / Open socket
			mySocket = new Socket(InetAddress.getByName(serverIp),45000);
			try{
				LocalAddress = InetAddress.getLocalHost();
				System.out.println("The local address of the client is the following one : " +LocalAddress);
				//System.out.println("My local address is the following one : " +LocalAddress);
			}catch(UnknownHostException e){
				e.printStackTrace();
			}
			System.out.println("The client is connected to " + serverIp);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendSubClient(){

		System.out.println("Path to folder you want to share :");
		//String pathSharedFolder = scanner.nextLine();
		String pathSharedFolder = "C:\\temp";

		File f = new File(pathSharedFolder);
		myPath = new ArrayList<File>(Arrays.asList(f.listFiles())); // Get all files upon this path
		subclientMe = new SubClient(myIp, myName, myPath); // Create this client
		System.out.println("Created Client: " + subclientMe.getIP());
	}
	public void sendObjectToServer(){
		try {
			os = mySocket.getOutputStream();
			objOutStr = new ObjectOutputStream(os);
			objOutStr.writeObject(subclientMe);
			objOutStr.flush();
			System.out.println("SubClient object sent.");			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	public void getClientFileList(){
		try {
			objInpStr = new ObjectInputStream(mySocket.getInputStream());
			subClientList = (List<SubClient>) objInpStr.readObject();
			objOutStr.flush();
			System.out.println("Object received.");
			printSubclientList(subClientList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void closeConnection(){
		try {
			objOutStr.close();
			objInpStr.close();
			mySocket.close();
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
			/*for (int j = 0; j < subclientlist.get(i).getList().size(); j++) {
				subclientlist.get(i).getList().get(j).toString();
			}*/
		}
	}
	
	public static void main(String[] args) {
		
		Thread tserver = new Thread(new Client());
		tserver.start();
		
		
		/*
		Client c = new Client();
		c.connetToServer("192.168.108.10", 45000);
		c.sendSubClient();
		c.sendObjectToServer();
		c.getClientFileList();
		
		*/
	}



	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		System.out.println("asdf");
		connetToServer("192.168.108.10", 45000);
		sendSubClient();
		sendObjectToServer();
		getClientFileList();
		
	}

}
