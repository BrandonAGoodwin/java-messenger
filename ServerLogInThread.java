
// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.

import java.net.*;
import java.io.*;

public class ServerLogInThread extends Thread {

	private Socket socket;
	private ClientTable clientTable;

	private BufferedReader fromClient;
	private PrintStream toClient;

	private String clientName;
	private String uniqueCode;

	public ServerLogInThread(Socket socket, ClientTable clientTable) {

		// This table will be shared by the server threads:
		this.clientTable = clientTable;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {

			// This is so that we can use readLine():
			fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// This is so that we can use println():
			toClient = new PrintStream(socket.getOutputStream());

			// Unique code for client not yet assigned
			uniqueCode = null;

			// We loop continuously until the user quits
			while (true) {

				// We always expect a command first
				String command = fromClient.readLine().toLowerCase(); // Matches BBBBB in Client.java

				if (command.equals(Command.REGISTER)) {
					register();
					continue;
				}

				if (command.equals(Command.LOGIN)) {

					// Read in the clients name
					clientName = fromClient.readLine(); // Matches CCCCL in Client.java

					Report.behaviour("User " + clientName + " is trying to login");

					if (clientTable.contains(clientName)) {
						toClient.println(Command.ALLOW);
						String password = fromClient.readLine();
						if (clientTable.getAccount(clientName).isPassword(password)) {
							// Tell the client that logging in was successful
							toClient.println(Command.ALLOW); // Matches DDDDL in Client.java

							login();
							// We remove this print writer from the ArrayList of print writers for the
							// account logged in to
							clientTable.getClientWriters(clientName).remove(toClient);

							// If the user logged out don't exit the loop, else the client quit, so exit the
							// loop
							if (fromClient.readLine().equals(Command.LOGOUT)) {
								continue;
							} else {
								break;
							}
						}else {
							toClient.println(Command.BLOCK);
							// Try give more information as to why they failed
							Report.error("User " + clientName + " failed to login");
						}
					} else {
						// Tell the client that the login was unsuccessful
						toClient.println(Command.BLOCK); // Matches DDDDL in Client.java

						// Try give more information as to why they failed
						Report.error("User " + clientName + " failed to login");
					}
				}

				if (command.equals(Command.QUIT)) {
					fromClient.close();
					toClient.close();
					socket.close();
					Report.behaviour("User has disconnected");
					break;
				}

			}

		} catch (IOException e) {
			Report.error("IO error " + e.getMessage());
		}
	}

	/**
	 * Receive a clientName from the Client, if the the name is already in the
	 * clientTable we tell send a BLOCK command to the client otherwise we send an
	 * ALLOW command to let the client know the clientName was registered
	 * successfully
	 * 
	 * @throws IOException
	 */
	private void register() throws IOException {

		clientName = fromClient.readLine(); // Matches CCCCR in Client.java

		Report.behaviour("Client is trying to register with nickname " + clientName);

		// Check the name isn't already in use
		if (clientTable.contains(clientName)) {

			// Tell the client that registering failed
			toClient.println(Command.BLOCK); // Matches DDDDR in Client.java

			Report.error("The nickname " + clientName + " is already in use");

		} else {
			// Tell the client that it is waiting to receive a password was successful
			toClient.println(Command.ALLOW); // Matches DDDDR in Client.java

			String password = fromClient.readLine();

			// We add the client to the table:
			clientTable.add(clientName, password);
			Report.behaviour("User " + clientName + " successfully registered");
		}
	}

	private void login() {

		Report.behaviour(clientName + " connected");

		// If a unique code hasn't already been generated for this user, generate a new
		// code
		if (uniqueCode == null) {
			uniqueCode = clientTable.generateUniqueCode();
		}

		// We create and start a new thread to read from the client:
		ServerReceiver reciever = new ServerReceiver(clientName, fromClient, clientTable, uniqueCode);
		reciever.setName(clientName + "'s ServerReceiver");
		reciever.start();

		// We create and start a new thread to write to the client:
		ServerSender sender = new ServerSender(clientTable.getAccount(clientName), toClient, uniqueCode);
		sender.setName(clientName + "'s ServerSender");
		sender.start();

		try {
			// Wait for both threads to finish before continuing to the ServerLogInThread
			// loop
			reciever.join();
			sender.join();

		} catch (InterruptedException e) {
			Report.error("Unexpected interruption " + e.getMessage());
		}

	}
}
