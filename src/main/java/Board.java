
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Board extends Parent {
    private VBox rows = new VBox();
    private boolean enemy = false;
    public int ships = 5;

    private Cell[][] cells = new Cell[10][10];
    public Cell currentTarget = null;

    public Board(boolean enemy, EventHandler<? super MouseEvent> handler) {
        this.enemy = enemy;
        for (int y = 0; y < cells.length; y++) {
            HBox row = new HBox();
            for (int x = 0; x < cells[y].length; x++) {
                cells[y][x] = new Cell(x, y, this);
                cells[y][x].setOnMouseClicked(handler);
                row.getChildren().add(cells[y][x]);
            }
            rows.getChildren().add(row);
        }
        getChildren().add(rows);
    }


    public Cell getCell(int x, int y) {
        if (x >= 0 && x < 10 && y >= 0 && y < 10) {
            return cells[y][x];
        }
        return null;
    }

    public void setTarget(int x, int y) {
        if (enemy) { // Ensure we only target cells on enemy boards
            Cell cell = getCell(x, y);
            if (cell != null && !cell.wasShot) {
                if (currentTarget != cell) {
                    cell.target();
                }
            }
        }
    }

    private Cell createCell(int x, int y, EventHandler<? super MouseEvent> handler) {
        Cell cell = new Cell(x, y, this);
        cell.setOnMouseClicked(handler);
        return cell;
    }

    public void forEachCell(Consumer<Cell> action) {
        for (Node row : rows.getChildren()) {
            for (Node cellNode : ((HBox) row).getChildren()) {
                Board.Cell cell = (Board.Cell) cellNode;
                action.accept(cell);
            }
        }
    }

//    public void confirmAttacks() {
//        for (Node row : rows.getChildren()) {
//            for (Node cellNode : ((HBox) row).getChildren()) {
//                Cell cell = (Cell) cellNode;
//                if (cell.isTargeted) {
//                    boolean hit = cell.confirmAttack(); // Confirm the attack
//                    if (hit) {
//                        // handle hit logic, e.g., check if game is over
//                    }
//                    cell.isTargeted = false; // Reset the targeted flag
//                }
//            }
//        }
//        // Add any additional logic needed after confirming attacks
//    }

    public boolean allShipsSunk() {
        return ships == 0;  // Assuming 'ships' is decremented whenever a ship is fully sunk
    }

    public boolean placeShip(Ship ship, int x, int y) {
        if (canPlaceShip(ship, x, y)) {
            int length = ship.type;

            if (ship.isVertical) {
                for (int i = y; i < y + length; i++) {
                    Cell cell = getCell(x, i);
                    cell.ship = ship;
                    if (!enemy) {
                        cell.setFill(Color.WHITE);
                        cell.setStroke(Color.GREEN);
                    }
                }
            }
            else {
                for (int i = x; i < x + length; i++) {
                    Cell cell = getCell(i, y);
                    cell.ship = ship;
                    if (!enemy) {
                        cell.setFill(Color.WHITE);
                        cell.setStroke(Color.GREEN);
                    }
                }
            }

            return true;
        }

        return false;
    }

    private Cell[] getNeighbors(int x, int y) {
        Point2D[] points = new Point2D[]{
                new Point2D(x - 1, y),
                new Point2D(x + 1, y),
                new Point2D(x, y - 1),
                new Point2D(x, y + 1)
        };

        List<Cell> neighbors = new ArrayList<Cell>();

        for (Point2D p : points) {
            if (isValidPoint(p)) {
                neighbors.add(getCell((int) p.getX(), (int) p.getY()));
            }
        }

        return neighbors.toArray(new Cell[0]);
    }

    private boolean canPlaceShip(Ship ship, int x, int y) {
        int length = ship.type;

        if (ship.isVertical) {
            for (int i = y; i < y + length; i++) {
                if (!isValidPoint(x, i))  // Check if the point is within the board limits
                    return false;

                Cell cell = getCell(x, i);
                if (cell.ship != null)  // Check only the intended cell, not neighbors
                    return false;
            }
        } else {
            for (int i = x; i < x + length; i++) {
                if (!isValidPoint(i, y))  // Check if the point is within the board limits
                    return false;

                Cell cell = getCell(i, y);
                if (cell.ship != null)  // Check only the intended cell, not neighbors
                    return false;
            }
        }

        return true;  // Return true if all checks are passed
    }

    private boolean isValidPoint(Point2D point) {
        return isValidPoint(point.getX(), point.getY());
    }

    private boolean isValidPoint(double x, double y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    public void setCellBlue(int x, int y) {
        Cell cell = getCell(x, y);
        if (cell != null && !cell.wasShot) {
            cell.setFill(Color.BLUE);
            cell.wasShot = true;  // Assuming marking the cell as shot
        }
    }

    // Method to set a cell's color to red (indicating a hit typically)
    public void setCellRed(int x, int y) {
        Cell cell = getCell(x, y);
        if (cell != null && !cell.wasShot) {
            cell.setFill(Color.RED);
            cell.wasShot = true;  // Assuming marking the cell as shot
            if (cell.ship != null) {
                cell.ship.hit();
                if (!cell.ship.isAlive()) {
                    ships--;
                }
            }
        }
    }
    public class Cell extends Rectangle {
        public int x, y;
        public Ship ship = null;
        public boolean wasShot = false;
        public boolean isTargeted = false; // Add a flag to indicate the cell is targeted for an attack

        private Board board;

        public Cell(int x, int y, Board board) {
            super(30, 30);
            this.x = x;
            this.y = y;
            this.board = board;
            setFill(Color.LIGHTGRAY);
            setStroke(Color.BLACK);
        }

        public Cell target() {
            if (!wasShot && board.currentTarget != this) {  // Ensure it's not the same cell
                    if (board.currentTarget != null) {
                        board.currentTarget.resetColor();  // Reset the previous targeted cell
                    }
                    board.currentTarget = this; // Update current target to this cell
                    setFill(Color.DARKGRAY);     // Set the cell color to dark grey to indicate targeting
                    return this;

            }
            return null;
        }

        public boolean shoot() {
            if (!wasShot) {
                wasShot = true;
                setFill(Color.BLACK);

                if (ship != null) {
                    ship.hit();
                    setFill(Color.RED);
                    if (!ship.isAlive()) {
                        board.ships--;
                    }
                    return true;
                }
            }
            return false;
        }

        public void resetColor() {
            if (!wasShot) {
                setFill(Color.LIGHTGRAY);
//                isTargeted = false;
            }
        }

        public boolean confirmAttack() {
            if (isTargeted && !wasShot) {
                wasShot = true;
                setFill(Color.BLACK); // Confirm the attack, turn the cell black

                if (ship != null) {
                    ship.hit();
                    setFill(Color.RED); // If it hits a ship, mark it red
                    if (!ship.isAlive()) {
                        board.ships--;
                    }
                    return true;
                }
            }
            return false;
        }


        public String toString(){
             return "X : " + x + " Y : " + y;
        }
    }
}