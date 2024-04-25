// Rogelio Lozano and Pradyun Shrestha
// CS 342 - Software Design - Prof. McCarthy
// Project 3: Messaging App
// Description: This Class is responsible for Messaging App GUI on the client side
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Pair;

public class GuiClient extends Application{

	String username = "";
	String opponentUsername = "";
	TextField clientUsernameInput = new TextField();
	Label gameStatusUpdates = new Label();
	Button sendButton = new Button("Send");
	Button readyButton = new Button("Ready");
	Button newGameButton = new Button("New Game");
	Button setUsername = new Button("Set Username");;
	Button leaveServer = new Button("Leave");
	Button confirmOpponent = new Button("Confirm Opponent");
	HashMap<String, Scene> sceneMap = new HashMap<String, Scene>();
	VBox setUserBox;

	HBox buttonsBox = new HBox(10, readyButton, sendButton, newGameButton);
	Alert invalidUserAlert = new Alert(AlertType.ERROR);
	Alert gameoverMessage = new Alert(AlertType.INFORMATION);
	Client clientConnection;
	ListView<String> MessagesStream;
	ObservableList<String> observableList = FXCollections.observableArrayList("Computer");
	ComboBox<String> selectReceiver = new ComboBox<>(observableList);
	Battleship battleshipGame = new Battleship(opponentUsername);


	////////////////////////////////////FROM BATTLESHIP.JAVA////////////////////////////////////
//	boolean running = false;
//	Board enemyBoard, playerBoard;
//
//	int shipsToPlace = 5;
//
//	boolean enemyTurn = false;
//
//	Random random = new Random();
//
//	Alert gameoverMessage = new Alert(Alert.AlertType.INFORMATION);
	////////////////////////////////////FROM BATTLESHIP.JAVA////////////////////////////////////


	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(
						()->{

							if (data instanceof GameMessage) {

								GameMessage gameMessage = (GameMessage) data;

								// Opponent sends us an "Attack"
								if (gameMessage.getContent().equals("Attack")) {
									System.out.println("Received " + gameMessage.getContent() + " from " + gameMessage.getSender());
									String result = battleshipGame.playMove(gameMessage, battleshipGame.playerBoard);  // Assuming attacks are always on the player board

									if (result.equals("Hit")){ // Opponent sent us a Missile and it "Hit"
										sendButton.setDisable(true);
										battleshipGame.playerBoard.setCellRed(gameMessage.getX(), gameMessage.getY());

										if (battleshipGame.playerBoard.allShipsSunk()) {
											GameMessage winMessage = new GameMessage(username, opponentUsername, "Win", gameMessage.getX(), gameMessage.getY());
											clientConnection.send(winMessage);
											displayLose();

										} else {
											GameMessage hitMessage = new GameMessage(username, opponentUsername, "Hit", gameMessage.getX(), gameMessage.getY());
											clientConnection.send(hitMessage);
											System.out.println("Sent: " + hitMessage.getContent() + " to " + hitMessage.getRecipient());
										}

									}else{ // Opponent sent a Missile and it was a "Miss", so we send "Miss"
										sendButton.setDisable(false);
										battleshipGame.playerBoard.setCellBlue(gameMessage.getX(), gameMessage.getY());
										GameMessage missMessage = new GameMessage(username,opponentUsername,"Miss", gameMessage.getX(), gameMessage.getY());
										clientConnection.send(missMessage);
										System.out.println("Sent: " + missMessage.getContent() + " to " + missMessage.getRecipient());
									}

									// Opponent sends us "Hit" as feedback from our turn
								} else if (gameMessage.getContent().equals("Hit")) {
									// Handle results of your attacks here
									battleshipGame.enemyBoard.setCellRed(gameMessage.getX(), gameMessage.getY());
									System.out.println("Result of attack at (" + gameMessage.getX() + ", " + gameMessage.getY() + "): HIT");
									sendButton.setDisable(false);

									// Opponent sends us "Miss" as feedback from our turn
								} else if (gameMessage.getContent().equals("Miss")){
									battleshipGame.enemyBoard.setCellBlue(gameMessage.getX(), gameMessage.getY());
									System.out.println("Result of attack at (" + gameMessage.getX() + ", " + gameMessage.getY() + "): MISS");
									sendButton.setDisable(true);

									// Server sends us "Start" letting us know that our opponent is ready
								} else if (gameMessage.getContent().equals("Start")) {
									battleshipGame.startGame();
									gameStatusUpdates.setText("");
									sendButton.setDisable(false);
									readyButton.setDisable(true);
								}else if (gameMessage.getContent().equals("Win")){
									displayWin();
								}

							}else { // responsible for updating list of clients on server
								String currSelected = selectReceiver.getSelectionModel().getSelectedItem();
								MessagesStream.getItems().add(data.toString());
								observableList.clear();
								observableList.addAll(clientConnection.serverClients);
								selectReceiver.getSelectionModel().select(currSelected);
							}
						});
		});

		selectReceiver.getSelectionModel().selectFirst();

		clientConnection.start();

		MessagesStream = new ListView<String>();

		// Turns ListView Cells Blue if they start with client's username
