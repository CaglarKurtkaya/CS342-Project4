import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;




public class ClientApp extends Application {
	private ArrayList<Thread> threads;
	private Client client;	
	private HashMap<String, Scene> sceneMap;   //to hold scenes
	private Stage myStage;   // to be able to change scene of the primaryStage
	private Scene welcomeScene; 
	private Scene gameScene;
	
	
	
	
	public static void main(String[] args){
		launch(args);
	}
	

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		this.myStage = primaryStage;
		this.sceneMap = new HashMap<String, Scene>();

		
		// create and set the welcome screen
		welcomeScene = setWelcomeScene();
		
		
		//add it the hashMap
		this.sceneMap.put("Welcome", welcomeScene);
		
		// Instantiate a collection of Thread arrayList
		threads = new ArrayList<Thread>();
		
		
		myStage.setTitle("Client App");
		myStage.setScene(sceneMap.get("Welcome"));
		myStage.show();
			
	}
	
	
	
//==================================================================================
	//Sets the Welcome Screen
	//Here we collect Client Name, IP address and Port Number
	
	public Scene setWelcomeScene() {
		GridPane root = new GridPane();
		root.setPadding(new Insets(20));
		root.setHgap(10);
		root.setVgap(10);
		root.setAlignment(Pos.CENTER);
		
		//Label for NAME, IP AND PORT for Client to Choose
		Label nameLabel = new Label("Name ");
		GridPane.setHalignment(nameLabel, HPos.RIGHT);
		
		Label ipLabel = new Label("IP Adrress ");
		GridPane.setHalignment(ipLabel, HPos.RIGHT);

		Label portLabel = new Label("Port Number");
		GridPane.setHalignment(portLabel, HPos.RIGHT);

		Label errorLabel = new Label();
		
		
		
		//TextField for NAME, IP AND PORT for Client to Choose
		TextField nameField = new TextField();
		TextField ipField = new TextField();
		TextField portField = new TextField();
		
		
		Button enterButton = new Button("Connect");
		
		//Action EVENT for enter Button
		//Create a client and starts its own thread
		enterButton.setOnAction(event->{
			try {
			// Create a client 
			client = new Client(nameField.getText(), ipField.getText(),Integer.parseInt(portField.getText()));
			
			//Start a thread for the client
			Thread clientThread = new Thread(client);
			clientThread.setDaemon(true);
			clientThread.start();
			threads.add(clientThread);
			
			
			//create and set gameScene then set it to our stage
			this.gameScene = setGameScene();
			sceneMap.put("Game", gameScene);
			this.myStage.setScene(sceneMap.get("Game"));
		
			
			} catch (ConnectException  e1) {
				errorLabel.setTextFill(Color.RED);
				errorLabel.setText("Please Check IP Adrress");
				
				
			} catch (NumberFormatException | IOException e) {	
				errorLabel.setTextFill(Color.RED);
				errorLabel.setText("Please Check Port Number");
			}
					
		});
		
		
		
		
		// add nodes to root 
		root.add(nameLabel, 0, 0);
		root.add(nameField, 1, 0);
		root.add(ipLabel, 0, 1);
		root.add(ipField, 1, 1);
		root.add(portLabel, 0, 2);
		root.add(portField, 1, 2);
		root.add(enterButton, 1, 3);
		root.add(errorLabel, 1, 4);
		
		
		root.setPrefSize(400, 400);
		return new Scene(root);
		
	}
	
