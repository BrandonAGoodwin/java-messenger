https://git.cs.bham.ac.uk/bxg796/SWW-exercises/tree/master/realistic-messenger

# Solution

# Initial Idea
* Overhaul the system to expect a command prior to any action.
* Create a `ServerLogInThread` class so that the server could sort out logging people in on a different thread, instead of the sever being taken up trying to log in users and disabling other users from connecting until the previous user is finished logging in.
* Deal with people being logged in already (May be this should be allowed - if so messages need to be sent to all places where the client is logged in)

# Consistencies
* If the user correctly inputs a command and then is required to input additional information and that additional information leads to an error, the user will have to re-input the command, not just immediately input the additional information again.
* The file name for saving the accountTable is a set value 

# Extra Features
* Passwords
* Password Encryption 
* Hidden Password Input (WIP)
* Current Message Command
* GUI [**NOTE:** Type in "toggle gui" in the current input area (GUI or command prompt) to swap from one to the other.]

# Creation Process
Decisions made:
* Register doesn't automatically log in
* Allowed multiple logins
* Delete goes to previous message if any

### Dealing With Clients while Not Logged In
* First I made a `ServerLogInThread` class which took away almost all of the responsibility for setting up a user from the `Server` loop. I made it a thread so that the `Server` could continue looking for incoming connections and clients, and so that multiple clients could be in this "pre-logged in" state where no messages can be send. It listened for commands from the `Client`. Which now also has a loop which it would only exit on a `QUIT` command. Communication would take place between the `Client` and the `ServerLogInThread` until the user successfully logs in (in which case the `ClientSender`, `ClientReceiver`, `ServerSender` and `ServerReceiver` would be started and take control).
* If the user logs out, then the `ClientSender`, `ClientReceiver`, `ServerSender` and `ServerReceiver` threads are closed, and the while loops within the `Client` and `ServerLogInThread` continue, waiting for a `REGISTER`, `LOGIN` or `QUIT` command.
* If the user quits then the `ClientSender`, `ServerReciever`, `ServerSender`, `ClientReceiver`, `Client`, `ServerLogInThread` all end in that order.

### Updated Command Class
* In addition to the new commands in the `Command` class, I added all the commands to a `ArrayList` and made some public methods to increase ease of use. I added a `isCommand(String input)` method that returns a boolean value if the input is the same as one of the commands, this is used to check that a command input is valid and that inputs for a user `nickname` are the same as one of the commands.
* Next I needed to make it so that users had to use  the `SEND` command before sending a message which just went in line with the `ClientSender` waiting to receive a command at the beginning of the loop, and then depending on which command was input action would be taken on a case by case basis.

## Implementing Commands
* All the main commands can be divided into 4 categories.
* Commands that are used while logged in: `SEND`, `CURRENT`, `PREVIOUS`, `NEXT`, `DELETE` and `LOGOUT`.
* Commands that are used while not logged in: `REGISTER` and `LOGIN`.
* Global commands that can be used while logged in or logged out: `QUIT` and `TOGGLE_GUI`
* And verification commands: `ALLOW` and `BLOCK`.

[**Note:** Empty inputs are not read in by the GUI and all commands are checked to be valid before being processed. If I command is valid but has no function nothing happens.]

### MessengerThread
* The `MessengerThread` is a abstract class I made to be inherited by the `ClientSender`, `ServerReceiver` and `ServerSender`, as they require the block of `if` statements to check what command they need to respond to, they also need the methods that pertain to what they should do if a particular command is read. Because I didn't want this repeated chunk of code in all of the while loops within these classes, I round them all up in the `MessengerThread` class.
* This class contains a `runCommand(String command)` method that checks the input message and runs the appropriate method, and a method for each command to be overridden.

### Command: QUIT
* Then I added the `QUIT` command which works similarly to the `LOGOUT` command, but can be used when the user isn't logged in, and it will close down the client thread and the client command prompt / GUI.
* The when the `QUIT` command is sent, before the client has logged in or after they have logged out, it sets the `quitting` variable in the `Client` class to true and at the bottom of the while loop listening for commands the `ClientUI` is disposed, the `QUIT` command is sent to the `ServerLogInThread`, all the data streams are closed and the `Client` breaks out of the while loop.

