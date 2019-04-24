package Client;

import java.io.*;
import java.net.*;

import Server.Room;
import Server.User;

public class Client {

	private BufferedReader stdIn;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	//Establishes a connection with server and continuously communicates with it
	public Client() {
		stdIn = new BufferedReader(new InputStreamReader(System.in));
		try {
			socket = new Socket("127.0.0.1", 7);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("Host not found");
		} catch (IOException e) {
			System.out.println("Socket error");
		}
	}
	//Waits for the user to input messages/commands
	protected boolean prompt() {
		try {
			String line = stdIn.readLine();
			out.println(line);
			out.flush();
		} catch (IOException e) {
			System.out.println("You ded");
			return false;
		}
		return true;
	}
	//Prints our the data it receives to the terminal. The message §QUIT§ tells the client to shut down
	protected boolean handleInput(){

		String line;
		try {
			line = in.readLine();
			if (line.equals("§QUIT§")) {
				logout();
				return false;
			}
			System.out.println(line);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return true;
	}

	private void logout() {
		try {
			stdIn.close();
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
		}
		;
	}

	public static void main(String[] args) {
		Client client = new Client();
		new ClientThread(client).start();
		while (client.prompt())
			;
	}
}
