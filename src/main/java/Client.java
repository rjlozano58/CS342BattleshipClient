// Rogelio Lozano, Pradyun Shrestha, Zakareah Hafeez
// CS 342 - Software Design - Prof. McCarthy
// Project 4: Battleship

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
import javafx.util.Pair;
import java.util.HashMap;



public class Client extends Thread{

	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	String clientUsername;
	private Consumer<Serializable> callback;
	public ArrayList<String> serverClients = new ArrayList<>();
	
	Client(Consumer<Serializable> call){
	
		callback = call;
	}
	
	public void run() {
		
		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {
			System.err.println("There was trouble gonnecting to the server: " + e.getMessage());
		}
		
		while(true) {
			 
			try {
				Object receivedObject = in.readObject();

				if (receivedObject instanceof GameMessage){
					GameMessage attack = (GameMessage) receivedObject;
					callback.accept(attack);
					System.out.println("Received " + attack.getContent() + " from " + attack.getSender());
				}else if (receivedObject instanceof Message){ // we received a message
					Message message = (Message) receivedObject;
					clientUsername = message.getRecipient();
					System.out.println("your username is : " + clientUsername);
					callback.accept(message.getContent());
				}else if(receivedObject instanceof ArrayList){ // we receive a ArrayList object
					serverClients = (ArrayList<String>) receivedObject;

					printArrayList(serverClients);

				}
			}
			catch(Exception e) {
				System.err.println("There was trouble getting message from server");
			}
		}
	
    }


	public void send(Message data) {

		if (out != null) {
			try {
				out.writeObject(data);
				out.reset();
				out.flush();

			} catch(IOException e) {
				System.out.println("Error sending message: " + e.getMessage());
			}
		} else {
			System.out.println("Connection is not established properly.");
		}
	}

	public void send(GameMessage data) {

		if (out != null) {
			try {
				out.writeObject(data);
				out.reset();
				out.flush();
			} catch(IOException e) {
				System.out.println("Error sending message: " + e.getMessage());
			}
		} else {
			System.out.println("Connection is not established properly.");
		}
	}

	public void send(Pair<String,String> data) {

		if (out != null) {
			try {
				out.writeObject(data);
				out.reset();
				out.flush();

			} catch(IOException e) {
				System.out.println("Error sending message: " + e.getMessage());
			}
		} else {
			System.out.println("Connection is not established properly.");
		}
	}

	public void printArrayList(ArrayList<String> list){
		System.out.print("This Array List has :");
			for (int i = 0; i < list.size();i++){
				System.out.print(" " + list.get(i));
			}
		System.out.println();

	}

}
