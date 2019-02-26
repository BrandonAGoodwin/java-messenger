import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ClientTable {

	// All the file locations
	private static final String ACCOUNT_TABLE_FILE_LOCATION = "saves/accountTableFile.txt";
	private static final String COUNTER_FILE_LOCATION = "saves/counterFile.txt";

	/**
	 * Store all the user accounts
	 */
	private ConcurrentMap<String, Account<Message>> accountTable = new ConcurrentHashMap<String, Account<Message>>();

	/**
	 * The counter used to produce unique codes
	 */
	private Integer counter = 1;

	/**
	 * Add a new account to the clientTable.
	 * 
	 * @param nickname
	 *            The nickname to identify data associated with the account.
	 * @param password
	 *            The password to be set for this account
	 */
	public void add(String nickname, String password) {
		accountTable.put(nickname, new Account<Message>(password));
	}

	/**
	 * Generate a unique code that can be used to identify messages for particular
	 * ServerSenders
	 * 
	 * @return Return a unique code (Unique from all previously generated codes)
	 */
	public String generateUniqueCode() {
		synchronized (counter) {
			String uniqueCode = String.format("%04d", counter++);
			return uniqueCode;
		}
	}

	/**
	 * Return the ArrayList of PrintStreams for all clients that are logged in on
	 * the specified account
	 * 
	 * @param nickname
	 * @return The ArrayList of PrintStreams for all clients that are logged in on
	 *         the specified account
	 */
	public ArrayList<PrintStream> getClientWriters(String nickname) {
		return accountTable.get(nickname).getClientWriters();
	}

	/**
	 * Get the account that contains all the information and data regarding the
	 * specified user
	 * 
	 * @param nickname
	 * @return The account that contains all the information and data regarding the
	 *         specified user
	 */
	public Account<Message> getAccount(String nickname) {
		return accountTable.get(nickname);
	}

	/**
	 * @param nickname
	 *            A nickname
	 * @return Whether or not the a nickname is stored in the queueTable
	 */
	public boolean contains(String nickname) {
		return accountTable.containsKey(nickname);
	}

	/**
	 * Save the accountTable and counter to files.
	 */
	public void save() {

		// Save the account table that stores all the information on the users
		try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(ACCOUNT_TABLE_FILE_LOCATION))) {
			writer.writeObject(accountTable);
			writer.flush();
		} catch (FileNotFoundException e) {
			Report.error("The file '" + ACCOUNT_TABLE_FILE_LOCATION + "' could not be found");
		} catch (IOException e) {
			Report.error("An IO error occurred when trying to save the ClientTable ");
			e.printStackTrace();
		}

		// Save the counter used to generate unique codes
		try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(COUNTER_FILE_LOCATION))) {

			writer.writeObject(counter);
			writer.flush();

		} catch (FileNotFoundException e) {
			Report.error("The file '" + COUNTER_FILE_LOCATION + "' could not be found");
		} catch (IOException e) {
			Report.error("An IO error occurred when trying to save the counter ");
			e.printStackTrace();
		}

	}

	/**
	 * Read in any files containing a saved account table or counter file
	 */
	@SuppressWarnings("unchecked")
	public void read() {

		// Read in the saved account table file if it exists
		try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(ACCOUNT_TABLE_FILE_LOCATION))) {
			accountTable = (ConcurrentMap<String, Account<Message>>) reader.readObject();
		} catch (FileNotFoundException e) {
			Report.error("File '" + ACCOUNT_TABLE_FILE_LOCATION + "' not found, creating a new file");
		} catch (IOException e) {
			Report.error("An IO error occurred when trying to read the accountTable file");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Create a new clientWriter array for each read in account in the account table
		for (Account<Message> account : accountTable.values()) {
			account.createClientWriterArray();
		}

		// Read in the saved counter
		try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(COUNTER_FILE_LOCATION))) {
			counter = (Integer) reader.readObject();
		} catch (FileNotFoundException e) {
			Report.error("File '" + COUNTER_FILE_LOCATION + "' not found, creating a new file");
		} catch (IOException e) {
			Report.error("An IO error occurred when trying to read the counter file");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}
