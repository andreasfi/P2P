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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Server implements Runnable {
	PrintWriter pout;
	SubClient newSubClient;
	
	
	SubClient client_distant;
	List<SubClient> subClientList = new ArrayList<SubClient>();
	
	ObjectInputStream inputStream;
	ObjectOutputStream outputStream;
	InputStreamReader isr;
	String choice;
	Socket threadSocket;
	
	Logger log;
	
	public Server(Socket threadSocket, List<SubClient> subClientList){
		this.threadSocket = threadSocket;
		this.subClientList = subClientList;
		this.log = Logger.getLogger("Log");
	}
	


	public void listen(){
		try {
			pout = new PrintWriter(threadSocket.getOutputStream()); // send to client
			
			BufferedReader buffin = new BufferedReader (new InputStreamReader (threadSocket.getInputStream()));
			log.info("Server is listening");
			String action ="";
			while(true){
				action = buffin.readLine().trim();
				
				switch(action){
				case "receiveClient":
					inputStream = new ObjectInputStream(threadSocket.getInputStream());
					newSubClient = (SubClient) inputStream.readObject();
					subClientList.add(new SubClient(newSubClient.getIP(), newSubClient.getName(), newSubClient.getList()));
					log.info("Client received");
					break;
				case "sendFiles":
					outputStream = new ObjectOutputStream(threadSocket.getOutputStream());
					System.out.println(subClientList.get(0).getIP());
					outputStream.writeObject(subClientList);
					outputStream.flush();
					outputStream.close();
					log.info("List send");
					break;
				case "quit":
					threadSocket.close();
					pout.close();
					log.info("Server closed");
					return;
				}
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("Connection error");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("Connection error");
		}
		
		
	}
	
	@Override
	public void run() {
		listen();
	}

	public static void main(String[] args) {
		String interfaceName = "eth1";
		InetAddress localAddress = null;
		NetworkInterface ni;
		ServerSocket mySkServer;
		Socket srvSocket = null ;
		String ipAddress;
		
		Logger log = null;
		
		List<SubClient> subClientList = new ArrayList<SubClient>();
		
		try {
			ni = NetworkInterface.getByName(interfaceName);
			Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
			log = new Log().getLog();
			log.info("new Connection");
			while(inetAddresses.hasMoreElements()) {
				InetAddress ia = inetAddresses.nextElement();

				if(!ia.isLinkLocalAddress()) {
					if(!ia.isLoopbackAddress()) {
						System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
						localAddress = ia;
					}
				}   
			}
			
			mySkServer = new ServerSocket(45000,10,localAddress);
			
			mySkServer.setSoTimeout(180000);
			
			System.out.println("Usedd IpAddress :" + mySkServer.getInetAddress());
			System.out.println("Listening to Port :" + mySkServer.getLocalPort());
			

			while(true){
				Socket threadSocket = mySkServer.accept(); 	
				ipAddress = threadSocket.getRemoteSocketAddress().toString();
				System.out.println(ipAddress + " is connected ");
				Thread thread = new Thread(new Server(threadSocket, subClientList));
				thread.start();
			}	
			
			
		} catch (SocketException e) {
			e.printStackTrace();
			log.info("Error");
		} catch (IOException e) {
			e.printStackTrace();
			log.info("Error");
		}
	}
}