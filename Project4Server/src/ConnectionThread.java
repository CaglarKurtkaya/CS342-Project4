import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javafx.application.Platform;



public class ConnectionThread implements Runnable {

	private Socket connSocket;
	private Server AdminServer;
	private PrintWriter out;
	private BufferedReader in;
	private String clientName;
	
	private volatile boolean running = false;
	
	private Boolean gameStarted = false;
	private Boolean gamePaused = false;
	private String move = "no";
	private Boolean canPlay = true;
	private Integer won = 0;
	
	
	
	
	//Constructor
	public ConnectionThread(Server givenServer, Socket givenSocket) {
		
		this.AdminServer = givenServer;
		this.connSocket = givenSocket;
		try{
			out = new PrintWriter(connSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
		}
	
		catch (IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		
	}
//===========================================================================	
	
	// Getters
	public Socket getConnSocket() {
		return this.connSocket;
	}
	
	public  Boolean getRunning() {
		return this.running;
	}
	
	public String getMove() {
		return this.move;
	}
	
	public Boolean getCanPlay() {
		return this.canPlay;
	}
	
	public int getWon() {
		return this.won;
	}
	
	public Boolean getGameStarted() {
		return this.gameStarted;
	}
	
	public Boolean getGamePaused() {
		return this.gamePaused;
	}
	
	public String getClientName() throws IOException{
		//it is the first input coming from client(check client constructor)
		return in.readLine();
	}
	
	//This one returns client name that we got form above function
	//without IOException
	public String getSafeClientName() {
		return this.clientName;
	}
	
//===========================================================================	
	
	//Setters
	public void setConnSocket(Socket s) {
		this.connSocket = s;
	}
	
	public void setRunning(boolean r) {
		this.running = r;
	}
	public void setMove(String m) {
		this.move = m;
	}
	public void setCanPlay (Boolean c) {
		this.canPlay = c;
	}
	public void increaseWon() {
		this.won += 1;
	}
	public void resetWon() {
		this.won = 0;
	}
	public void setGameStarted(Boolean b) {
		this.gameStarted = b;
	}
	public void setGamePaused(Boolean b) {
		this.gamePaused = b;
	}
	
//===========================================================================	


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {

			this.clientName = getClientName();
			this.getMessage("Hello " + clientName + "\nWelcome to RPSLS Game Network");
			
			while(!running) {
				
				//read incoming messages an stored into String
				final String messages  = in.readLine();
				Platform.runLater(()-> {

					if (this.getGameStarted()) {

						
						//If player wants to play again
						if (messages.equals("playagain")) {
							if(AdminServer.clients.size() == 2) {
								this.resumeGame();
							}
							else {
								AdminServer.sendMessageAll("CANNOT PLAY AGAIN!! Waiting for second client!");
							}
							
						}
						else {
							
							if (AdminServer.getGamePaused()) {
								this.getMessage("Game has finished, please wait.");
							}
							else {
								
								if (this.getCanPlay()) {
									this.setCanPlay(false);
									this.makeMove(messages);
									AdminServer.checkMoves();
									AdminServer.setPlayerMoveLog(this.clientName + " played " + messages);
								}
								else {
									this.getMessage("You already made your move, please wait your opponent to make their move.");
								}
							}

						}
					}
					else {
						this.getMessage("Please wait game to be started.");
					}
				});
			}
		} 
		catch (SocketException e1) {
			AdminServer.disconnected(this);

		}
		catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		this.closeConnThread();

	}

//===========================================================================	
	// Thread methods
	
	public void closeConnThread() {
			this.setRunning(true);
			try {
				this.in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	}
	
	
	public void sendMessage(String input) {
		out.println(input);
	}
	
	//client connection thread receives message with this function 
	public void getMessage(String message) {
		// send client Thread(this) and message to server
		//then server sends the message to desired client thread
		AdminServer.sendToSpecificThread(this, message);
	}
	
//===========================================================================	
	//Game methods
	
	public void startGame() {
		this.getMessage("Game has started.");
		this.setGameStarted(true);
	}
	
	public void stopGame() {
		this.getMessage("Game has stopped.");
		this.setGameStarted(false);
	}
	public void pauseGame() {
		this.setGamePaused(true);
	}
	public void resumeGame() {
		this.setGamePaused(false);
		this.clearGame();
		AdminServer.sendMessageAll(this.getSafeClientName() + " wants to play again.");
		AdminServer.resumeGame();
	}
	
	// Receives the message and sends that to client
	// messages are predefined("rock","paper","scissors", "lizard","spock")  
	// it comes when they clicked the image
	//Shows them what they played 
	// and sets their move for the game
	public void makeMove(String move) {
		this.setMove(move);
		this.getMessage("You played " + move);
	}
	
	public void makeWinner() {
		this.increaseWon();
		this.getMessage("You won the round.");
		this.clearGame();
	}
	
	public void makeLoser() {
		this.getMessage("You lost the round.");
		this.clearGame();
	}
	
	public void makeTie() {
		this.getMessage("It's a tie!");
		this.clearGame();
	}
	
	public void printScore(ConnectionThread otherPlayer) {
		this.getMessage("You: " + this.getWon() + " / " + otherPlayer.getSafeClientName() + ": " + otherPlayer.getWon());
	}
	
	public void clearGame() {
		this.setMove("no");
		this.setCanPlay(true);
	}

}
