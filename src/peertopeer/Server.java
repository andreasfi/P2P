package peertopeer;

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

	private PrintWriter pout;
	private SubClient newSubClient;

	//The SubClient is the client who's connecting to the server, it contains client informations like ip, name...
	private SubClient client_distant;

	//This is a SubClientList which is sent to the client to choose the client to connect to
	private List<SubClient> subClientList = new ArrayList<SubClient>();

	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private InputStreamReader isr;
	private String choice;
	private Socket threadSocket;

	Logger log;
	
	/*
	 * this is the constructor who needs to get the Socket, and the subclient list to send it to
	 * the client
	 */
	public Server(Socket threadSocket, List<SubClient> subClientList){
		this.threadSocket = threadSocket;
		this.subClientList = subClientList;
		this.log = Logger.getLogger("Log");
	}

	/*
	 * This is the method that's listening for any connection onto the server
	 */
	public void listen(){
		try {
			pout = new PrintWriter(threadSocket.getOutputStream()); // send to client

			BufferedReader buffin = new BufferedReader (new InputStreamReader (threadSocket.getInputStream()));
			log.info("Server is listening");
			String action ="";
			boolean alreadyClient = false;
			while(true){
				action = buffin.readLine().trim();

				switch(action){
				case "receiveClient":
					inputStream = new ObjectInputStream(threadSocket.getInputStream());
					newSubClient = (SubClient) inputStream.readObject();
					for (int i = 0; i < subClientList.size(); i++) {
						if(subClientList.get(i).getIP().equals(newSubClient.getIP())){
							alreadyClient=true;
						}
					}
					if(!alreadyClient){
						subClientList.add(new SubClient(newSubClient.getIP(), newSubClient.getName(), newSubClient.getFilepath(), newSubClient.getList()));
					} else {
						for (int i = 0; i < subClientList.size(); i++) {
							if(subClientList.get(i).equals(newSubClient)){
								subClientList.set(i, newSubClient);
							}
						}
					}
					
					log.info("Client received");
					break;
				case "sendFiles":
					outputStream = new ObjectOutputStream(threadSocket.getOutputStream());
					System.out.println(subClientList.get(0).getIP());
					outputStream.writeObject(subClientList);
					outputStream.flush();
					log.info("List send");
					break;
				case "quit":
					inputStream.close();
					outputStream.close();
					threadSocket.close();
					pout.close();
					log.info("Server closed");
					return;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.severe("Connection error");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.severe("Connection error");
		}
	}
	/*
	 * Here we're running our server threads to make them listening to any client connections
	 */
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

		/*
		 * This is where we're getting the ip of the server
		 */
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

			/*
			 * This is the serverSocker who needs the port, the backlog and the ipaddress
			 */
			mySkServer = new ServerSocket(45000,10,localAddress);

			//mySkServer.setSoTimeout(180000);

			System.out.println("Usedd IpAddress :" + mySkServer.getInetAddress());
			System.out.println("Listening to Port :" + mySkServer.getLocalPort());

			/*
			 * Here we're accepting connection
			 */
			int threadcount = 1;
			while(true){
				Socket threadSocket = mySkServer.accept(); 	
				ipAddress = threadSocket.getRemoteSocketAddress().toString();
				System.out.println(ipAddress + " is connected ");
				new Thread(new Server(threadSocket, subClientList)).start();
				System.out.println("count: "+threadcount++);
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