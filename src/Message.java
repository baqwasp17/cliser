import java.io.Serializable;
public class Message implements Serializable {
	Command command;
	String roomName;
	String messageString;
	String clientId;

	public Message(Command command, String roomName, String messageString) {
		this.command = command;
		this.roomName = roomName;
		this.messageString = messageString;
	}

	public String toString() {
		return String.format("Command: %s, Room: %s, Message: %s, From: %s", this.command, this.roomName, this.messageString, this.clientId);
	}
}
