
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ServerApp extends Application{
	public static ArrayList<Thread> threads;
	private HashMap<String, Scene> sceneMap;
	private Scene portScene;
	private Scene serverScene;
	private Stage myStage;
	private Server server;
	private Thread serverThread;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		
		myStage = primaryStage;
		
		//instantiate threads and sceneMap
		threads = new ArrayList<Thread>();
		this.sceneMap = new HashMap<String, Scene>();
		
		//set the Welcome scene to get port number from user 
		this.portScene = setPortScene();
		
		//add welcome scene to hasMap
		sceneMap.put("Welcome", portScene);
		
		
		myStage.setTitle("Server Application");
		myStage.setScene(sceneMap.get("Welcome"));
		myStage.show();
	}
	
	
	
//==================================================================================
	//Sets the Welcome Scene 
	
	private Scene setPortScene() {
		GridPane root = new GridPane();
		root.setPadding(new Insets(20));
		root.setVgap(10);
		root.setHgap(10);
		root.setAlignment(Pos.CENTER);
		
		
		Text portText = new Text("Please Choose a Port Number\nFrom 1 to 65535");
		portText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 15));

		
		TextField portTextField = new TextField();
		
		Label errorMessage = new Label();
		errorMessage.setTextFill(Color.RED); 
		
		
		Button portDoneButton = new Button("Done");
		portDoneButton.setStyle("-fx-font-weight: bold;");
		
		
		//Action Event in Lambda expression for "Done" button 
		// When clicked it creates the server with the given port
		// then changes Welcome scene to ServerScene 
		portDoneButton.setOnAction(event->{
			try{
				
				server = new Server(Integer.parseInt(portTextField.getText()));
				portTextField.clear();

				this.serverScene = setServerScene();
				sceneMap.put("SeverScene", serverScene);
				myStage.setScene(serverScene);
			}
			catch(IllegalArgumentException e){
				errorMessage.setText("Invalid Number\nPlease Try Again!");

			}
			catch(Exception e2) {
				e2.printStackTrace();
			}
		});


		
		
		//add child nodes to root node
		root.add(portText, 0, 0);
		root.add(portTextField, 0, 1);
		root.add(portDoneButton, 0, 2);
		root.add(errorMessage, 0, 3);
		
		//Set preferred size 
		root.setPrefSize(500, 500);
		
		
		return new Scene(root);
	}
	
//==================================================================================
	//Sets ServerScene after receiving port number from user
	//All main actions happen in this Scene
	
	public Scene setServerScene() {
		GridPane root = new GridPane();
		root.setPadding(new Insets(20));
		root.setVgap(10);
		root.setHgap(10);
		
		
		// Column Constraints
		root.getColumnConstraints().add(new ColumnConstraints(300)); // column 0 is 300 wide
		root.getColumnConstraints().add(new ColumnConstraints(50)); // column 1 is 50 wide
		root.getColumnConstraints().add(new ColumnConstraints(300)); // column 2 is 300 wide
		
		//Row Constraints
		root.getRowConstraints().add(new RowConstraints(20)); // row 0 is 20 wide
		root.getRowConstraints().add(new RowConstraints(20)); // row 1 is 20 wide
		root.getRowConstraints().add(new RowConstraints(20)); // row 2 is 20 wide
		root.getRowConstraints().add(new RowConstraints(300)); // row 3 is 300 wide
		
		
		
		
		
		//Button to turn On and turn Off the server
		Button turnOnButton = new Button("Turn On Server");
		Button turnOffButton = new Button("Turn Off Server");
		GridPane.setHalignment(turnOffButton, HPos.RIGHT);
		
		//To show Current Port
		Text currentPortText = new Text("Current Port is: " + server.getPort());
		currentPortText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 10));
		
		// Header Label for Login activities 
		Label loginLabel = new Label("Connected Clients");
		//To show messages
		ListView<String> loginView = new ListView<String>();
		ObservableList<String> loginList = server.getServerLog();
		loginView.setItems(loginList);
		loginView.setMaxSize(300, 300);
		
		
		//To show what each player played
		Label playerMoveLabel = new Label("Players Selections");
		GridPane.setHalignment(playerMoveLabel, HPos.LEFT);
		
		//To show playe Moves 
		ListView<String> playerMoveView = new ListView<String>();
		GridPane.setHalignment(playerMoveView, HPos.LEFT);
		ObservableList<String> playerMoveList = server.getPlayerMoveLog();
		playerMoveView.setItems(playerMoveList);
		playerMoveView.setMaxSize(300, 300);
		
		
		//==========================
		/*
		 * Action Events for buttons 
		 */
		//==========================

		
		//Turns On the server
		turnOnButton.setOnAction(event->{			
			try {
				//Creates a Thread for the server
				serverThread = new Thread(server);
				serverThread.setDaemon(true);
				serverThread.start();
				//add threads to the list
				threads.add(serverThread);

				
				//add message to Login View in the GUI to notify the user 
				// that the server is turned On
				Platform.runLater(()->{
					loginView.getItems().add("Server Turned On!");

				});
			}
			catch(Exception e1) {
				e1.printStackTrace();
			}
		});

		//Turns Off the Server
		turnOffButton.setOnAction(event->{

			try {
				//breaks the while loop in the Run() method 
				server.setRunning(true);
				//closes server Socket and avoids Null Exception
				if(server.serverSocket != null) {
					server.serverSocket.close();
					threads.remove(threads.indexOf(serverThread));				
					turnOffButton.setDisable(true);	
				}

				//Goes back to the Welcome Scene
				Platform.runLater(()->{		
					System.exit(0);
				});
						
			}
			catch(SocketException e) {
			}
			
			catch(Exception e2){
				e2.printStackTrace();
			}		
		});
		

		//Add child nodes to root
		root.add(currentPortText, 0, 0);		
		root.add(turnOnButton, 0, 1);
		root.add(turnOffButton, 0, 1);
		root.add(loginLabel, 0, 2);
		root.add(loginView, 0, 3);
		root.add(playerMoveLabel, 2, 2);
		root.add(playerMoveView, 2, 3);	
		
		root.setPrefSize(700, 700);
		
		return new Scene(root);
	}//End of setServerScene()
	
}//End Off Server App







