import java.io.*;

public class ClientReceiver extends Thread {

	private BufferedReader server;
	private ClientUI clientUI;

	/**
	 * Constructor. Creates the ClientReceiver.
	 * 
	 * @param server
	 *            The BufferedReader to read information sent by the server.
	 * @param clientUI
	 *            The ClientUI to print information to the UI
	 */
	ClientReceiver(BufferedReader server, ClientUI clientUI) {
		this.server = server;
		this.clientUI = clientUI;
	}

	/**
	 * Start the loop that listens for messages from the server.
	 */
	public void run() {
		// Print to the user whatever we get from the server:
		try {

			while (true) {
				String input = server.readLine();

				// Check the input isn't null (not possible after GUI implementation)
				if (input != null) {

					// If the input is a QUIT or LOGOUT command break the loop (ending the thread)
					if (input.equals(Command.QUIT) || input.equals(Command.LOGOUT)) {
						Report.behaviour("Received " + input + " command");
						break;
					} else {
						// Otherwise print out the input from the server
						// System.out.println(input);
						clientUI.tell(input);
					}
				} else {
					// If null is received this means the InputStream from the server has been
					// closed and must have died
					Report.errorAndGiveUp("Server seems to have died");
				}
			}

		} catch (IOException e) {
			Report.errorAndGiveUp("Server seems to have died " + e.getMessage());
		}
	}
}
