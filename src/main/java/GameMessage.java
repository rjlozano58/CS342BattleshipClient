import java.io.Serializable;

public class GameMessage extends Message implements Serializable {
    private int x;
    private int y;

    // Constructor that takes coordinates directly
    public GameMessage(String sender, String recipient, String content, int x, int y) {
        super(sender, recipient, content);
        this.x = x;
        this.y = y;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "GameMessage{" +
                "sender='" + getSender() + '\'' +
                ", recipient='" + getRecipient() + '\'' +
                ", content='" + getContent() + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

