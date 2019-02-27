import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Uses a loop to continuously waits for the user to input a command and then
 * asks for a sequence of inputs and sends a series of messages to the server to
 * carry out the input command.
 * 
 * @author bxg796
 *
 */
public class ClientSender extends MessengerThread {

	/**
	 * Format for the time.
	 */
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	/**
	 * Format to get the date.
	 */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM");

	/**
	 * The PrintStream to send data to the server.
	 */
	private PrintStream server;

	/**
	 * The clientUI.
	 */
	private ClientUI clientUI;

	/**
	 * Constructor.
	 * 
	 * @param clientUI
	 *            The clientUI is used to take input in from the user for the
	 *            ClientSender to use.
	 * @param server
	 *            The PrintStream to the server so that messages can be sent to the
	 *            server.
	 */
	ClientSender(ClientUI clientUI, PrintStream server) {
		this.clientUI = clientUI;
		this.server = server;
	}

	/**
	 * Runs the loop of listening for user input, checking the initial input is a
	 * command and acting on it.
	 */
	public void run() {

		try {
			// When the user logs on, get their current message if they have any
			current();

			// Then loop forever sending messages to recipients via the server:
			while (true) {

				// Read in input from the clientUI
				String command = clientUI.readLine();

				// Check the input is a command
				if (Command.isCommand(command)) {

					runCommand(command);
					
					// If the command was QUIT or LOGOUT break out of the while loop ending the
					// thread
					if (command.equals(Command.QUIT) || command.equals(Command.LOGOUT)) {
						break;
					}

				} else {
					clientUI.error("Invalid input - Not a valid command");
				}
			}

		} catch (IOException e) {
			Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
		} catch (InterruptedException e) {
			// Continue listening for an input
		}

	}

	/**
	 * Carry out the message sending sequence. Inputing the recipient and text to
	 * send a message to a user registered in the server.
	 */
	@Override
	protected void send() throws IOException, InterruptedException {
		clientUI.tell("Input the recipient:");
		String recipient = clientUI.readLine();

		// Check the user entered a recipient name
		if (recipient.isEmpty() == false) {
			server.println(Command.SEND);
			server.println(recipient);
			clientUI.tell("Input your message:");
			String text = clientUI.readLine();
			server.println(text);

			clientUI.tell("[" + getTime() + "]To " + recipient + ": " + text);
		} else {
			clientUI.error("Invalid input - You must input the recipient's nickname");
		}

	}

	/**
	 * Send the current command to the server.
	 */
	@Override
	protected void current() {
		server.println(Command.CURRENT);
	}

	/**
	 * Send the previous command to the server.
	 */
	@Override
	protected void previous() {
		server.println(Command.PREVIOUS);
	}

	/**
	 * Send the next command to the server.
	 */
	@Override
	protected void next() {
		server.println(Command.NEXT);
	}

	/**
	 * Send the delete command to the server.
	 */
	@Override
	protected void delete() {
		server.println(Command.DELETE);
	}

	/**
	 * Send the logout command to the server.
	 */
	@Override
	protected void logout() {
		server.println(Command.LOGOUT);
	}

	@Override
	protected void help() {
		clientUI.tell("send\t\tInitiatates the message sending sequence\n"
		+ "current\t\tPrints message at the current pointer location\n"
		+ "next\t\tMoves the pointer towards to the next most recent message and prints it\n"
		+ "previous\tMoves the pointer towards the previous oldest message and prints it\n"
		+ "delete\t\tDeletes the message at the pointer\n"
		+ "logout\t\tLogs the client out of the current user\n"
		+ "quit\t\tCloses the client application");
	}

	/**
	 * Send the quit command to the server.
	 */
	@Override
	protected void quit() {
		server.println(Command.QUIT);
	}

	/**
	 * Get the current time.
	 */
	private String getTime() {
		Calendar cal = Calendar.getInstance();
		String time = timeFormat.format(cal.getTime());
		String date = dateFormat.format(cal.getTime());
		return (date + " | " + time);
	}

}