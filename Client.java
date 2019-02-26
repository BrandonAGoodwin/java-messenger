
// CHANGE
// Usage:
//        java Client user-nickname server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.

import java.io.*;
import java.net.*;

public class Client {

	private static final String DEFAULT_CLIENT_NAME = "Client";

	// Open sockets:
	private PrintStream toServer = null;
	private BufferedReader fromServer = null;
	private Socket server = null;

	private ClientUI clientUI = null;

	// Holds the client making it wait for user input from the clientUI
	private Object holder = new Object();

	// When set to true, at the end of the while loop, the client will quit
	private boolean quitting = false;

	public static void main(String[] args) {

		if (args.length != 1) {
			Report.errorAndGiveUp("Usage: java Client server-hostname");
		}

		// Initialise information:
		String hostname = args[0];

		Client client = new Client();
		client.run(hostname);
	}

	public void run(String hostname) {

		try {
			// Try to connect to the server
			server = new Socket(hostname, Port.number); // Matches AAAAA in Server.java

			// Create the data streams to communicate with the server
			toServer = new PrintStream(server.getOutputStream());
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
		} catch (UnknownHostException e) {
			Report.errorAndGiveUp("Unknown host: " + hostname);
		} catch (IOException e) {
			Report.errorAndGiveUp("The server doesn't seem to be running " + e.getMessage());
		}

		// Create the clientUI
		clientUI = new ClientUI(DEFAULT_CLIENT_NAME, holder);

		while (true) {
			try {

				// Check the user input a valid command
				String input = clientUI.readLine();

				if (Command.isCommand(input)) {

					String command = input;

					if (command.equals(Command.REGISTER)) {
						register();
						continue;
					}

					if (command.equals(Command.LOGIN)) {
						login();
					}

					if (command.equals(Command.QUIT)) {
						quitting = true;
					}

					// Close all the data streams, tell the ServerLogInThread to quit and break out
					// of the loop allowing the Client thread to end
					if (quitting == true) {
						clientUI.dispose();
						toServer.println(Command.QUIT);
						toServer.close();
						fromServer.close();
						server.close();
						break;
					}

				} else {
					clientUI.error("Invalid command");
				}

			} catch (IOException e) {
				Report.errorAndGiveUp("Something wrong " + e.getMessage());
			} catch (InterruptedException e) {
				// Continue
			}
		}

	}

	/**
	 * Carry out registration sequence.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void register() throws IOException, InterruptedException {
		clientUI.tell("Please enter a user name");
		String nickname = clientUI.readLine();

		// Check the user input a value;
		if (nickname.isEmpty()) {
			clientUI.tell("An empty input is not valid");
			return;
		}

		// Check the user name isn't an existing command
		if (Command.isCommand(nickname)) {
			clientUI.error("Invalid username - your nickname can't be the name of a command");
			return;
		}
		
		toServer.println(Command.REGISTER);
		toServer.println(nickname);

		// If an ALLOW command is received from the server, we know that the account was
		// successfully registered
		if (fromServer.readLine().equals(Command.ALLOW)) {
			clientUI.tell("Input your password");
			String password = clientUI.readLine();
			toServer.println(password); // Matches EEEER in ServerLogInThread.java
			clientUI.tell(nickname + " has been successfully registered");
		} else {
			clientUI.error("Username already taken");

		}
	}

	/**
	 * Carry out the login sequence.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void login() throws IOException, InterruptedException {
		toServer.println(Command.LOGIN);
		clientUI.tell("Please enter your username");
		String nickname = clientUI.readLine();
		toServer.println(nickname);

		// Allow is returned if the the user name is not already in use
		if (fromServer.readLine().equals(Command.ALLOW)) {
			clientUI.tell("Please enter your password");

			String password = clientUI.readLine();
			toServer.println(password);

			// Returns true if the password is correct
			if (fromServer.readLine().equals(Command.ALLOW)) {
				clientUI.setTitle(nickname + "'s Messages");

				// Create two client threads of a different nature:
				ClientSender sender = new ClientSender(clientUI, toServer);
				ClientReceiver receiver = new ClientReceiver(fromServer, clientUI);

				// Run them in parallel:
				sender.start();
				receiver.start();
				clientUI.tell("Sucessfully logged in");

				// Wait for them to end and close sockets.
				try {
					sender.join();
					receiver.join();
					clientUI.setTitle(DEFAULT_CLIENT_NAME);

					// If the next messaged received from the server is LOGOUT then continue the
					// while loop
					if (fromServer.readLine().equals(Command.LOGOUT)) {
						clientUI.tell("You have logged out");
						toServer.println(Command.LOGOUT);
					} else {
						// Otherwise quit
						quitting = true;
					}
				} catch (InterruptedException e) {
					Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
				}
			} else {
				clientUI.error("Incorrect password");
			}
		} else {
			clientUI.error("No users with that nickname exist");
		}
	}

}
