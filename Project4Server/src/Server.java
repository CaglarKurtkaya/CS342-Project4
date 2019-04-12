
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



public class Server implements Runnable {
	
	private int port;	
	private HashMap<String, ConnectionThread> connThreads;
	private ObservableList<String> serverLog;    // used for adding messages in ListView(in ServerApp)
	private ObservableList<String> playerMoveLog;	
	private volatile Boolean running = false;
	private ArrayList<Game> games;
	
	
	//Following members accessible only by classes in the same package.
	//ArrayList<Socket> clients;         
	ServerSocket serverSocket;
	Thread clientThread;
	Socket socket;
	ConnectionThread cnnThd;

	
	
	//Constructor
	public Server(int givenPort) {
		if(givenPort < 1 || givenPort > 65535) {
            throw new IllegalArgumentException();
		}
		port = givenPort;
		//clients = new ArrayList<Socket>();
		connThreads = new HashMap<String, ConnectionThread>();	
		serverLog = FXCollections.observableArrayList();	
		playerMoveLog = FXCollections.observableArrayList();
		//clientNames = FXCollections.observableArrayList();	
		games = new ArrayList<Game>();
	}
	
//========================================================================================
	//Getters
	
	public int getPort() {
		return this.port;
	}

	public ObservableList<String> getPlayerMoveLog() {
		return playerMoveLog;
	}
	
	
	public ObservableList<String> getServerLog(){
		return this.serverLog;
	}
	
	public Boolean getRunning() {
		return this.running;
	}
	
	public HashMap<String, ConnectionThread> getConnThreads (){
		return this.connThreads;
	}

	
//========================================================================================
	//Setters

	public void setPlayerMoveLog(String input) {
		this.playerMoveLog.add(input);
	}
	
	public void setServerLog(String input) {
		this.serverLog.add(input);
	}
	
	public void setRunning(boolean b) {
		this.running = b;
	}
	
	public void addGame(Game g) {
		this.games.add(g);
	}
	
	public void removeGame(Game g) {
		this.games.remove(this.games.indexOf(g));
	}
	
//========================================================================================
	//methods for the game 
	
	
	// This is used to update the List when we get the client names in the ConnectionThread run() method
	public void updateMe() {
		this.updateList();
	}

	public synchronized void updateList() {
		
		HashMap<String, String> list = new HashMap<String, String>();
		
		//HashMap.keySet() method in Java is used to create a set out of the key elements 
		//contained in the hash map.
		//iterate the connThreads get the threads and put it into 
		//HaspMay<String,String> list to send it to clients
		//This will be shown in the clients GUI to show connected clients on the server
		for (String key : this.connThreads.keySet()) {			
			ConnectionThread t = this.connThreads.get(key);
			list.put(t.getThreadID(), t.getSafeClientName());
		}
		
		this.sendHashmapMessageAll(list);
	}

	

	
//========================================================================================
	

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			serverSocket = new ServerSocket(port);

			// Listen incoming requests
			while(true) {
				if (!this.running) {
					

					Platform.runLater(()-> {
						serverLog.add("Waiting for Client");
					});

					socket = serverSocket.accept();
					// add clients to the list
					//clients.add(socket);

					Platform.runLater(()->{
						serverLog.add("Client " + socket.getRemoteSocketAddress() + " connected");
					});

					cnnThd = new ConnectionThread(this, socket);
					clientThread = new Thread(cnnThd);
					clientThread.setDaemon(true);
					clientThread.start();
				
					
					
					String str = Long.toString(clientThread.getId());
					this.connThreads.put(str, cnnThd);
					cnnThd.setThreadID(str);
				
					
				} 


			}// end of while


		}
		catch(SocketException e) {

		}
		catch(IOException e) {			
			e.printStackTrace();
		}

	}


