// This is to handle logging of normal and erroneous behaviours.

public class Report {

	public static void behaviour(String message) {
		System.err.println(message);
	}

	public static void error(String message) {
		System.err.println(message);
	}

	public static void errorAndGiveUp(String message) {
		Report.error(message);
		System.exit(1);
	}
}
