import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Message implements Serializable {

	private static final long serialVersionUID = 3145245600108951549L;
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM");

	private final String sender;
	private final String text;
	private final String command;
	private final String uniqueCode;
	private final String timeSent;

	Message(String sender, String text, String command) {
		this.sender = sender;
		this.text = text;
		this.command = command;
		uniqueCode = "COMMON";
		timeSent = getTime();
	}

	Message(String command, String uniqueCode) {
		this.command = command;
		this.uniqueCode = uniqueCode;
		sender = null;
		text = null;
		timeSent = getTime();
	}

	public String getSender() {
		return sender;
	}

	public String getText() {
		return text;
	}

	public String getCommand() {
		return command;
	}

	public String getUniqueCode() {
		return uniqueCode;
	}

	private String getTime() {
		Calendar cal = Calendar.getInstance();
		String time = timeFormat.format(cal.getTime());
		String date = dateFormat.format(cal.getTime());
		return (date + " | " + time);
	}

	public String toString() {
		return "[" + timeSent + "] From " + sender + ": " + text;
	}
}