//==================================================================================
	//Sets Game Scene 
	//All game actions happens here 
	
	
	public Scene setGameScene() {
		
		GridPane root = new GridPane();
		root.setPadding(new Insets(20));
		root.setHgap(10);
		root.setVgap(10);
		
		
		root.getColumnConstraints().add(new ColumnConstraints(130)); // column 0 is 130 wide
		root.getColumnConstraints().add(new ColumnConstraints(540)); // column 1 is 540 wide
		root.getColumnConstraints().add(new ColumnConstraints(130)); // column 2 is 130 wide

		root.getRowConstraints().add(new RowConstraints(225)); // row 0 is 225 wide
		root.getRowConstraints().add(new RowConstraints(150)); // row 1 is 150 wide
		root.getRowConstraints().add(new RowConstraints(225)); // row 2 is 225 wide

		
		ListView<String> messagesFormServerView = new ListView<String>();
		messagesFormServerView.setMaxSize(250, 195);
		GridPane.setMargin(messagesFormServerView, new Insets(20));

		ObservableList<String> messagesList = client.getClientLog();
		messagesFormServerView.setItems(messagesList);

		ObservableList<String> allClients = FXCollections.observableArrayList(
			"Client 1",
			"Client 2",
			"Client 3"
		);

		ComboBox comboBox = new ComboBox(allClients);

		// HBOX to put clickable button images
		HBox hbox = new HBox();	
		hbox.setPadding(new Insets(15,10,15,10));
		hbox.setSpacing(10);
		
		//HBOX to add playAgain and Quit buttons
		HBox hbox2 = new HBox();
		hbox2.setPadding(new Insets(35,10,15,10));
		hbox2.setSpacing(30);

		HBox hbox3 = new HBox();
		hbox3.setPadding(new Insets(15,10,15,10));
		hbox3.setSpacing(20);
		
		
		Label youLabel = new Label();
		youLabel.setText("YOU : " + client.getClientScore());
		youLabel.setTextFill(Color.BLACK);
		
		
		
		// BUTTONS to represent RPSLS images
		Button rock = new Button();
		Button paper = new Button();
		Button scissors = new Button();
		Button lizard = new Button();
		Button spock = new Button();
		
		//BUTTONS for play again and quit
		Button playAgain = new Button("Play Again");
		Button quit = new Button("Quit");

		Button challengeButton = new Button("Challenge");
		Button declineButton = new Button("Decline");
		
		//Set preferred, max and min size to lock the size of the buttons
		rock.setPrefSize(100, 150);
		rock.setMaxSize(100, 150);
		rock.setMinSize(100, 150);
		
		paper.setPrefSize(100, 150);
		paper.setMaxSize(100, 150);
		paper.setMinSize(100, 150);
		
		scissors.setPrefSize(100, 150);
		scissors.setMaxSize(100, 150);
		scissors.setMinSize(100, 150);
		
		lizard.setPrefSize(100, 150);
		lizard.setMaxSize(100, 150);
		lizard.setMinSize(100, 150);
		
		spock.setPrefSize(100, 150);
		spock.setMaxSize(100, 150);
		spock.setMinSize(100, 150);

		challengeButton.setPrefSize(100, 25);
		challengeButton.setMaxSize(100, 25);
		challengeButton.setMinSize(100,25);

		declineButton.setPrefSize(100, 25);
		declineButton.setMaxSize(100, 25);
		declineButton.setMinSize(100,25);
		
		//IMAGES for RPSLS
		Image rockImage = new Image("rock1.png");
		Image paperImage = new Image("paper1.png");
		Image scissorsImage = new Image("scissors1.png");
		Image lizardImage = new Image("lizard1.png");
		Image spockImage = new Image("spock1.png");
		
		//IMAGESVIEWS to put it into buttons
		ImageView rockImageView = new ImageView(rockImage);
		rockImageView.fitWidthProperty().bind(rock.widthProperty()); 
		rockImageView.fitHeightProperty().bind(rock.heightProperty());
		
		ImageView paperImageView = new ImageView(paperImage);
		paperImageView.fitWidthProperty().bind(paper.widthProperty()); 
		paperImageView.fitHeightProperty().bind(paper.heightProperty());
		
		ImageView scissorsImageView = new ImageView(scissorsImage);
		scissorsImageView.fitWidthProperty().bind(scissors.widthProperty()); 
		scissorsImageView.fitHeightProperty().bind(scissors.heightProperty());
		
		ImageView lizardImageView = new ImageView(lizardImage);
		lizardImageView.fitWidthProperty().bind(lizard.widthProperty()); 
		lizardImageView.fitHeightProperty().bind(lizard.heightProperty());
		
		ImageView spockImageView = new ImageView(spockImage);
		spockImageView.fitWidthProperty().bind(spock.widthProperty()); 
		spockImageView.fitHeightProperty().bind(spock.heightProperty());
		
		
		// Put imageViews into Buttons
		rock.setGraphic(rockImageView);
		paper.setGraphic(paperImageView);
		scissors.setGraphic(scissorsImageView);
		lizard.setGraphic(lizardImageView);
		spock.setGraphic(spockImageView);
		
		
		// add all buttons to HBOX
        hbox.getChildren().addAll(rock, paper, scissors, lizard, spock);
        hbox2.getChildren().addAll(playAgain, quit);
        hbox3.getChildren().addAll(challengeButton, declineButton);
		
        
        //====================================
		/*
		 * ACTION EVENTS IN LAMBDA EXPRESSIONS
		 */
        //====================================


        rock.setOnAction(event->{
        	client.sendMessage("rock");
        });
        
        paper.setOnAction(event->{
        	client.sendMessage("paper");
        });
        
        scissors.setOnAction(event->{
        	client.sendMessage("scissors");
        });
        
        lizard.setOnAction(event->{
        	client.sendMessage("lizard");
        });
        
        spock.setOnAction(event->{
        	client.sendMessage("spock");
        });
   
        playAgain.setOnAction(event->{
        	client.sendMessage("playagain");
        });
        
        quit.setOnAction(event->{
        	System.exit(0);
        });

        challengeButton.setOnAction(e -> {

		});

		declineButton.setOnAction(e -> {

		});

        //END OF ACTION EVENTS
        //====================================

        
        
        
        
		root.setPrefSize(900, 600);	
		
		//add child nodes to root 
		root.add(messagesFormServerView, 1, 0);
		root.add(comboBox, 2, 0);
		root.add(hbox, 1, 2);
		root.add(hbox2, 1, 3);
		root.add(hbox3, 2, 1);
		
		
			
		return new Scene(root);	
	}
	
	
	
//==================================================================================

	
	
	@Override
	public void stop() throws Exception {
		
		super.stop();
		for (Thread thread: threads){
			thread.interrupt();
		}
	}

}
