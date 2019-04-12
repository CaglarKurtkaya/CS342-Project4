import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Platform;



public class ConnectionThread implements Runnable {

	private Socket connSocket;
	private Server AdminServer;
	private ObjectOutputStream   out;
	private ObjectInputStream in;
	private String clientName;
	
	private volatile boolean running = false;
	
	private Boolean gameStarted = false;
	private Boolean gamePaused = false;
	private String move = "no";
	private Boolean canPlay = true;

	
	private String threadID;
	
	private Game game;
	

	
	
	
	
	
	public ConnectionThread() {
		
	}
	
	//Constructor
	public ConnectionThread(Server givenServer, Socket givenSocket) {
		
		this.AdminServer = givenServer;
		this.connSocket = givenSocket;
		try{
			out = new ObjectOutputStream (connSocket.getOutputStream());
			in = new ObjectInputStream(connSocket.getInputStream());
		}
	
		catch (IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		game = new Game();
		
	}
//===========================================================================	
	
	// Getters
	public String getThreadID() {
		return this.threadID;
	}
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
	

	public Boolean getGameStarted() {
		return this.gameStarted;
	}
	
	public Boolean getGamePaused() {
		return this.gamePaused;
	}
	
	public String getClientName() throws IOException{
		//it is the first input coming from client(check client constructor)
		String name = null;
		try {
			name =  (String) in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
	}
	
	//This one returns client name that we got form above function
	//without IOException
	public String getSafeClientName() {
		return this.clientName;
	}
	
//===========================================================================	
	
	//Setters
	public void setThreadID (String s) {
		this.threadID = s;
	}
	
	public void setConnSocket(Socket s) {
		this.connSocket = s;
	}
	
	public void setRunning(boolean r) {
		this.running = r;
	}
	public void setMove(Serializable messages) {
		this.move = (String) messages;
	}
	public void setCanPlay (Boolean c) {
		this.canPlay = c;
	}

	public void setGameStarted(Boolean b) {
		this.gameStarted = b;
	}
	public void setGamePaused(Boolean b) {
		this.gamePaused = b;
	}
	
	public void setGame(Game g) {
		this.game = g;
	}
//===========================================================================	


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.clientName = getClientName();
			//calls updateList() in Server Class
			AdminServer.updateMe();
			this.getMessage("Hello " + clientName + "\nWelcome to RPSLS Game Network");
			
			while(!running) {
				
				//read incoming messages an stored into String	
				Serializable messages = (Serializable) in.readObject();
				
				Platform.runLater(()-> {

					
					// here we split the message into to strings to get name
					String msgStr = messages.toString();
					if (msgStr.contains("challenge:")) {
						String[] exploded = msgStr.split("challenge:");
						//System.out.println(exploded[1]);
						this.handleChallenge(exploded[1]);
					}
					
					else if (msgStr.contains("accept:")) {
						String[] exploded = msgStr.split("accept:");
						//System.out.println(exploded[1]);
						this.handleAccept(exploded[1]);
					}
					else {
						if (this.getGameStarted()) {
								
								if (game.getGamePaused()) {
									this.getMessage("Game has finished, please wait.");
								}
								else {
									
									if (this.getCanPlay()) {
										this.setCanPlay(false);
										this.makeMove(messages);
										game.checkMoves();
										AdminServer.setPlayerMoveLog(this.clientName + " played " + messages);
									}
									else {
										this.getMessage("You already made your move, please wait your opponent to make their move.");
									}
								}

							
						}
						else {
							this.getMessage("Please wait game to be started.");
						}
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	
	public void sendMessage(Serializable data) throws Exception{
		out.writeObject(data);
		out.flush();
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
		this.getMessage("Game has started. Make your move");
		this.setGameStarted(true);
	}
	
	public void stopGame() {
		this.getMessage("Game has finished. Challenge someone else to play again");
		this.setGameStarted(false);
		this.clearGame();
		
	}
	public void pauseGame() {
		this.setGamePaused(true);
	}
	public void resumeGame() {
		this.setGamePaused(false);
		this.clearGame();
		AdminServer.sendMessageAll(this.getSafeClientName() + " wants to play again.");
		//AdminServer.resumeGame();
		game.resumeGame();
	}
	
	// Receives the message and sends that to client
	// messages are predefined("rock","paper","scissors", "lizard","spock")  
	// it comes when they clicked the image
	//Shows them what they played 
	// and sets their move for the game
	public void makeMove(Serializable messages) {
		this.setMove(messages);
		this.getMessage("You played " + messages);
	}
	
	public void makeWinner() {
		this.getMessage("You won the Game!!.");
		this.clearGame();
	}
	
	public void makeLoser() {
		this.getMessage("You lost the Game!!.");
		this.clearGame();
	}
	
	public void makeTie() {
		this.getMessage("It's a tie!");
		this.clearGame();
	}
	
	
	
	public void clearGame() {
		this.setMove("no");
		this.setCanPlay(true);
	}
	
	
	public void handleChallenge(String name) {
		//
		System.out.println(name);
		if (name.equals(this.getSafeClientName())) {
			//System.out.println("You cant challenge to yourself.");
			this.getMessage("You can't challenge to yourself.");
		}
		else {
			AdminServer.handleChallange(this.getSafeClientName(), name, false);
		}
	}
	
	
	public void askChallange (ConnectionThread t) {
		this.getMessage(t.getSafeClientName() + " is challanging you.");
		//this is going to be used to identify who challenged 
		this.getMessage("challenge:" + t.getSafeClientName());
	}
	
	public void handleAccept(String name) {
		if (name.equals(this.getSafeClientName())) {
			this.getMessage("You can't accept to yourself.");
		}
		else {
			AdminServer.handleChallange(this.getSafeClientName(), name, true);
		}
	}

}
