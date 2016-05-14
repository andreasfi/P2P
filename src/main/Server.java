package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.Collections;
import java.util.logging.Logger;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;



public class Server implements Runnable {

	Socket srvSocket = null ;
	InetAddress localAddress = null;
	ServerSocket mySkServer;
	PrintWriter pout;
	Scanner sc; 
	int i =0;
	String interfaceName = "eth1";
	SubClient client_distant;
	List<SubClient> SubClientList = new ArrayList<SubClient>();
	String ipAddress;
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	InputStreamReader isr;
	String choice;

	public void ServerClass()
	{
		SubClientList.add(new SubClient("129123", "asdfasfd", null));
	}
	
	public void connect()
	{
		SubClient in = null;
		
		try {

			NetworkInterface ni = NetworkInterface.getByName(interfaceName);
			Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
			while(inetAddresses.hasMoreElements()) {
				InetAddress ia = inetAddresses.nextElement();

				if(!ia.isLinkLocalAddress()) {
					if(!ia.isLoopbackAddress()) {
						System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
						localAddress = ia;
					}
				}   
			}

			//Warning : the backlog value (2nd parameter is handled by the implementation
			mySkServer = new ServerSocket(45000,10,localAddress);

			//set 3min timeout
			mySkServer.setSoTimeout(180000);

			System.out.println("Usedd IpAddress :" + mySkServer.getInetAddress());
			System.out.println("Listening to Port :" + mySkServer.getLocalPort());

			//wait for client connection		
			srvSocket = mySkServer.accept(); 	
			ipAddress = srvSocket.getRemoteSocketAddress().toString();
			System.out.println(ipAddress + " is connected "+ i++);

			//open the output data stream to write on the client
			pout = new PrintWriter(srvSocket.getOutputStream()); 		  

			inputStream = new ObjectInputStream(srvSocket.getInputStream());
	        //outputStream = new ObjectOutputStream(srvSocket.getOutputStream());
	        
			in = (SubClient) inputStream.readObject();
			SubClientList.add(new SubClient(in.getIP(), in.getName(), in.getList()));
			
			System.out.println(in.getIP());
			
			sendClientList();
			//getClientChoice();
			//giveClientIP();
					
			//Then die
			System.out.println("Now dying");
			srvSocket.close();
			mySkServer.close();
			pout.close();


		}catch (SocketException e) {

			System.out.println("Connection Timed out");
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void sendClientList()
	{
		try {
			outputStream = new ObjectOutputStream(srvSocket.getOutputStream());
			System.out.println(SubClientList.get(0).getIP());
			outputStream.writeObject(SubClientList);
			outputStream.flush();
			outputStream.close();
			System.out.println("List send");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void giveClientIP()
	{
		SubClient clientChoosed = (SubClient) SubClientList.get(1);
		clientChoosed.getIP();
		
		try {
			
			pout = new PrintWriter(srvSocket.getOutputStream());
			pout.println(clientChoosed.getIP());
			System.out.println("The ip send to connect to the client is " + clientChoosed.getIP());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getClientChoice()
	{
		
		try {
			BufferedReader buffin = new BufferedReader (new InputStreamReader (srvSocket.getInputStream()));
			choice = buffin.readLine();
			System.out.println("Your choice is" + choice);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	 
		public static void main(String[] args) {
		// TODO Auto-generated method stub

		Server server = new Server();
		
		server.connect();
		
	}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}

}