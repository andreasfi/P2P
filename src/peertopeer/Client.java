/**
 * Andreas Fischer
 * P2PTests
 * 20.05.2016
 * Client.java
 */
package peertopeer;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;

/**
 * @author Andreas
 *
 */
public class Client {
	List<String> filenames;
	List<SubClient> subClientList;
	String mylocalip;
	Socket mySocket;
	String filepath;
	SubClient subclientMe;

	public Client(String serverip, String serverport, String filepath){
		this.filepath = filepath;
		mylocalip = getLocalIp();
		try {
			InetAddress LocalAddress = InetAddress.getLocalHost();
			System.out.println("1My local address is the following one : " +LocalAddress);
			mySocket = new Socket(InetAddress.getByName(serverip),Integer.parseInt(serverport)); // Connect to server / Open socket
			System.out.println("The client is connected to " + serverip);
			
			PrintWriter write = new PrintWriter(mySocket.getOutputStream());
			
			filenames = new ArrayList<String>();
			filenames = getFileNames(filepath);
			subclientMe = new SubClient(mylocalip, "andy", filepath, filenames);
			System.out.println(mylocalip);
			write.println("receiveClient");
			write.flush();
			OutputStream os = mySocket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(subclientMe);
			oos.flush();
			System.out.println("SubClient object sent.");	

			write.println("sendFiles");
			write.flush();
			
			ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
			subClientList = (List<SubClient>) ois.readObject();
			oos.flush();
			System.out.println("Object received.");
			printSubclientList(subClientList);
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public List<String> getFileNames(String filepath){
		File folder = new File(filepath);
		File[] listOfFiles = folder.listFiles();
		List<String> filenames = new ArrayList<String>();
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		        filenames.add(listOfFiles[i].getName());
		      } else if (listOfFiles[i].isDirectory()) {
		        //System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
		return filenames;
	}
	public void closeConnection(){

		System.out.println("Connections now closed");
	}
	public void printSubclientList(List<SubClient> subclientlist){
		for (int i = 0; i < subclientlist.size(); i++) {	
			
			System.out.print("Name:" + subclientlist.get(i).getName());
			System.out.print("IP:" + subclientlist.get(i).getIP());
			System.out.print(" Filelist: ");
			System.out.println();
			if(subclientlist.get(i).getList()!= null){
				for (int j = 0; j < subclientlist.get(i).getList().size(); j++) {
					System.out.println("ID: "+(j+1)+", File: "+subclientlist.get(i).getList().get(j));
				}
			} else {
				System.out.println("List empty");
			}
			System.out.println();
		}
	}
	public void askForFile(String fileiwant){
		String fileip = null;
		String fileiwantpath = null;
		for (int i = 0; i < subClientList.size(); i++) {
			for (int j = 0; j < subClientList.get(i).getList().size(); j++) {
				if(subClientList.get(i).getList().get(j).toString().equals(fileiwant)){
					fileip = subClientList.get(i).getIP();
					fileiwantpath = subClientList.get(i).getFilepath()+"/"+fileiwant;
				}
			}
		}

		if(fileip == null){
			return;
		}
		System.out.println("Download server found: "+fileip+", for file "+fileiwantpath);
		int bytesRead;
	    int current = 0;
	    FileOutputStream fos = null;
	    BufferedOutputStream bos = null;
	    Socket clientsock = null;
	    
	    int FILE_SIZE = 6022386;
	    
	    try{
	    	clientsock = new Socket(fileip, 45001);
	    	System.out.println("Connecting...");
	    	
	    	PrintWriter write = new PrintWriter(clientsock.getOutputStream());
			write.println(fileiwantpath);
			write.flush();
			
			
	    	byte [] mybytearray  = new byte [FILE_SIZE];
		      InputStream is = clientsock.getInputStream();
		      System.out.println("Writing to path: "+filepath+"/"+fileiwant);
		      fos = new FileOutputStream((this.filepath+"/"+fileiwant));
		      System.out.println("stop");
		      bos = new BufferedOutputStream(fos);
		      bytesRead = is.read(mybytearray,0,mybytearray.length);
		      current = bytesRead;
		      System.out.println("Writing to path: "+filepath);
		      int counts = 0;
		      do {
			         bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
			         if(bytesRead >= 0) current += bytesRead;
			         System.out.println(counts);
			         counts++;
			  } while(bytesRead > -1);
		      
		      bos.write(mybytearray, 0 , current);
		      bos.flush();
		      System.out.println("File " + fileiwantpath
		          + " downloaded (" + current + " bytes read)");
	    	
		      
		      if (fos != null) fos.close();
		      if (bos != null) bos.close();
		      if (clientsock != null) clientsock.close();
	    }catch(Exception e){
	    	
	    }
	}

	public String getLocalIp(){
		InetAddress LocalAddress=null;
		try {
			LocalAddress = InetAddress.getLocalHost();
			System.out.println("My local address is the following one : " +LocalAddress.getHostAddress().toString());
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
		return LocalAddress.getHostAddress().toString();
	}
	
	
	
	
	public DefaultListModel getLocalFilenamesAsModel() {
		   DefaultListModel listModel = new DefaultListModel<>();
		   for (int i = 0; i < filenames.size(); i++) {
			   listModel.addElement(filenames.get(i));
		   }   
		return listModel;
	}
	public DefaultListModel getServerFilenamesAsModel(){
		DefaultListModel listModel = new DefaultListModel<>();
		for (int i = 0; i < subClientList.size(); i++) {
			if(subClientList.get(i).getIP() != ""){
				for (int j = 0; j < subClientList.get(i).getList().size(); j++) {
					listModel.addElement(subClientList.get(i).getList().get(j));
				}
			}
		} 
		return listModel;
	}
	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}
	public String getMylocalip() {
		return this.mylocalip;
	}
	public void setMylocalip(String mylocalip) {
		this.mylocalip = mylocalip;
	}
	public SubClient getSubclientMe() {
		return this.subclientMe;
	}
	public void setSubclientMe(SubClient subclientMe) {
		this.subclientMe = subclientMe;
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
	String locfilepath;
	
	SubClient subclient;
	public ClientServer(Socket clientSocket, SubClient subclient){
		System.out.println("Starting Client-side server");
		this.clientSocket = clientSocket;
		this.subclient = subclient;
		this.locfilepath = subclient.getFilepath();
	}
	public void acceptConnection(){
		try {
			
			
			BufferedReader buffin = new BufferedReader (new InputStreamReader (clientSocket.getInputStream()));
			
			String filepath = buffin.readLine().trim();
			FileInputStream fis = null;
			BufferedInputStream bis = null;
		    OutputStream os = null;
			// Check if file exists
			if(checkFileExistence(filepath)){
				System.out.println("Request for file"+filepath);
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
			return true;	
		}
	}
	@Override
	public void run() {
		acceptConnection();
	}
	
}