import java.net.*;
import java.io.*;

public class Server {

	public static void main(String[] args) {

		// This table will be shared by the server threads:
		ClientTable clientTable = new ClientTable();;
		// Maybe allow for arg 0 to be a client table file location
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(Port.number);
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}

		try {
			// Try to load in a clientTable if the files are there
			clientTable.read();
			
			// Create the saveThread that has the sole purpose of updating the clientTable files at a set rate 
			SaveThread saveThread = new SaveThread(clientTable);
			saveThread.setDaemon(true); // End the thread when the Server thread ends
			saveThread.start(); // Start running
			
			// We loop for ever, looking for a new connection
			while (true) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept(); // Matches AAAAA in Client
				Report.behaviour("Connection accepted");

				(new ServerLogInThread(socket, clientTable)).start();

			}
		} catch (IOException e) {
			Report.error("IO error " + e.getMessage());
		}
	}
}

/**
 * A class to frequently save a clientTable.
 * @author b1999
 *
 */
class SaveThread extends Thread {
	
	private ClientTable clientTable;

	public SaveThread(ClientTable clientTable) {
		this.clientTable = clientTable;
	}

	@Override
	public void run() {
		
		while(true) {
			try {
				sleep(1000);
				clientTable.save();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

	}

}
