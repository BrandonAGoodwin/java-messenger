import java.io.*;
import java.util.concurrent.*;

// Comment and class explanation

public class ServerReceiver extends MessengerThread {
	private String clientsName;
	private BufferedReader client;
	private ClientTable clientTable;
	private String uniqueCode;

	public ServerReceiver(String clientsName, BufferedReader client, ClientTable clientTable, String uniqueCode) {
		this.clientsName = clientsName;
		this.client = client;
		this.clientTable = clientTable;
		this.uniqueCode = uniqueCode;
	}

	public void run() {
		try {
			while (true) {
				// Read the command from the ClientSender
				String command = client.readLine();
				runCommand(command);
				// If the command was QUIT or LOGOUT break out of the while loop ending the
				// thread
				if (command.equals(Command.QUIT) || command.equals(Command.LOGOUT)) {
					break;
				}
			}

		} catch (IOException e) {
			Report.error("Something went wrong with the client " + clientsName + " " + e.getMessage());
		} catch (InterruptedException e) {
			// Do nothing and keep listening
		}
	}

	@Override
	protected void send() throws IOException {
		String recipient = client.readLine();
		String text = client.readLine();

		// Check that this is previously checked, if so this if is unnecessary
		if (clientTable.contains(recipient)) {
			BlockingQueue<Message> recipientsQueue = clientTable.getAccount(recipient).getPendingMessageQueue();
			Message message = new Message(clientsName, text, Command.SEND);

			// if (recipientsQueue != null) {
			recipientsQueue.offer(message);
			// }

		} else {
			Report.error("Message for unexistent client " + recipient + ": " + text);
		}
	}

	@Override
	protected void current() {
		sendCommand(Command.CURRENT);
	}
	
	@Override
	protected void previous() {
		sendCommand(Command.PREVIOUS);
	}

	@Override
	protected void next() {
		sendCommand(Command.NEXT);
	}

	@Override
	protected void delete() {
		sendCommand(Command.DELETE);
	}

	@Override
	protected void logout() {
		sendCommand(Command.LOGOUT);
	}

	@Override
protected void help() {/* Do Nothing */}

	@Override
	protected void quit() {
		sendCommand(Command.QUIT);
	}

	private void sendCommand(String command) {
		// Get the blocking queue for the current user and add a message with the
		// command and unique code attached to the users BlockingQueue
		//BlockingQueue<Message> usersQueue = clientTable.getQueue(clientsName);
		BlockingQueue<Message> usersQueue = clientTable.getAccount(clientsName).getPendingMessageQueue();
		Message commandMessage = new Message(command, uniqueCode);
		Report.behaviour("Sending " + command + " command to " + clientsName + " userQueue");
		usersQueue.offer(commandMessage);
	}

}