//========================================================================================
	//Thread methods 
	
	public void stopServerThread() {
		this.setRunning(true);
		this.serverSocket = null;
	}
	
	public void sendHashmapMessageAll(HashMap<String, String> input) {
		Serializable messages = (Serializable) input;
	
		for (String key : this.connThreads.keySet()) {
			ConnectionThread it = this.connThreads.get(key);
			try {
				it.sendMessage(messages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	
	}
	
	public void sendMessageAll(String input) {
		Serializable messages = (Serializable) input;
	
		for (String key : this.connThreads.keySet()) {
			ConnectionThread it = this.connThreads.get(key);
			
			try {
				it.sendMessage(messages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	
	}
	
	public void sendToSpecificThread(ConnectionThread t, String input) {
		Serializable messages = (Serializable) input;
		
		for (String key : this.connThreads.keySet()) {
			ConnectionThread it = this.connThreads.get(key);
			if(it == t) {
				try {
					it.sendMessage(messages);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
	//p1 is first asking for challange
	//p2 is accepting challange.
	public void handleChallange(String p1, String p2, Boolean accept) {
		
		ConnectionThread player1 = new ConnectionThread();
		ConnectionThread player2 = new ConnectionThread();
		
		//used to make sure we found both Threads in our HashMap
		Boolean p1set = false;
		Boolean p2set = false;
		
		//Here we try to find threads by giving p1, p2 values
		//p1 and p2 represents their name. Our key for HashMap is username!
		for (String key : this.connThreads.keySet()) {
			ConnectionThread it = this.connThreads.get(key); 
			
			if (it.getSafeClientName().equals(p1)) {
				player1 = it;
				p1set = true;
			}
			
			if (it.getSafeClientName().equals(p2)) {
				player2 = it;
				p2set = true;
			}
			
		}
		
		//When we get here check if we find both threads	
		if ( (p1set == true) && (p2set == true) ) {
			
			//Since in this method player1 challenges player2
			// We first check if player2 already is in a game
			if (player2.getGameStarted() == true) {
				player1.getMessage(p2 + " is playing another game. please wait and try again.");
			}
			else {
				// if given boolean value is true, challange has been accepted. Start the game!
				if (accept == true) {
					this.startAGame(player1, player2);
				}
				//if not ask for challenge
				else {
					this.askChallange(player1, player2);
				}
			}
			
			
		}
		else {
			//TODO
			//error-one or 2 player thread cannot found.
		}
		
	}
	
	//t1 is asking to challenge t2
	// by calling methods in ConnectionThread->askChallange
	public void askChallange(ConnectionThread t1, ConnectionThread t2) {
		t2.askChallange(t1);
	}
	

	//Star game between t1 and t2 
	public void startAGame (ConnectionThread t1, ConnectionThread t2) {
		Game aGame = new Game(t1, t2, this);
		t1.setGame(aGame);
		t2.setGame(aGame);
		this.addGame(aGame);
		aGame.startGame();
	}
	
	
	
	//Handles disconnected clients
	public void disconnected(ConnectionThread connection ) {
		
		Platform.runLater(()->{
			
			//Send message to server that this client is disconnected 
			serverLog.add("Client " + connection.getConnSocket().getRemoteSocketAddress() + " disconnected");
			//Delete its Thread from HashMap
			this.connThreads.remove(connection.getThreadID());
			// update the Observable list that we use to show connected clients to clients
			this.updateList();
		
			// get a temp Game class for place holder
			Game temp = null;
			
			
			//Loop thru games that we store in the ArrayList<Game> games
			//If the disconnected client is already in game send a warning message to opponent
			//update opponent's game status by calling stopGame method
			for(Game g : games) {
				if(g.player1 == connection) {
					g.player2.getMessage("Your opponent left please choose someone else to play");
					g.player2.stopGame();
					temp = g;
				}
				if(g.player2 == connection) {
					g.player1.getMessage("Your opponent left please choose someone else to play");
					g.player1.stopGame();
					temp = g;
				}
			}
			
		
			// if temp is not null remove the game from ArrayList
			if(temp != null) {
				this.games.remove(temp);		
				
			}
			
			
		});
	}
	
}
