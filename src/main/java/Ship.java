import javafx.scene.Parent;

public class Ship {
    public int type; //This is the length of the ship ex. 5 - > ship is 5 cells long
    public boolean isVertical; // If ship is vertical -> true, if it is horizontal - > false;

    public int health;

    public Ship(int type, boolean isVertical){
        this.health = type;
        this.isVertical = isVertical;
        this.type = type;

    }

    // if a ship is hit the health decreases by 1 point
    public void hit() {
        health--;
    }

    // returns true if alive, false if ship is sunk
    public boolean isAlive() {
        return health > 0;
    }

}
