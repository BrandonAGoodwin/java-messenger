import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

// Code description and comments

public class ServerSender extends MessengerThread {
	private BlockingQueue<Message> clientMessageQueue;
	private ArrayList<Message> receivedMessages;
	private PrintStream client;
	private ArrayList<PrintStream> allClients;
	private String uniqueCode;

	private Message message;

	// Points to the current message
	private int messagePointer;

	public ServerSender(Account<Message> account, PrintStream client, String uniqueCode) {
		this.client = client;
		this.uniqueCode = uniqueCode;

		// Get the blocking queue that holds all the unreceived messages for this
		// account
		clientMessageQueue = account.getPendingMessageQueue();

		// Get all the ArrayList storing all the received messages for this account
		receivedMessages = account.getReceivedMessages();

		// Get all the client writers for clients logged in to the same account
		allClients = account.getClientWriters();

		// Simultaneous Logins: Add this this clients print writer the the ArrayList of
		// print writers for clients logged in on the same user
		allClients.add(client);

		// Set pointer to last item added to the list
		messagePointer = receivedMessages.size() - 1;
	}

	public void run() {
		while (true) {
			try {
				while (true) {
					message = null;
					// Check for any messages and then sleep for a short delay and repeat
					sleep(500);
					message = clientMessageQueue.peek(); // Matches EEEEE in ServerReceiver
					// If there is a message and it is meant for this user, then take it and break
					// out of the loop
					if (message != null && (message.getUniqueCode().equals(uniqueCode)
							|| message.getCommand().equals(Command.SEND))) {
						message = clientMessageQueue.poll();
						break;
					}
				}
				// Perform an action based on the command within the message
				String command = message.getCommand();
				runCommand(command);

				// If the command was QUIT or LOGOUT, break out of the loop and exit the run
				// loop causing the thread to end
				if (command.equals(Command.QUIT) || command.equals(Command.LOGOUT)) {
					break;
				}

			} catch (InterruptedException | IOException e) {
				// Do nothing and go back to the infinite while loop.
			}
		}

		allClients.remove(client);
		Report.behaviour("User disconnected");
	}

	@Override
	protected void send() {
		// If it is a SEND command, send the message to all clients logged in as the
		// same user
		for (PrintStream client : allClients) {
			client.println(message); // Matches FFFFF in ClientReceiver
			// State there is a shared array pointer
		}

		// And add the message to the received messages array list
		receivedMessages.add(message);

		// Reset the message pointer
		messagePointer = receivedMessages.size() - 1;
	}

	@Override
	protected void current() {
		checkPointer();
		if (messagePointer >= 0) {
			client.println("Current Message: " + receivedMessages.get(messagePointer));
		} else {
			client.println("From server: There are no messages to be retrieved");
		}
	}

	@Override
	protected void previous() {
		checkPointer();
		if (messagePointer > 0) {
			messagePointer--;
			client.println("Previous Message: " + receivedMessages.get(messagePointer));
		} else {
			client.println("From server: There are no messages to be retrieved");
		}
	}

	@Override
	protected void next() {
		checkPointer();
		// If the pointer isn't already pointing to the max index in the array list
		// increment
		// the pointer and send the message stored at that index to the client
		if (receivedMessages.size() - 1 > messagePointer) {
			messagePointer++;
			client.println("Next Message: " + receivedMessages.get(messagePointer));
		} else {
			// Otherwise tell the client there were no messages to be retrieved
			client.println("From server: There are no messages to be retrieved");
		}
	}

	@Override
	protected void delete() {
		checkPointer();
		// If the pointer is pointing to a index storing a message delete the message
		if (messagePointer >= 0) {
			receivedMessages.remove(messagePointer);
			client.println("From server: Message deleted");
			messagePointer--;
		} else {
			// Otherwise let the client know there was no message to delete
			client.println("From server: There are no messages to delete");
		}
	}

	@Override
	protected void logout() {
		// Send the logout command to the clientReciver so it exits
		client.println(Command.LOGOUT);
		// Send the logout command again to the Client so it knows not the close
		client.println(Command.LOGOUT);
	}

	@Override
	protected void quit() {
		// Send the quit command to the clientReceiver
		client.println(Command.QUIT); // Matches FFFFF in ClientReceiver
		// Send the quit command again to the Client so it knows it should close
		client.println(Command.QUIT);
	}

	// Check the message pointer isn't larger than the size of the array list
	private void checkPointer() {
		messagePointer = Math.min(messagePointer, receivedMessages.size() - 1);
	}
}