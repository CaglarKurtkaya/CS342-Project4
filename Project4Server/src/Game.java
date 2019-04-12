public class Game {

	ConnectionThread player1;
	ConnectionThread player2;
	Server AdminServer;
	
	private int roundCount = 0;
	private int roundLimit = 1;
	private Boolean gameStarted = false;
	private Boolean gamePaused = false;	
	
	public Game() {
		
	}
	public Game(ConnectionThread p1, ConnectionThread p2, Server a) {
		player1 = p1;	
		player2 = p2;
		AdminServer = a;
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
	
	
	public void setGameStarted(Boolean b) {
		this.gameStarted = b;
	}
	
	//it will be used when players wants to play again
	public void setGamePaused(Boolean b) {
		this.gamePaused = b;
	}
	
	
	public void increaseRoundCount() {
		this.roundCount += 1;
	}
	
	public void resetRoundCount() {
		this.roundCount = 0;
	}
	
	public synchronized void startGame() {
		player1.startGame();
		player2.startGame();
	}
	
	public synchronized void stopGame() {
		player1.stopGame();
		player2.stopGame();
		
	}
	
	public void pauseGame() {
		this.setGamePaused(true);
		player1.pauseGame();
		player2.pauseGame();
		player1.getMessage("Game finished. Use play again button to start again.");
		player2.getMessage("Game finished. Use play again button to start again.");
	}
	
	public void resumeGame() {
	
		//both player clicked play again.
		if ( (player1.getGamePaused() == false) && (player2.getGamePaused() == false) ) {
			//resume game.
			this.setGamePaused(false);
			this.resetRoundCount();
			player1.getMessage("Game has restarted, please make your move.");
			player2.getMessage("Game has restarted, please make your move.");
		}
	}
	
	public void checkMoves() {
			
			if (this.getGamePaused()) {
				player1.getMessage("Game paused.");
				player2.getMessage("Game paused.");
				return;
			}
			

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
				
				
				if (this.getRoundCount() == this.roundLimit) {
					this.stopGame();
					AdminServer.removeGame(this);
				}
				
			}
			else {
				player1.getMessage("Waiting both players to make move.");
				player2.getMessage("Waiting both players to make move.");
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

}
