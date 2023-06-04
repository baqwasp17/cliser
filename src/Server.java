import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	public static final int PORT = 8888;
	public static final String DEFAULT_ROOM = "";
	public static Map<String, List<ClientHandler>> rooms;
	public static ExecutorService es;

	public static void main(String[] args)throws Exception {
		ServerSocket serverSocket = new ServerSocket(PORT);
		rooms = new ConcurrentHashMap<>();
		rooms.put(DEFAULT_ROOM, new ArrayList<>());
		es = Executors.newFixedThreadPool(10);
		Socket clientSocket = null;

		while(true) {
			try {
				clientSocket = serverSocket.accept();
				if(Objects.isNull(clientSocket)) continue;
				ClientHandler clientHandler = new ClientHandler(clientSocket, UUID.randomUUID().toString());
				rooms.get(DEFAULT_ROOM).add(clientHandler);//everyone gets added to the default room
				es.submit(clientHandler);
			} catch (Exception e) {
				System.err.println("Something went wrong: " + e.getMessage());
			}
		}
	}



	static class ClientHandler implements Runnable {
		private Socket socket;
		private String clientId;
		private ObjectOutputStream oos;

		public ClientHandler(Socket socket, String clientId) {
			this.socket = socket;
			this.clientId = clientId;
		}

		public void sendMessage(Message message) throws Exception {
			if(Objects.isNull(this.oos)) {
				this.oos = new ObjectOutputStream(this.socket.getOutputStream());
			}
			this.oos.writeObject(message);
			this.oos.flush();
		}

		public void run() {
			try (ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream())) {
				while(true) {
					System.out.println("Listening for messages");
					Message message = (Message) ois.readObject();
					System.out.println("Got a message");
					message.clientId = this.clientId;
					System.out.println(message);
					switch(message.command) {
						case EXIT:
							List<ClientHandler> clientsInRoom = rooms.get(message.roomName);
							if(Objects.nonNull(clientsInRoom)) clientsInRoom.remove(this);
							break;
						case JOIN:
							rooms.computeIfAbsent(message.roomName, k -> new ArrayList<>()).add(this);
							break;
						case SEND:
							String targetRoom = Optional.ofNullable(message.roomName).orElse(DEFAULT_ROOM);
							if(Objects.nonNull(rooms.get(targetRoom)) && rooms.get(targetRoom).contains(this)) {
								for(ClientHandler client : rooms.get(targetRoom)) {
									if(this.equals(client)) continue;
									try {
										client.sendMessage(message);
									} catch(Exception e) {
										System.out.print("Something went wrong while seinding message to client: " + client.clientId);
										System.out.println("message: " + e.getMessage());
									}
								}
							}
							break;
					}
				}
				
			} catch (Exception e) {
				System.err.print("Something went wrong while reading from client : " + this.clientId);
				System.err.println(" message: " + e.getMessage());
				rooms.forEach((k, v) -> v.remove(this));
				try {this.socket.close();} catch(Exception ex) {}
			}
		}
	}
}
