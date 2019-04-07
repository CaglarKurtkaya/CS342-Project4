
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



public class Server implements Runnable {
	
	private int port;	
	private ArrayList<ConnectionThread> connThreads;
	private ObservableList<String> serverLog;    // used for adding messages in ListView(in ServerApp)
	private ObservableList<String> playerMoveLog;	
	private int roundCount = 0;
	private int roundLimit = 3;
	private Boolean gameStarted = false;
	private Boolean gamePaused = false;	
	private volatile Boolean running = false;
	
	//Following members accessible only by classes in the same package.
	ArrayList<Socket> clients;         
	ServerSocket serverSocket;
	ObservableList<String> clientNames;
	Thread clientThread;
	Socket socket;
	ConnectionThread cnnThd;


	
	//Constructor
	public Server(int givenPort) {
		if(givenPort < 1 || givenPort > 65535) {
            throw new IllegalArgumentException();
		}
		port = givenPort;
		clients = new ArrayList<Socket>();
		connThreads = new ArrayList<ConnectionThread>();	
		serverLog = FXCollections.observableArrayList();	
		playerMoveLog = FXCollections.observableArrayList();
		clientNames = FXCollections.observableArrayList();		
	}
	
//========================================================================================
	//Getters
	
	public int getPort() {
		return this.port;
	}
	
	public ObservableList<String> getClientNames() {
		return clientNames;
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
	
	public Boolean getGameStarted() {
		return this.gameStarted;
	}
	
	public int getRoundCount() {
		return this.roundCount;
	}
	public Boolean getGamePaused() {
		return this.gamePaused;
	}
	
	public ArrayList<ConnectionThread> getConnThreads (){
		return this.connThreads;
	}

	
//========================================================================================
	//Setters
	
	public void setClientNames(String input) {
		this.clientNames.add(input);
	}

	public void setPlayerMoveLog(String input) {
		this.playerMoveLog.add(input);
	}

	public void setGameStarted(Boolean b) {
		this.gameStarted = b;
	}
	
	//it will be used when players wants to play again
	public void setGamePaused(Boolean b) {
		this.gamePaused = b;
	}
	
	
	public void setServerLog(String input) {
		this.serverLog.add(input);
	}
	
	public void setRunning(boolean b) {
		this.running = b;
	}
	
//========================================================================================
	//methods for the game 
	
	public void resetScores() {
		for (ConnectionThread t : this.connThreads) {
			t.resetWon();
		}
	}
	
	public void increaseRoundCount() {
		this.roundCount += 1;
	}
	
	public void resetRoundCount() {
		this.roundCount = 0;
	}
	
	
	public synchronized void startGame() {
		for (ConnectionThread t : this.connThreads) {
			t.startGame();
		}
	}
	
	public synchronized void stopGame() {
		for (ConnectionThread t : this.connThreads) {
			t.stopGame();
		}
	}
	
	public void pauseGame() {
		this.setGamePaused(true);
		for (ConnectionThread t : this.connThreads) {
			t.pauseGame();
		}
		this.sendMessageAll("Game finished. Use play again button to start again.");
	}
	
	public void resumeGame() {
		ConnectionThread player1 = this.connThreads.get(0);
		ConnectionThread player2 = this.connThreads.get(1);
		
		//both player clicked play again.
		if ( (player1.getGamePaused() == false) && (player2.getGamePaused() == false) ) {
			//resume game.
			this.setGamePaused(false);
			this.resetScores();
			this.resetRoundCount();
			this.sendMessageAll("Game has restarted, please make your move.");
		}
	}
	
	public void checkMoves() {
		
		if (this.getGamePaused()) {
			this.sendMessageAll("Game paused");
			return;
		}
		
		ConnectionThread player1 = this.connThreads.get(0);
		ConnectionThread player2 = this.connThreads.get(1);
		
		//check if both player made the move
		if ( (player1.getMove() != "no") && (player2.getMove() != "no") ) {
			
			this.increaseRoundCount();
			
			player1.getMessage("Your opponent played " + player2.getMove());
			player2.getMessage("Your opponent player " + player1.getMove());
			
			if(player1.getMove().equals(player2.getMove())) {
				player1.makeTie();
				player2.makeTie();
				
			}
			else {
				
				if ( this.checkWinner( player1.getMove(), player2.getMove() ) )  {
					//player1 wins
					player1.makeWinner();
					player2.makeLoser();
					
				}
				else {
					//player2  wins
					player2.makeWinner();
					player1.makeLoser();
				}
				
			}
			
			player1.printScore(player2);
			player2.printScore(player1);
			
			if (this.getRoundCount() == this.roundLimit) {
				this.pauseGame();
			}
			
		}
		else {
			this.sendMessageAll("Waiting both players to make move");
		}
		
	}
	
	//returns true if first arg. wins
	public boolean checkWinner(String play1, String play2) {
		
		if(play1.equals("rock")) {
			
			//rock wins against scissors and lizard
			if ( (play2.equals("scissors")) || ((play2.equals("lizard"))) ) {
				return true;
			}
			//loses to others 
			return false;
			
		}
		
		if(play1.equals("paper")) {
			//paper wins against rock and spock
			if ( (play2.equals("rock")) || ((play2.equals("spock"))) ) {
				return true;
			}
			//loses to others 
			return false;
			
		}
		//spock wins against rock and scissors 
		if(play1.equals("spock")) {
		
			if ( (play2.equals("rock")) || ((play2.equals("scissors"))) ) {
				return true;
			}
			//loses to others 
			return false;
		}
		//scissors wins against paper and lizard 
		if(play1.equals("scissors")) {
			if ( (play2.equals("paper")) || ((play2.equals("lizard"))) ) {
				return true;
			}
			//loses to others 
			return false;
			
		}
		//lizard wins against paper and spock
		if(play1.equals("lizard")) {
			if ( (play2.equals("paper")) || ((play2.equals("spock"))) ) {
				return true;
			}
			//loses to others 
			return false;
			
		}

		return true;
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
					clients.add(socket);

					Platform.runLater(()->{
						serverLog.add("Client " + socket.getRemoteSocketAddress() + " connected");
					});

					cnnThd = new ConnectionThread(this, socket);
					clientThread = new Thread(cnnThd);
					this.connThreads.add(cnnThd);
					clientThread.setDaemon(true);
					clientThread.start();
					ServerApp.threads.add(clientThread);

					if(clients.size() == 2) {
						Platform.runLater(()->{
							serverLog.add("2 Clients connected");
						});
						this.setRunning(true);


						try {
							Thread.sleep(1000);
							this.startGame(); 
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				} else {
					//TODO send max connection error to client.

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
	
	public void sendMessageAll(String input) {
		for(ConnectionThread it : connThreads) {
			it.sendMessage(input);
		}
	}
	
	public void sendToSpecificThread(ConnectionThread t, String input) {
		  	
		for(ConnectionThread it : connThreads) {
			if(it == t) {
			it.sendMessage(input);
			}
		}	
	}
	
	
	public void disconnected(ConnectionThread connection ) {
		
		Platform.runLater(()->{
			serverLog.add("Client " + connection.getConnSocket().getRemoteSocketAddress() + " disconnected");
			this.clients.remove(this.connThreads.indexOf(connection));
			this.connThreads.remove(this.connThreads.indexOf(connection));
			this.sendMessageAll("Your oponent left! Waiting for new clients");
			
			if (this.clients.size() < 2) {
				this.setRunning(false);
			}
			else {
				this.setRunning(true);
			}	
			
		});
	}
	
}