//
//
//
//		//////////////////////////////////////////////////// Event Listeners ////////////////////////////////////////////////////
		setUsername.setOnAction(e->{

			if (usernameAvailable(clientUsernameInput.getText(),clientConnection.serverClients)){
				username = clientUsernameInput.getText();
				System.out.println("Username has been set to: " + username);
				clientConnection.send(new Message(username, "Server", "This is the username")); // Send username right away
				sceneMap.put("opponents",createOpponentSelection());
				primaryStage.setScene(sceneMap.get("opponents"));
			}else{
				invalidUserAlert.setTitle("Invalid Username");
				invalidUserAlert.setContentText("This is an invalid username, please try another.");
				invalidUserAlert.showAndWait();
			}

		});

		confirmOpponent.setOnAction(e -> {
			opponentUsername = selectReceiver.getSelectionModel().getSelectedItem();
			battleshipGame = new Battleship(opponentUsername);



			if (opponentUsername.equals("Computer")){
				sceneMap.put("battleshipComputer",createBattleshipComputer());
				primaryStage.setScene(sceneMap.get("battleshipComputer"));
			}else{
				sceneMap.put("battleshipPlayer",  createBattleshipPlayer());
				primaryStage.setScene(sceneMap.get("battleshipPlayer"));
			}

		});

		newGameButton.setOnAction(e -> {
			battleshipGame.resetGame();
			primaryStage.setScene(sceneMap.get("opponents"));
		});

		// Click leave button to leave the server and close window
		leaveServer.setOnAction(e ->{
			Platform.exit();
			System.exit(0);
		});

		// If you close window we exit from the server
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});
		/////////////////////////////////////////////// Event Listeners ////////////////////////////////////////////////////


		sceneMap.put("initial",  createEnterScene());

		primaryStage.setScene(sceneMap.get("initial"));
		primaryStage.setTitle("BATTLESHIP");
		primaryStage.show();


		
	}


	// Checks if a User picked a valid username. Traverses through current clients on server to prevent duplicates.
	public Boolean usernameAvailable(String username, ArrayList<String> serverList){
		ArrayList<String> allUsers = new ArrayList<>(serverList);
		if (username.isEmpty()){
			return false;
		}else{
			for (int i = 0; i < allUsers.size();i++){
				System.out.println(allUsers.get(i));
				if (allUsers.get(i).equals(username)){
					return false;
				}
			}
		}
		return true;
	}

	// Creates Enter Scene for setting the username
	public Scene createEnterScene() {

		Label introMessage = new Label("Set your username!");
		introMessage.setStyle("-fx-font-size:50px; -fx-text-fill:white; -fx-font-family:Arial;");

		setUserBox = new VBox(10,introMessage, clientUsernameInput, setUsername);
		setUserBox.getStyleClass().add("initial-scene");
		setUserBox.setStyle("-fx-background-color: #303030;" + "-fx-font-family: 'serif';");
		setUserBox.setAlignment(Pos.CENTER);
		clientUsernameInput.setMaxWidth(500);
		clientUsernameInput.setPrefHeight(100);
		clientUsernameInput.setPromptText("Enter Username");
		clientUsernameInput.setStyle("-fx-background-color: #616161; -fx-font-family:arial; -fx-text-fill: white; -fx-font-size: 25px; -fx-pref-height: 50px; -fx-border-color:#33c9ff; -fx-border-width:2px; -fx-margin: 50;");
		setUsername.setStyle("-fx-background-color: white;-fx-text-fill: black; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");

		// Create Border Pane
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(setUserBox);
		BorderPane.setAlignment(setUserBox, Pos.CENTER);

		Scene initialScene = new Scene(borderPane, 800, 800);

		return initialScene;

	}

	public Scene createOpponentSelection(){
//#303030
		BorderPane borderPane = new BorderPane();
		borderPane.setStyle("-fx-background-color:#303030;");

		Label nameDisplay = new Label(" " + username + " ");
		nameDisplay.setStyle("-fx-font-family:arial; -fx-font-size:50; -fx-text-fill:white;");

		Label chooseEnemyText = new Label("Choose your Opponent!");
		chooseEnemyText.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		VBox nameSelectPlayer = new VBox(50,nameDisplay,chooseEnemyText,selectReceiver,confirmOpponent);
		nameSelectPlayer.setAlignment(Pos.CENTER);
		selectReceiver.setStyle("-fx-background-color:black; -fx-text-fill:white; -fx-border-color:#616161; -fx-border-width:2px");
		confirmOpponent.setStyle("-fx-background-color:black; -fx-text-fill:white; -fx-border-color:#616161; -fx-border-width:2px; ");
		borderPane.setCenter(nameSelectPlayer);

//		Scene root = new Scene(borderPane,800,800);

        return new Scene(borderPane,800,800);
	}

	public Scene createBattleshipComputer(){
		Label opponentDisplay = new Label(" VS " + selectReceiver.getSelectionModel().getSelectedItem());
		opponentDisplay.setStyle("-fx-font-family:arial; -fx-font-size:50; -fx-text-fill:white;");

		Label usernameDisplay = new Label(username);
		usernameDisplay.setTextAlignment(TextAlignment.CENTER);
		usernameDisplay.setStyle("-fx-font-family:arial; -fx-font-size:50; -fx-text-fill:white;");

		Label yoursText = new Label("Yours");
		yoursText.setTextAlignment(TextAlignment.CENTER);
		yoursText.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		Label enemyText = new Label("Enemy");
		enemyText.setTextAlignment(TextAlignment.CENTER);
		enemyText.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		Label shipsToPlace = new Label("Place " + battleshipGame.shipsToPlace + " cell ship");
		shipsToPlace.setTextAlignment(TextAlignment.CENTER);
		shipsToPlace.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		leaveServer.setStyle("-fx-background-color:#000; -fx-text-fill:white; -fx-border-color:#616161; -fx-border-width:2px;");

		HBox topContainer = new HBox(leaveServer);
		topContainer.setAlignment(Pos.TOP_RIGHT);

		HBox usernameDisplayBox = new HBox(usernameDisplay,opponentDisplay);
		usernameDisplayBox.setAlignment(Pos.TOP_CENTER);

		gameStatusUpdates.setStyle("-fx-font-family:arial; -fx-font-size:20; -fx-text-fill:white;");

		VBox titleAndCombo = new VBox(50, usernameDisplayBox,topContainer);
		titleAndCombo.setAlignment(Pos.TOP_CENTER);

		HBox buttonsBox = new HBox(10, readyButton, sendButton, newGameButton);
		buttonsBox.setAlignment(Pos.BOTTOM_CENTER);

		sendButton.setDisable(true);
		readyButton.setDisable(true);
		newGameButton.setDisable(true);
		sendButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");
		readyButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");
		newGameButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");

		sendButton.setDisable(true);
		readyButton.setDisable(true);
		gameStatusUpdates.setText("Set up your ships! Left Click - Vertical. Right Click - Horizontal");

		battleshipGame.enemyBoard = new Board(true, event -> {

			if (!battleshipGame.running)
				return;

			Board.Cell cell = (Board.Cell) event.getSource();
			if (!cell.wasShot) {
				battleshipGame.enemyBoard.setTarget(cell.x, cell.y);
				System.out.println("CURRENT GUESS - X: " + cell.x + " Y: " + cell.y);
				Board.Cell targetCell = battleshipGame.enemyBoard.currentTarget;
				System.out.println("Currently pointing missile at " + (targetCell != null ? targetCell.toString() : "no target"));
			}

		});

		battleshipGame.playerBoard = new Board(false, event -> {
			if (battleshipGame.running)
				return;

			Board.Cell cell = (Board.Cell) event.getSource();
			System.out.println(" PLAYER CLICK - X: " + cell.x + " Y: " + cell.y);
			if (battleshipGame.playerBoard.placeShip(new Ship(battleshipGame.shipsToPlace, event.getButton() == MouseButton.PRIMARY), cell.x, cell.y)) {
				--battleshipGame.shipsToPlace;
				shipsToPlace.setText("Place " + battleshipGame.shipsToPlace + " cell ship");
				if (battleshipGame.shipsToPlace == 0) {
					battleshipGame.shipsSet = true;
					readyButton.setDisable(false);
					gameStatusUpdates.setText("Ready up to Start!");
					shipsToPlace.setText("");

				}
			}
		});

		VBox playerVbox = new VBox(20,yoursText,battleshipGame.playerBoard);
		playerVbox.setAlignment(Pos.CENTER);
		VBox enemyVbox = new VBox(20, enemyText, battleshipGame.enemyBoard);
		enemyVbox.setAlignment(Pos.CENTER);

		HBox boardsBox = new HBox(50,playerVbox,enemyVbox);
		boardsBox.setAlignment(Pos.CENTER);

//        sendButton.setStyle("-fx-background-color: black;-fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");
//
		sendButton.setOnAction(e -> {
			System.out.println("Pressing player vs. computer send button");

			Board.Cell cell = battleshipGame.returnTarget(battleshipGame.enemyBoard);

			battleshipGame.enemyTurn = !cell.shoot();

			if (battleshipGame.enemyBoard.ships == 0) {
				displayWin();
			}

			if (battleshipGame.enemyTurn) {
				battleshipGame.enemyMove();
				if (battleshipGame.playerBoard.ships == 0){
					displayLose();
				}
			}
		});

		readyButton.setOnAction(e -> {
			battleshipGame.startGame();
			sendButton.setDisable(false);
			readyButton.setDisable(true);
			gameStatusUpdates.setText("");
		});

		VBox centralVBox = new VBox(50,gameStatusUpdates,boardsBox,shipsToPlace,buttonsBox);

		centralVBox.setAlignment(Pos.CENTER);

		BorderPane root = new BorderPane();
		root.setTop(titleAndCombo);
		root.setCenter(centralVBox);
		root.setStyle("-fx-background-color: #303030; -fx-padding: 20;");

		return new Scene(root, 1000, 800);
	}

	public Scene createBattleshipPlayer(){

		Label opponentDisplay = new Label(" VS " + selectReceiver.getSelectionModel().getSelectedItem());
		opponentDisplay.setStyle("-fx-font-family:arial; -fx-font-size:50; -fx-text-fill:white;");

		Label usernameDisplay = new Label(username);
		usernameDisplay.setTextAlignment(TextAlignment.CENTER);
		usernameDisplay.setStyle("-fx-font-family:arial; -fx-font-size:50; -fx-text-fill:white;");

		leaveServer.setStyle("-fx-background-color:#000; -fx-text-fill:white; -fx-border-color:#616161; -fx-border-width:2px;");

		HBox topContainer = new HBox(leaveServer);
		topContainer.setAlignment(Pos.TOP_RIGHT);

		HBox usernameDisplayBox = new HBox(usernameDisplay,opponentDisplay);
		usernameDisplayBox.setAlignment(Pos.TOP_CENTER);

		Label yoursText = new Label("Yours");
		yoursText.setTextAlignment(TextAlignment.CENTER);
		yoursText.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		Label enemyText = new Label("Enemy");
		enemyText.setTextAlignment(TextAlignment.CENTER);
		enemyText.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		Label shipsToPlace = new Label("Place " + battleshipGame.shipsToPlace + " cell ship");
		shipsToPlace.setTextAlignment(TextAlignment.CENTER);
		shipsToPlace.setStyle("-fx-font-family:arial; -fx-font-size:25; -fx-text-fill:white;");

		gameStatusUpdates.setStyle("-fx-font-family:arial; -fx-font-size:20; -fx-text-fill:white;");

		VBox titleAndCombo = new VBox(50, usernameDisplayBox,topContainer);
		titleAndCombo.setAlignment(Pos.TOP_CENTER);

		HBox buttonsBox = new HBox(10, readyButton, sendButton, newGameButton);
		buttonsBox.setAlignment(Pos.BOTTOM_CENTER);

		sendButton.setDisable(true);
		readyButton.setDisable(true);
		newGameButton.setDisable(true);
		sendButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");
		readyButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");
		newGameButton.setStyle("-fx-background-color: grey; -fx-text-fill: white; -fx-font-size: 16px;-fx-border-color: #616161; -fx-border-width: 2px; -fx-border-radius: 2px;-fx-pref-height: 50px;");
		gameStatusUpdates.setText("Set up your ships! Left Click - Vertical. Right Click - Horizontal");


		battleshipGame.enemyBoard = new Board(true, event -> {

			if (!battleshipGame.running || ((Board.Cell) event.getSource()).wasShot)
				return;

			Board.Cell cell = (Board.Cell) event.getSource();
			cell = cell.target();
			System.out.println("CURRENT GUESS - X: " + cell.x + " Y: " + cell.y);

			Board.Cell targetCell = battleshipGame.returnTarget(battleshipGame.enemyBoard);
			System.out.println("Currently pointing missile at " + (targetCell != null ? targetCell.toString() : "no target"));

		});

		battleshipGame.playerBoard = new Board(false, event -> {
			if (battleshipGame.running)
				return;

			Board.Cell cell = (Board.Cell) event.getSource();
			System.out.println("YOU PLACED SHIP AT - X: " + cell.x + " Y: " + cell.y);
			if (battleshipGame.playerBoard.placeShip(new Ship(battleshipGame.shipsToPlace, event.getButton() == MouseButton.PRIMARY), cell.x, cell.y)) {
				if (--battleshipGame.shipsToPlace == 0) {
					battleshipGame.shipsSet = true;
					readyButton.setDisable(false);
					gameStatusUpdates.setText("Ready up!");

				}
			}
		});

		VBox playerVbox = new VBox(20,yoursText,battleshipGame.playerBoard);
		playerVbox.setAlignment(Pos.CENTER);
		VBox enemyVbox = new VBox(20, enemyText, battleshipGame.enemyBoard);
		enemyVbox.setAlignment(Pos.CENTER);


		HBox boardsBox = new HBox(50, playerVbox, enemyVbox);
		boardsBox.setAlignment(Pos.CENTER);

		sendButton.setOnAction(e -> {

			Board.Cell cell = battleshipGame.returnTarget(battleshipGame.enemyBoard);

			GameMessage attackMessage = new GameMessage(clientConnection.clientUsername, opponentUsername, "Attack", cell.x,cell.y);
			clientConnection.send(attackMessage);

			System.out.println("Enemy has " + battleshipGame.enemyBoard.ships + " ships");
			System.out.println("You have " + battleshipGame.enemyBoard.ships + " ships");

			if (battleshipGame.enemyBoard.ships == 0) {
                displayWin();

            }else if (battleshipGame.playerBoard.ships == 0){
				displayLose();
			}

		});

		readyButton.setOnAction(e -> {
			// send  message to server saying ready -> server looks through currentGames<>
			clientConnection.send(new Pair<String,String>(username,opponentUsername));
			gameStatusUpdates.setText("Waiting for Player 2 to Ready Up...");
		});

		VBox centralVBox = new VBox(50,gameStatusUpdates,boardsBox,buttonsBox);

		centralVBox.setAlignment(Pos.CENTER);

		BorderPane root = new BorderPane();
		root.setTop(titleAndCombo);
		root.setCenter(centralVBox);
		root.setStyle("-fx-background-color: #303030; -fx-padding: 20;");

		return new Scene(root, 1000, 800);
	}

	public void displayLose(){
		System.out.println("YOU LOST");
		gameoverMessage.setTitle("YOU LOST");
		gameoverMessage.setContentText("YOU LOSEEEEE");
		gameoverMessage.showAndWait();
		sendButton.setDisable(true);
		readyButton.setDisable(true);
		newGameButton.setDisable(false);
	}

	public void displayWin(){
		System.out.println("YOU WIN");
		gameoverMessage.setTitle("YOU WIN");
		gameoverMessage.setContentText("YOU WINNNNN");
		gameoverMessage.showAndWait();
		sendButton.setDisable(true);
		readyButton.setDisable(true);
		newGameButton.setDisable(false);
	}

}