![alt text](https://i.gyazo.com/212c17fffa92f0d896eb3e08bb157cc8.png)
* Once the `ServerLogInThread` receives the quit command, is closes the steams on it's end and closes the socket, then breaks out of the while loop.

![alt text](https://i.gyazo.com/216cdba17560a3f636e5d6e4f1ea6b8c.png)
* When `QUIT` is called while a user is logged in, the `QUIT` command is propagated through out the listeners, starting in the `ClientSender` and then moving to the `ServerReceiver`, `ServerSender` and `ClientReceiver` in that order, breaking out of each loop as is goes closing each thread. Then a final message is sent from the `Client` to the `ServerLoginThread` telling them both to end just like when `QUIT` is called when not logged in.

### Command: LOGOUT 
* Then I implemented the `LOGOUT` command which let users log out after logging in. I did this by making it so in the `ServerSender`, `LOGOUT` (and `QUIT`) are sent to the `Client` twice. The first one lets the `ClientReceiver` know that is should break out of the loop and, the second tells the `Client` whether or not it should close all the streams and break out of the loop when `QUIT` is sent, or just continue the loop waiting for a `REGISTER`, `LOGIN` or `QUIT` command when `LOGOUT` is sent. This is similar to what happens in the `ServerReceiver` to let the `ServerLogInThread` if it should exit the loop (`QUIT`) or continue listening for commands (`LOGOUT`).

### Command: REGISTER
* When the `REGISTER` command is called whilst not logged in, the user is prompted by the `Client` to input a user name, before forwarding this message to the `ServerLogInThread` it is checked that it isn't the same as an existing command or empty. I understand that this won't be a technical issue anymore as the send command must be send before specifying the recipients name, but I thought it would reduce confusion and making mistakes when inputting commands and sending messages.
* If it isn't an existing command then the `REGISTER` command is forwarded to the `ServerLogInThread` where it is checked against clientNames in the `clientTable` to check the nickname isn't in use (using the `ClientTable`'s `contains` method). If there already exists a user with the same nickname, the `BLOCK` command is sent to the `Client`, which then tells the user that the nickname is already in use. Otherwise, the `ALLOW` command is send to the `Client` and the user is prompted to input a password. Once the password is input, it is sent to the `ServerLogInThread` and the account is successfully registered.
* I decided that after registering the user wouldn't be immediately logged in, this just gives the user more freedom to either register more accounts, log in to a different account, quit, what ever they please.
* The command does nothing if the user is currently logged in.

### Command: LOGIN
* When the `LOGIN` command is called whilst not logged in, the user is prompted by the `Client` to input a user name.
* This `LOGIN` command and user name are sent to the `ServerLogInThread` which checks that the user name is contained within the `clientTable`.
* If it isn't a `BLOCK` command is sent to the `Client` and the user is told that a user with that name does not exist.
* Otherwise, an `ALLOW` command is sent to the `Client` at which point the user is prompted to input the password for the account,
* If the password does not match with password stored in the account on the server, then a `BLOCK` message is sent to the `Client` and the user is told that they input an incorrect password.
* Otherwise, they are told they have successfully logged in and the `ClientSender`, `ServerReceiver`, `ServerSender` and `ClientReceiver` threads are started. And finally the last message they received, and will receive any messages that were sent to them when they were not logged in.

### Command: SEND
* When the `SEND` command is input while the user is logged in, they are prompted to input the recipients name, then they are prompted to input the message.
* All these inputs as well as the `SEND` command are sent to the `ServerReceiver` (from the `ClientSender`) where the `ServerReceiver` turns the inputs into a `Message` and adds the message into the recipient's BlockingQueue.

#### Dealing With Multiple Simultaneous Logged in Users
* To ensure that messages are sent to all clients logged in to the recipients accounts, all the `clientWriters` (`PrintStreams`) that are associated with a client that is logged into a specific account, are added to an ArrayList of these `clientWriters` stored within the `Account` that the users are logged into.
* Then whenever a user receives a message, it is sent to all `clientWriters` for clients logged into the same `Account`.

* To make sure commands that are sent by a particular user aren't sent to all other logged in clients or just a random client, a `uniqueCode` is generated (by the `ClientTable`) and given to each `ServerReceiver` and `ServerSender` pair that are created. This code is added to any command message received by the `ServerReceiver` that is only meant to send a response to the client that sent it. Now when the un-received messages queue is checked by the `ServerSender`, if the `uniqueCode` attached to the `Message` isn't the same as the the `uniqueCode` stored in the `ServerSender` it isn't removed and acted on (unless the command attached is a `SEND` command). This acting similar way to a label on a packe	t being send through a network.
* The `Message` class now has two constructors: `Message(String sender, String text, String command)` and `Message(String command, String uniqueCode)` one for sending messages to other users, one for sending commands to the server.

### Command: CURRENT
* The `CURRENT` command returns the `current` message, which is the set to the message received when a new message is received or the current message being looked at after performing a `PREVIOUS`, `NEXT` or `DELETE` command.
* A `messagePointer` variable in the `ServerSender` keeps track of what is the `current` message, which points to the location of the current message in the `receivedMessages` ArrayList inside the `ServerSender`.
* If there is no `current` message then a message is sent to the `ClientReceiver` telling the user that there are no messages.
* Each logged in user has their own `messagePointer`.

### Command: PREVIOUS
* The `PREVIOUS` command returns the message before the `current` message, and sets this new message to the `current` message by decrementing the `messagePointer`.
* If there is no message before the `current` message or no messages at all a message is sent telling the user there are no previous messages.

### Command: NEXT
* The `NEXT` command returns the message after the `current` message, and sets this new message to the `current` message
by incrementing the `messagePointer`.
* If there is no message after the `current` message or no messages at all a message is sent telling the user is no next message.

### Command: DELETE
* The `DELETE` command deletes the `current` message and sets the current message to the previous message by decrementing the `messagePointer`.
* If there are no messages to delete then a message is sent telling the user there are no messages to delete.

### Command: TOGGLE_GUI
* The `TOGGLE_GUI` command toggles the interface between the command line and the javax.swing window, when the user types in "toggle gui".

## Storing Received Messages
* When a message is received (removed from the `unreceivedMessagesQueue` (BlockingQueue)) and is added to the users `receivedMessages` ArrayList which is stored in the users `Account`.
* Sent commands are not stored.

## Account Class
* The `Account` class holds all the data (`Message`s, `PrintStream`s and Passwords) for each user account that is registered. These are stored in the server's `ClientTable` using a `ConcurrentHashMap` and are saved in a file to be retrieved when the server is turned off and turned back on again.
* It also holds the `hash(String input)` method that hashes the stored `password` in an unreadable form. 

## Saving Accounts to Files
* Saving is done in the `ClientTable`, by using a `ObjectOutputStream` to serialise the `accountTable` that stores all the account information for each registered user, and is then read back in using a `ObjectInputStream`.
* Then after reading a file back in, I run a `createClientWriterArray()`, a method in the `Account` class that creates a new `ClientArray` for the `clientWriters` (`PrintStreams`) for clients logged into this account.
* The `counter` that generates the `uniqueCode`s in the `ClientTable` is also saved to make sure that future codes generated are also unique to those previously made.

## Passwords
* Passwords are set when an user registers an account and a password is required to make an account.
* When the password is entered, it turned into a byteDigest using an MD5 message digestion class, so that the actual string password isn't stored on the `Server` or `accountTable` file (low level protection).
* When a user wants to login they have to input the password for that account, this input is hashed and compared to the hashed byte array in the account, if the two byte arrays are equal the passwords input are the same and the user is allowed to log into that account.

## GUI (ClientUI)
* To activate the GUI the user must input "toggle gui" into the command prompt and to return to command prompt mode, they must type "toggle gui" in the GUI.
* The client initially starts with the GUI hidden and input taken in the Command Prompt and can swap at any time to change between the gui and command prompt.

* This is how I deal with input from the gui and command prompt:
* Listening for input in the text field:

![alt text](https://i.gyazo.com/6bd6216fc01d21d5d797f557cab254c7.png)
* Reading the user input from the command prompt or GUI text field:

![alt text](https://i.gyazo.com/c80d57b7253b4534e707a8aa120e0ce4.png)
* Swapping between GUI mode and CLI (Command line interface) mode:

![alt text](https://i.gyazo.com/4b299e1c1a69dba1767866b66fb04122.png)
* The GUI implemented creates a scrollable `JTextArea`, where all the messages from the Server/other users or prompts from the client are displayed.
* I also added the time and date that a server receives a message to the message.
* Beneath this text area is a `JTextField`, where the user can input information for the client to read. This text where I previously used `(BufferedReader) user.readLine()` to read input from the console, I instead wait for the user to input a value into the text field and press enter, using `wait()` and `interrupt()` in the `ClientUI` class or if GUI mode is set to false then it uses the BufferedReader in the `ClientUI` class.
* I also made it so that the user could see messages that they had sent in the log.
