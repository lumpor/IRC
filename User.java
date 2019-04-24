package Server;

import java.io.*;
import java.net.*;
import java.util.*;

//Representation of a user
public class User {
	private PrintWriter out;
	private BufferedReader in;
	private String nickname;
	private static int counter = 0;
	private Server server;
	private Room room;
	private Socket socket;
	private HashMap<String, Command> commands = new HashMap<String, Command>();

	protected User(Socket socket, Room room, Server server) {
		this.socket = socket;
		this.server = server;
		this.room = room;
		counter++;
		nickname = "User " + counter;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			InitiateCommandList();
		} catch (IOException e) {
			write("Socket exception");
		}
		listRooms();
	}

	private interface Command {
		void perform(String parameter);
	}

	//These are all available commands. For what they do, see documentation.
	private void InitiateCommandList() {

		commands.put("nick", param -> {
			if (param.isEmpty())
				write("No nickname specified");
			else if (server.nicknameAvailable(param))
				nickname = param;
			else
				write("Nickname already taken");
		});
		commands.put("rooms", param -> {
			if (!param.isEmpty())
				write("Wrong command format");
			else
				listRooms();
		});
		commands.put("create", param -> {
			if (param.isEmpty())
				write("No room name specified");
			else if (server.findRoom(param).equals(new Room("§NULL§"))) {
				server.createRoom(param);
				listRooms();
			} else
				write("Room already exists");
		});
		commands.put("users", param -> {
			if (!param.isEmpty())
				write("Wrong command format");
			else
				listUsers(room);
		});
		commands.put("kill", param -> {
			if (param.isEmpty())
				write("No victim specified");
			else if (param.equals(nickname)) {
				write("You commit seppuku");
				logout();
			} else if (!room.containsUser(param))
				write("Victim not here");
			else {
				User victim = room.findUser(param);
				victim.write("You feel a sharp pain in your chest");
				victim.logout();
			}
		});
		commands.put("nuke", param -> {
			if (param.isEmpty())
				write("No target specified");
			else if (param.equals("Lobby"))
				write("Lobby is nuke-proof");
			else {
				Room target = server.findRoom(param);
				if (target.equals(new Room("§NULL§")))
					write("Room not found");
				else {
					ArrayList<User> evacuators = target.getUsers();
					server.removeRoom(target);
					write("BOOM!");
					for (User u : evacuators) {
						u.joinRoom(server.findRoom("Lobby"));
						u.write("You've been nuked");
						u.listRooms();
					}
				}
			}
		});
		commands.put("join", param -> {
			if (param.isEmpty())
				write("No destination specified");
			else {
				Room newRoom = server.findRoom(param);
				if (newRoom.equals(new Room("§NULL§")))
					write("Room not found");
				else {
					room.getUsers().remove(this);
					joinRoom(newRoom);
					listRooms();
				}
			}
		});
		commands.put("exit", param -> {
			if (!param.isEmpty())
				write("Wrong input format");
			else
				logout();
		});
	}
	//Performs commands/sends messages until user logout after which IOException is thrown
	protected boolean handleInput() {
		try {
			String line = in.readLine();
			if (line.startsWith("/")) {
				performCommand(line);
			} else
				room.write(nickname, line);
			return true;
		} catch (IOException logout) {
			return false;
		}
	}
	private synchronized void performCommand(String line) {
		String[] split = line.substring(1).split("\\s+", 2);
		if (!commands.containsKey(split[0]))
			write("Command not recognized");
		else {
			String param = "";
			if (split.length > 1)
				param = split[1];
			commands.get(split[0]).perform(param);
		}
	}

	private void listRooms() {
		write("---------------");
		write("You are in: " + room.getName());
		write("Available rooms:");
		for (Room r : server.getRooms()) {
			if (!r.equals(room))
				write(r.getName());
			// IO
		}
		write("---------------");
	}

	private void listUsers(Room room) {
		write("Users in " + room.getName() + ":");
		for (User u : room.getUsers()) {
			write(u.getName());
		}
		out.flush();
	}

	private void joinRoom(Room destination) {
		destination.getUsers().add(this);
		room = destination;
	}

	protected String getName() {
		return nickname;
	}

	protected synchronized void write(String data) {
		out.println(data);
		out.flush();
	}

	private synchronized void logout() {
		room.getUsers().remove(this);
		write("§QUIT§");
		try {
			socket.close();
		} catch (IOException e) {
			write("Could not close socket");
		}
	}

	protected boolean equals(User u) {
		return this.nickname == u.getName();
	}
}
