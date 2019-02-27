import java.util.ArrayList;

/**
 * The Command class stores all the strings that are commands which are used to
 * send messages to Server to let it know what the client wants or what values
 * it should expect to receive from the user.
 * 
 * @author bxg796
 *
 */
public class Command {

	/**
	 * Stores all the existing commands in an array list that can be iterated
	 * through to make comparison methods easier to execute.
	 */
	private static ArrayList<String> commands = new ArrayList<String>();

	/**
	 * Quit command.
	 */
	public static final String QUIT = addCommand("quit");
	/**
	 * Block command.
	 */
	public static final String BLOCK = addCommand("block");
	/**
	 * Allow command.
	 */
	public static final String ALLOW = addCommand("allow");
	/**
	 * Register command.
	 */
	public static final String REGISTER = addCommand("register");
	/**
	 * Login command.
	 */
	public static final String LOGIN = addCommand("login");
	/**
	 * Logout command.
	 */
	public static final String LOGOUT = addCommand("logout");
	/**
	 * Send command.
	 */
	public static final String SEND = addCommand("send");
	/**
	 * Current command.
	 */
	public static final String CURRENT = addCommand("current");
	/**
	 * Previous command.
	 */
	public static final String PREVIOUS = addCommand("previous");
	/** 
	 * Next command.
	 */
	public static final String NEXT = addCommand("next");
	/**
	 * Delete command.
	 */
	public static final String DELETE = addCommand("delete");
	/**
	 * Toggle gui command.
	 */
	public static final String TOGGLE_GUI = addCommand("toggle gui");
	/**
	 * Help command.
	 */
	public static final String HELP = addCommand("help");

	/**
	 * Add a command to the command ArrayList.
	 * @param command The command to be added to the ArrayList.
	 * @return
	 */
	private static String addCommand(String command) {
		commands.add(command);
		return command;
	}

	public static boolean isCommand(String input) {
		for (String command : commands) {
			if (input.equals(command)) {
				return true;
			}
		}
		return false;
	}

}
