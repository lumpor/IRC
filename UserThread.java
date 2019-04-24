package Server;

public class UserThread extends Thread{
	private User user;	
	public UserThread (User user) {
		this.user = user;
	}
	
	public void run() {
		while(user.handleInput());
	}
}
