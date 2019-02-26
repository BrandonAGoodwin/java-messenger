import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Account class holds all the data (Messages, PrintStreams and Passwords)
 * for each user account that is registered. These are stored in the server's
 * client table and are saved in a file to be retrieved when the server is turned
 * off and turned back on again.
 * 
 * @author bxg796
 *
 * @param <E>
 *            The type of message that the account will store, for the purpose
 *            of the assignment this will only ever be a "Message".
 */
public class Account<E> implements Serializable {

	/**
	 * The serial code for this class.
	 */
	static final long serialVersionUID = -1466934980840921417L;

	/**
	 * The hashed password value
	 */
	private final byte[] hashedPassword;

	/**
	 * Store the BlockingQueue which hold all the unsent messages for this accounts
	 */
	private BlockingQueue<E> unrecievedMessageQueue = new LinkedBlockingQueue<E>();

	/**
	 * An array list that stores all the recieved messages for this account
	 */
	private ArrayList<E> receivedMessages = new ArrayList<E>();

	/**
	 * Store the PrintStreams of clients logged in for a particular user.
	 */
	private transient ArrayList<PrintStream> clientPrintStreams;

	/**
	 * Constructor Creates an account with the specified password.
	 * 
	 * @param password
	 *            The password to be hashed and stored.
	 */
	public Account(String password) {
		createClientWriterArray();
		hashedPassword = hash(password);
	}

	/**
	 * Checks if the input value is the password for this account.
	 * 
	 * @param input
	 *            The input to be hashed and checked against the hashed password.
	 * @return True if the input is equal to the password or false otherwise.
	 */
	public boolean isPassword(String input) {
		if (Arrays.equals(hash(input), hashedPassword)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Takes an input and returns the hashed/digested byte array.
	 * 
	 * @param input
	 *            The input to be hashed.
	 * @return The byte array of the hashed input.
	 */
	private byte[] hash(String input) {
		byte[] hashedInput = null;
		try {
			byte[] inputBytes = input.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			hashedInput = md.digest(inputBytes);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return hashedInput;
	}

	/**
	 * Get the BlockingQueue that stores all the unreceived messages for this
	 * account.
	 * 
	 * @return The BlockingQueue that stores all the unreceived messages for this
	 *         account.
	 */
	public BlockingQueue<E> getPendingMessageQueue() {
		return unrecievedMessageQueue;
	}

	/**
	 * Get the ArrayList that stores all the received messages on this account.
	 * 
	 * @return The ArrayList that stores all the received messages on this account.
	 */
	public ArrayList<E> getReceivedMessages() {
		return receivedMessages;
	}

	/**
	 * Create a new clientWritter array, to be used when the object is constructed
	 * or read in from a file, as the ArrayList of PrintStreams is not saved.
	 */
	public void createClientWriterArray() {
		clientPrintStreams = new ArrayList<PrintStream>();
	}

	/**
	 * Get the ArrayList of clientWriters (PrintStreams) currently associated with
	 * this account.
	 * 
	 * @return The ArrayList of clientWriters (PrintStreams) currently associated
	 *         with this account.
	 * 
	 */
	public ArrayList<PrintStream> getClientWriters() {
		return clientPrintStreams;
	}

	/**
	 * Remove the specified writer from the ArrayList of clientWriters
	 * (PrintStreams) currently associated with this account.
	 * 
	 * @param writer
	 *            The writer to be removed from the ArrayList of clientWriters.
	 */
	public void removeClientWriter(PrintStream writer) {
		clientPrintStreams.remove(writer);
	}

}
