// Rogelio Lozano and Pradyun Shrestha
// CS 342 - Software Design - Prof. McCarthy
// Project 3: Messaging App
// Description: This Class is a Message object that is sent between server and client(s)
import java.io.Serializable;

// The Message class represents a message that can be sent between the server and clients.
// It implements the Serializable interface to allow it to be sent over a network connection.
public class Message implements Serializable {
    // A unique identifier for serialization. This is required for the Serializable interface.
    static final long serialVersionUID = 42L;
    private String sender;
    private String recipient; // Use "ALL" for broadcasting
    private String content;

    public Message( String sender, String recipient ,String content) {

        this.sender = sender;

        this.recipient = recipient;

        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}

