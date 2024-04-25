// Rogelio Lozano, Pradyun Shrestha, Zakareah Hafeez
// CS 342 - Software Design - Prof. McCarthy
// Project 4: Battleship

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

public class Battleship {
    public String opponentUser;
    public boolean running = false;
    public Board enemyBoard, playerBoard;
    public int shipsToPlace = 5;
    public boolean shipsSet = false;
    public boolean enemyTurn = false;
    private ArrayList<Pair<Integer,Integer>> potentialCells = new ArrayList<>();
    private Random random = new Random();
    public Alert gameoverMessage = new Alert(Alert.AlertType.INFORMATION);
    public boolean isComputer = false;

    // We construct a Battleship object with the opponents name
    public Battleship( String opponent) {

        this.opponentUser = opponent;
        if (opponentUser.equals("Computer")){
            isComputer = true;
        }else{
            isComputer = false;
        }

    }

    // Returns the current selected cell on the board
    public Board.Cell returnTarget(Board board){
        if (board.currentTarget == null) {
            System.out.println("No target is currently selected.");
            return null;
        }
        return board.currentTarget;
    }

    // Main method for the computer/AI to make a move
    public void enemyMove() {
        if (potentialCells.isEmpty()) {
            randomGuess();
        } else {
            targetSurroundingCells();
        }

        if (playerBoard.ships == 0) {
            System.out.println("YOU LOSE");
        }
    }

    // Makes a random guess on the board for the computer
    private void randomGuess() {
        System.out.println("Starting random guess...");
        while (enemyTurn) { // while it is the enemy's turn (they get another turn if "Hit"), make random guesses
            int x = random.nextInt(10);
            int y = random.nextInt(10);
            Board.Cell cell = playerBoard.getCell(x, y);
            if (cell.wasShot) {
                System.out.println("Already shot at X: " + x + " Y: " + y);
                continue;
            }
            System.out.println("Attempting shot at X: " + x + " Y: " + y);
            enemyTurn = cell.shoot();
            if (enemyTurn) {
                System.out.println("Hit at X: " + x + " Y: " + y);
                generatePotentialSpot(cell.x,cell.y);  // Store the hit for targeting mode
                targetSurroundingCells();
            } else {
                System.out.println("Miss at X: " + x + " Y: " + y);
            }
        }
    }

    // Generates potential hit points, storing the points in array potentialCells
    private void generatePotentialSpot(Integer x, Integer y){
        // generate 4 spots, below,top,left,right
        ArrayList<Pair<Integer,Integer>> newPoints = new ArrayList<>();
        Pair<Integer,Integer> below = new Pair<Integer,Integer>(x,y+1);
        newPoints.add(below);
        Pair<Integer,Integer> top = new Pair<Integer,Integer>(x,y-1);
        newPoints.add(top);
        Pair<Integer,Integer> left = new Pair<Integer,Integer>(x-1,y);
        newPoints.add(left);
        Pair<Integer,Integer> right = new Pair<Integer,Integer>(x+1,y);
        newPoints.add(right);

        //test if those are valid spots on the board
        newPoints.removeIf(pair -> (pair.getKey() < 0 || pair.getKey() > 9 || pair.getValue() < 0 || pair.getValue() > 9));

        // Adds potential points to arraylist of spots to visit
        newPoints.forEach(pair ->{
            System.out.println("Potential point -> X: " + pair.getKey() + " Y: " + pair.getKey());
            potentialCells.add(pair);
        });
    }

    // Uses the list of potential hits and shoots at one or many of those
    private void targetSurroundingCells() {

        while (enemyTurn && !potentialCells.isEmpty()) {
            Pair<Integer, Integer> cellCoords = potentialCells.remove(0);  // Get and remove the first element in the list
            int x = cellCoords.getKey();
            int y = cellCoords.getValue();
            Board.Cell cell = playerBoard.getCell(x, y);
            if (!cell.wasShot) {
                enemyTurn = cell.shoot();
                if (enemyTurn) {
                    System.out.println("Hit at X: " + x + " Y: " + y);
                    generatePotentialSpot(x, y);  // Generate new potential spots based on this hit
                    targetSurroundingCells();
                    break;  // Stop further shots if a hit occurs
                } else {
                    System.out.println("Miss at X: " + x + " Y: " + y);
                }
            }
        }
    }

    // For playing against computer
    public void startGame() {
        // place enemy ships
        int type = 5;

        while (type > 0) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            if (enemyBoard.placeShip(new Ship(type, Math.random() < 0.5), x, y)) {
                type--;
            }
        }

        running = true;

    }

    // This should be helpful for the sendButton,
    public String playMove(GameMessage gameMessage, Board board) {
        int xCoord = gameMessage.getX();
        int yCoord = gameMessage.getY();

        Board.Cell cell = board.getCell(xCoord, yCoord);
        if (cell != null) {
            boolean result = cell.shoot(); // shoot returns true if it's a hit, false otherwise
            if (result) {
                System.out.println("Move played at X: " + xCoord + " Y: " + yCoord + ": HIT");
                return "Hit";
            } else {
                System.out.println("Move played at X: " + xCoord + " Y: " + yCoord + ": MISS");
                return "Miss";
            }
        }
        return "Miss"; // Assume miss if no cell was found or other error occurred
    }

    // Restarts the values of Battleship object to default, so that we can start another game
    public void resetGame() {
        // Reset game flags and counters
        running = false;
        shipsToPlace = 5;
        enemyTurn = false;
        shipsSet = false;
    }

}
