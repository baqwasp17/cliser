import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

public class Client {
	public static final int PORT = 8888;
	public static void main(String args[]) throws Exception {
		try(Socket socket = new Socket("localhost", PORT)) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			Thread reader = new Thread(() -> {
				try(ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					while(true) {
						Message receivedMessage = (Message) ois.readObject();
						System.out.println(receivedMessage);
					}
				} catch(Exception e) {
					System.err.println("Something went wrong: " + e.getMessage());
				}
			});
			reader.start();
			while(true) {
				Message message = null;
				System.out.println("[1] Join a room.");
				System.out.println("[2] Exit a room.");
				System.out.println("[3] Send a message to a room.");
				System.out.print("Select an option: ");

				int choice = Integer.parseInt(br.readLine());
				switch(choice) {
					case 1: {
						System.out.println("Enter the name of the room: ");
						String roomName = br.readLine();
						message = new Message(Command.JOIN, roomName, null);
						break;
					}
					case 2: {
						System.out.print("Enter the name of the room: ");
						String roomName = br.readLine();
						message = new Message(Command.EXIT, roomName, null);
						break;
					}
					case 3: {
						System.out.print("Enter the name of the room: ");
						String roomName = br.readLine();
						System.out.print("Enter the message: ");
						String messageString = br.readLine();
						message = new Message(Command.SEND, roomName, messageString);
						break;
					}
					default:
						System.out.println("Try again!!");
						break;
				}

				if(Objects.nonNull(message)) {
					oos.writeObject(message);
					oos.flush();
					System.out.println("Message sent!!");
				}
			}
		} catch(Exception e) {
			System.err.println("Something went wrong: " + e.getMessage());
		}
	}
}
