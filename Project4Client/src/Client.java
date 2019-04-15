import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Client implements Runnable {
	
	private Socket clientSocket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String clientName;	
	private ObservableList<String> clientLog;
	private ObservableList<String> playerList;
	private int clientScore;
	private ClientApp clientApp;
	
	

	
	
	
//==================================================================================
	//Constructors

	public Client() {

	}
	
	public Client(String givenClientName,String ipAdresss, int port, ClientApp ca) throws UnknownHostException,IOException{

		clientSocket = new Socket(ipAdresss, port);
		
		out = new ObjectOutputStream (clientSocket.getOutputStream());
		in = new ObjectInputStream(clientSocket.getInputStream());
		
		
	
		clientLog = FXCollections.observableArrayList();
		playerList =  FXCollections.observableArrayList();
		clientName = givenClientName;
		clientApp = ca;

		//send client name to server
		out.writeObject(clientName);
		this.clientScore = 0;			
	}

//==================================================================================
	//Setters
	public void setClientScore(int clientScore) {
		this.clientScore = clientScore;
	}
	
	
	public void changeDupName(String name){
		this.clientName = name;
	}
	
	
//==================================================================================
	//Getters
	

	public ObservableList<String> getClientLog(){
		return this.clientLog;
	}
	
	public ObservableList<String> getPlayerList(){
		return this.playerList;
	}
	
	public int getClientScore() {
		return clientScore;
	}

	
//==================================================================================



	@Override
	public void run() {
		// TODO Auto-generated method stub

		while(true) {

			try {
				
				//reads incoming messages 				
				Serializable incominMessage = (Serializable) in.readObject();
	
				
				
				Platform.runLater(new Runnable() {
					public void run() {
						
						//check if incoming data is HashMap
						if (incominMessage instanceof HashMap) {
							//clear the playerList			
							playerList.clear();
							String n = null;
							
							for (Object key : ((HashMap<?, ?>) incominMessage).keySet()) {
								
								String str = (String) ((HashMap<?, ?>) incominMessage).get(key);
	
								
								if(playerList.contains(str)){
									n = clientApp.sameName(str);
									playerList.add(n);
									changeDupName(n);
								}
								else{
									playerList.add(str);
								}
								
							}
								
							
							//setPlayerList(plist);
							System.out.println("aaa");
							
							
						}
						
						//string
						if (incominMessage instanceof String) {
							
							String msgStr = incominMessage.toString();
							if (msgStr.contains("challenge:")) {
								String[] exploded = msgStr.split("challenge:");
								//call this method which will then popup challange request in client GUI
								handleChallenge(exploded[1]);
							}
							else {
								clientLog.add((String) incominMessage);
							}
							
							
							
							
						}
					}
				});

			} catch (SocketException e) {
				Platform.runLater(new Runnable() {
					public void run() {
						clientLog.add("Server Connection Lost");
					}

				});
				break;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	

//==================================================================================

	public void challenge(String name) throws Exception {
		this.sendMessage("challenge:" + name);
	}
	
	//this method trigers the popup challenge notification in client App
	public void handleChallenge(String name) {
		this.clientApp.challangePopup(name);
	}
	
	// this method will be called in client App when user accepts challenge
	public void acceptChallenge(String name) throws Exception {
		this.sendMessage("accept:" + name);
	}

	
	public void sendMessage(Serializable data) throws Exception{
		out.writeObject(data);
		out.flush();
		
	}
	

}
