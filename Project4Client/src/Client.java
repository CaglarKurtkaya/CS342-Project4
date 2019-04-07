import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client implements Runnable {
	
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	private String clientName;	
	private ObservableList<String> clientLog;
	private int clientScore;
	
	
	
//==================================================================================
	public Client() {
		
	}
	
	//Constructor
	public Client(String givenClientName,String ipAdresss, int port) throws UnknownHostException,IOException{

		clientSocket = new Socket(ipAdresss, port);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		clientLog = FXCollections.observableArrayList();
		clientName = givenClientName;

		//send client name to server
		out.println(clientName);
		this.clientScore = 0;			
	}

//==================================================================================
	//Setters
	public void setClientScore(int clientScore) {
		this.clientScore = clientScore;
	}
	
	public void setClientLog(ObservableList<String> oList) {
		this.clientLog = oList;
	}
	
	
//==================================================================================
	//Getters
	

	public ObservableList<String> getClientLog(){
		return this.clientLog;
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
				final String incominMessage = in.readLine();
				
				
				Platform.runLater(new Runnable() {
					public void run() {
						clientLog.add(incominMessage);
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
			}

		}

	}
	

//==================================================================================

	//sends messages 
	public void sendMessage(String input) {
		this.out.println(input);
	}
	

}
