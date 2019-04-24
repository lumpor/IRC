package Server;

import java.util.*;
import java.net.*;
import java.io.*;

//Listens for and establishes connections, as well as handles rooms
public class Server{
	ArrayList<Room> rooms = new ArrayList<Room>();
	
	ServerSocket server;
	public Server(int port) {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Port no. not recognized");
		}
		rooms.add(new Room("Lobby"));
		while(true){
			try {
				User newUser = new User(server.accept(), rooms.get(0), this);
				rooms.get(0).getUsers().add(newUser);
				new UserThread(newUser).start();
			} catch(IOException e) {
				System.out.println("Connection error occured");
			}
		}
	}
	
	protected synchronized void createRoom(String name) {
		for (Room room : rooms) {
			if (room.getName().equals(name)) return;
		}
		rooms.add(new Room(name));
	}
	protected synchronized void removeRoom(Room room) {
		rooms.remove(room);
	}
	protected synchronized ArrayList<Room> getRooms(){
		return rooms;
	}
	
	protected synchronized Room findRoom(String targetName) {
		for (Room room : rooms) {
			if(room.getName().equals(targetName)) {
				return room;
			}
		}
		return new Room("§NULL§");
	}
	protected synchronized boolean nicknameAvailable(String name) {
		for (Room room : rooms) {
			if(room.containsUser(name))
				return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		new Server(7);
	}
	

}

