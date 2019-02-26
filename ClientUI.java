import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;

public class ClientUI extends JFrame {

	private static final long serialVersionUID = 8075642296734150448L;

	private JPanel textPanel;

	private JTextArea textArea;

	private JScrollPane scrollPane;

	private JPanel inputPanel;

	private JTextField textField;

	private String userInput;

	private Object holder;

	private BufferedReader user;

	private boolean guiMode;

	public ClientUI(String frameTitle, Object holder) {
		this.holder = holder;
		user = new BufferedReader(new InputStreamReader(System.in));
		guiMode = false;

		// frameTitle = nickname + "'s Messages";
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setPreferredSize(new Dimension(500, 520));
		setResizable(false);
		setTitle(frameTitle);
		setVisible(false);

		textPanel = new JPanel(new FlowLayout());
		// textPanel.setLayout(new BorderLayout());
		textPanel.setPreferredSize(new Dimension(500, 500));

		textArea = new JTextArea(30, 44);

		textArea.setLineWrap(true);
		textArea.setEditable(false);

		scrollPane = new JScrollPane(textArea);

		// scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		textPanel.add(scrollPane);

		add(textPanel, BorderLayout.CENTER);

		inputPanel = new JPanel(new FlowLayout());
		// inputPanel.setPreferredSize(new Dimension(500, 50));

		textField = new JTextField(44);
		// textField.setPreferredSize(new Dimension(500, 0));

		textField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (holder) {
					// Get the text in the textField
					userInput = textField.getText();

					// Clear the text field
					textField.setText(null);

					// Notify the holder that input as been entered to allow the code to continue
					holder.notify();
				}
			}
		});

		inputPanel.add(textField/* , BorderLayout.SOUTH */);

		add(inputPanel, BorderLayout.SOUTH);

		pack();

	}

	/**
	 * Print a message to the text area and moving to the next line.
	 * 
	 * @param message
	 *            The message to be printed to the text area.
	 */
	public void tell(String message) {
		System.out.println(message);
		textArea.append(message);
		textArea.append("\n");
	}

	/**
	 * Prints an error to the text area using the tell() method and prints out the
	 * error to the console.
	 * 
	 * @param message
	 *            The message to be printed to the text area and console.
	 */
	public void error(String message) {
		// Report.error(message);
		tell(message);
	}

	/**
	 * Reads the value in the text field after a user presses enter.
	 * 
	 * @return The user input in the text field after the user presses enter.
	 * @throws InterruptedException
	 *             This is thrown when the method is interrupted while waiting for
	 *             user input.
	 * @throws IOException
	 */
	public String readLine() throws InterruptedException, IOException {
		synchronized (holder) {
			// If the guiMode is true it looks for user input from the textField
			if (guiMode == true) {
				while (userInput == null || userInput.isEmpty()) {
					// Wait for the user to input something
					holder.wait();
				}
			} else {
				// Otherwise it looks for input from the console
				userInput = user.readLine();
			}

			if (userInput.equals(Command.TOGGLE_GUI)) {
				toggleGUIMode();
			}

			String output = userInput;
			// Set the userInput to null to reset it
			userInput = null;
			return output;
		}
	}

	/**
	 * Toggle the guiMode variable and set the visibility of the clientUI to equal
	 * the guiMode.
	 */
	private void toggleGUIMode() {
		if (guiMode == true) {
			guiMode = false;
		} else {
			guiMode = true;
		}
		setVisible(guiMode);
	}

}
