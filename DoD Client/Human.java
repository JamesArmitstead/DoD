/*
 * This class allows human players to connect to the server and
 * play the game. It has Multithreading to handle outputting and
 * inputting simultaneously. The communication between client and server uses
 * pre-defined keywords such as HELLO, MOVE, PICKUP and LOOK.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JOptionPane;

public class Human {
	
	private static Socket sock;
	private static BufferedReader serverReader;
	private static final String CONNECTION_ERROR_MSG = "Server Connection Error";
	private static final String PORT_ERROR_MSG = "The port is invalid please try again.";
	private static final String WINNER_MSG = "You have won the game the game will now exit.";
	
	/*
	 * The main method handles the reading of outputs from the server
	 * and it decides what to do based on those outputs.
	 */
	public static void main(String args[]) throws InterruptedException, IOException {
		
		setupConnection();
		
		// Create GUI which also handles output to the server.
		HumanGUI GUI = new HumanGUI(sock);
		
		// Loop forever as the game could run forever
		while (true) {
			String input = null;;
			try {
				input = serverReader.readLine();
				
			} catch (IOException e) {
				showMSG(CONNECTION_ERROR_MSG);
				System.exit(-1);
			}
			
			// If the input is null it means the server has terminated the connection
			if(input == null) {
				showMSG(CONNECTION_ERROR_MSG);
				System.exit(-1);
			}
			
			// Check to see if the server has sent a winner command
			else if(input.contains("WINNER")) {
				showMSG(WINNER_MSG);
				System.exit(-1);
			}
			
			// Check if it is a look window being sent from the server
			else if(input.contains("X")) {
				String[] lookWindow = new String[5];
				lookWindow[0] = input;
				
				// Read all 5 lines of the look window
				for(int i = 1; i < 5; i++) {
					lookWindow[i] = serverReader.readLine();
				}
				GUI.updateLook(lookWindow);
			}
				
			// Check if the server has sent a 'successful pickup' command
			else if(input.contains("GOLD")) {
				GUI.updateGold(input);
			}		
		}
	}
	
	/*
	 * This method sets up the client's connection to the server.
	 */
	private static void setupConnection() {
		
		// Variable used to keep track of if a connection is sucessful
		boolean connectionSuccess;
		
		// Loop until a sucessful connection is made
		do {
			connectionSuccess = true;
			
			// Get server info from user
			String serverInfo = JOptionPane.showInputDialog(null, "Please input the server IP and port"
					+ " in the form \"IP:Port\"", "Server Connection Details", JOptionPane.PLAIN_MESSAGE);
			
			// If the X button or cancel is clicked exit the program
			if(serverInfo == null) {
				System.exit(-1);
			}
			
			String[] serverInfoArray = serverInfo.split(":", 2);
			int port = 0;
			
			// Check that both the port and IP have been inputted
			if(serverInfoArray.length > 1) {
				
				// Check that the port inputted is valid
				try{
					port = Integer.parseInt(serverInfoArray[1]);
					
					if(port > 65536 || port < 0) {
						showMSG(PORT_ERROR_MSG);
						connectionSuccess = false;
					}
					
				} catch(NumberFormatException e) {
					showMSG(PORT_ERROR_MSG);
					connectionSuccess = false;
				}
			}
			
			else {
				showMSG("Please input a port number and IP address");
				connectionSuccess = false;
			}
			
			// After port validation try to connect to the server
			if(connectionSuccess == true) {
				
				try {
					sock = new Socket(serverInfoArray[0], port);
					
				} catch (IOException e) {
					showMSG(CONNECTION_ERROR_MSG);
					connectionSuccess = false;
				}
			}
			// Loop until a successful connection to the server is made
		} while(connectionSuccess == false);
		
		
		// Setup buffered reader to read from the server
		try {
			serverReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
		} catch (IOException e) {
			showMSG(CONNECTION_ERROR_MSG);
			System.exit(-1);
		}
	}
	
	/*
	 * This method shows error messages to the user.
	 */
	private static void showMSG(String errorMSG) {
		JOptionPane.showMessageDialog(null, errorMSG, null, JOptionPane.WARNING_MESSAGE);
	}
}
