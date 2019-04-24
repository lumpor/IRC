package Server;

import java.util.ArrayList;
//A room that contains a certain amount of users. Users can only interact with others in the same room
public class Room {
	
	private final String name;
	private ArrayList<User> users = new ArrayList<User>();
	
	protected Room(String name) {
		this.name = name;
	}
	protected String getName() {
		return name;
	}
	protected synchronized ArrayList<User> getUsers(){
		return users;
	}
	protected boolean containsUser(String name) {
		for (User user : users) {
			if(user.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	//Always check with containsUser() before using this function
	protected User findUser(String name) {
		for (User user : users) {
			if(user.getName().equals(name))
				return user;
		}
		return null;
	}
	protected synchronized void write (String nickname, String message) {
		for (User user : users) {
			user.write(nickname + ": " + message);
		}
	}
	protected boolean equals(Room r) {
		return this.name.equals(r.getName());
	}
}
