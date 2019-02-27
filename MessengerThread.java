import java.io.IOException;

/**
 * The `MessengerThread` is a abstract class I made to be inherited by the
 * `ClientSender`, `ServerReceiver` and `ServerSender`, as they require the
 * block of `if` statements to check what command they need to respond to, they
 * also need the methods that pertain to what they should do if a particular
 * command is read. Because I didn't want this repeated chunk of code in all of
 * the while loops within these classes, I round them all up in the
 * `MessengerThread` class.
 * 
 * @author bxg796
 *
 */
public abstract class MessengerThread extends Thread {

	/**
	 * Takes a command and runs the corresponding method depending on which command
	 * was input.
	 * 
	 * @param command
	 *            The command to be run.
	 * @throws IOException
	 *             In the case that a command is listening for a response and it
	 *             loses connection, it will throw the IOException, but for the
	 *             current implementation this will only happen for the send method.
	 * @throws InterruptedException
	 *             This is thrown if one of the methods is waiting for a response
	 *             and is interrupted. For the current implementation this exception
	 *             is ignored and the threads just continue listening for input.
	 */
	protected void runCommand(String command) throws IOException, InterruptedException {

		if (command.equals(Command.SEND)) {
			send();
		}

		if (command.equals(Command.CURRENT)) {
			current();
		}

		if (command.equals(Command.PREVIOUS)) {
			previous();
		}

		if (command.equals(Command.NEXT)) {
			next();
		}

		if (command.equals(Command.DELETE)) {
			delete();
		}

		if (command.equals(Command.LOGOUT)) {
			logout();
		}

		if (command.equals(Command.HELP)) {
			help();
		}

		if (command.equals(Command.QUIT)) {
			quit();
		}
	}

	/**
	 * This contains the code for the send command should it be called in one of the
	 * implementing classes.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected abstract void send() throws IOException, InterruptedException;

	/**
	 * This contains the code for the current command should it be called in one of
	 * the implementing classes.
	 */
	protected abstract void current();

	/**
	 * This contains the code for the previous command should it be called in one of
	 * the implementing classes.
	 */
	protected abstract void previous();

	/**
	 * This contains the code for the next command should it be called in one of the
	 * implementing classes.
	 */
	protected abstract void next();

	/**
	 * This contains the code for the delete command should it be called in one of
	 * the implementing classes.
	 */
	protected abstract void delete();

	/**
	 * This contains the code for the logout command should it be called in one of
	 * the implementing classes.
	 */
	protected abstract void logout();

	/**
	 * This contains the code for the help command should it be called in one of
	 * the implementing classes.
	 */
	protected abstract void help();

	/**
	 * This contains the code for the quit command should it be called in one of the
	 * implementing classes.
	 */
	protected abstract void quit();

}