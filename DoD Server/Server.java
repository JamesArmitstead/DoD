/*
 * This server class handles all of the client connections
 * including outputting to the clients and getting inputs
 * from the clients. When a client connects it gets a dedicated
 * thread which sned the appropriate output when and input
 * from a client is received.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

public class Server implements Runnable {
	
	private static int port = 0;
	private static ServerSocket serverSock;
	private int player = 0;
	private GameLogic logic;
	private Socket clientConnection;
	private BufferedReader clientReader;
	private PrintWriter clientWriter;
	private final static int MAX_CONNECTIONS = 10;
	
	/*
	 * Constructor for when a  new thread is created, each
	 * client gets a reader and writer as well as a player ID.
	 */
	public Server(Socket sock, int player, GameLogic logic) throws IOException {
		this.player = player;
		this.logic = logic;
		this.clientConnection = sock;
		this.clientReader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
		this.clientWriter = new PrintWriter(clientConnection.getOutputStream(), true);
	}

	/*
	 * This main method sets up the game and creates a new thread every time a
	 * player joins the server to handle their requests.
	 */
	public static void main(String args[]) throws IOException {
		
		setupServerSocket();
		
		// Get input for custom map name
		String mapName = JOptionPane.showInputDialog(null, "Please input a map name or leave "
				+ "blank for defualt map.", "Server Map Details", JOptionPane.PLAIN_MESSAGE);
		
		GameLogic logic = new GameLogic(port);
		
		logic.setMap(mapName);	
		
		int players = 0;
		
		// Loop while the server is running
		while (logic.isRunning() && players < MAX_CONNECTIONS) {
			Socket sock = serverSock.accept();
			// Create a new thread for the newly connected client
			Thread t1 = new Thread(new Server(sock, players, logic));
			t1.start();
			System.out.println("Player " + players + " connected.");
			players++;
		}
		serverSock.close();
	}
	
	/*
	 * This is the threaded code that handles the output to each client.
	 * Messages reagarding players connecting and disconnecting
	 * are printed to the server console window.
	 */
	public void run() {
		
		logic.setPlayerPosition(player);
		boolean winner = false;
		
		// This loop ends when the player disconnects via a return statement
		while (true) {
			String input = readFromClient();
			// The read method returns null if the client had closed the socket
			if (input == null) {
				logic.erasePlayer(player);
				System.out.println("Player " + player + " disconnected.");
				return;
			}
			
			// Decide what to output to client based on input
			String output = updatePlayer(input, player);
			
			// Output response to the client
			clientWriter.println(output);
			
			if(winner == false) {
				winner = logic.checkWin(player);
			}

			// Erase the player from the server after they have won
			if(input.contains("LOOK") && winner == true) {
				logic.erasePlayer(player);
				System.out.println("Player " + player + " has won.");
				System.out.println("Player " + player + " disconnected.");
				
				// Tell the client they have won
				try {
					clientWriter.println("WINNER");
					clientConnection.close();
					return;
					
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/*
	 * This method takes commands from clients and decides what operation
	 * needs to be performed.
	 */
	private String parseCommand(String inputCommand) {
		String[] command;
		
		// Protect against null pointer exception
		if (inputCommand != null) {
			command = inputCommand.trim().split(" ");
		} else {
			return "FAIL";
		}

		String answer = "";
		
		// Choose what to do based on the command received
		switch (command[0].toUpperCase()) {
			
		case "HELLO":
			answer = logic.hello(player);
			break;
			
		case "MOVE":
			// Check there exists a valid direction to move in
			if (command.length == 2) {
				if (command[1].toUpperCase().equals("N") || command[1].toUpperCase().equals("E")
						|| command[1].toUpperCase().equals("S") || command[1].toUpperCase().equals("W")) {
							
					answer = logic.move(command[1].toUpperCase().charAt(0), player);
				} else {
					answer = "FAIL";
				}
			} else {
				answer = "FAIL";
			}
			break;
			
		case "PICKUP":
			answer = logic.pickup(player);
			break;
			
		case "LOOK":
			answer = logic.look(player);
			break;
			
		default:
			answer = "FAIL";
			break;
		}
		return answer;
	}
	
	/*
	 * This method is called every time the server receives an input from a client.
	 * It decides what should be outputted back to the client and returns it .
	 */
	private String updatePlayer(String input, int player) {
		this.player = player;
		String answer = "";
		answer = parseCommand(input);
		return answer;
	}
	
	/*
	 * This method handles receiving inputs from a client it returns
	 * null if the client wants to quit or if the client program terminates.
	 * Otherwise it returns the string received from the client.
	 */
	private String readFromClient() {

		String input;

		try {
			input = clientReader.readLine();
			return input;
		} catch (IOException e) {
			return null;
		}
	}
	
	/*
	 * This method provides an input method for the server IP and port and
	 * sets up the server socket.
	 */
	private static void setupServerSocket() {
		
		boolean portValid = true;
		do {
			
			// Get port info from user
			String serverInfo = JOptionPane.showInputDialog(null, "Please input a port"
					, "Server Connection Details", JOptionPane.PLAIN_MESSAGE);
					
			if(serverInfo == null) {
				System.exit(-1);
			}
			
			// Check the port is in the valid port range
			try{
				port = Integer.parseInt(serverInfo);
				if(port > 65536 || port < 0) {
					showMSG("The port is invalid");
					portValid = false;
				}
				
			} catch(NumberFormatException e) {
				showMSG("The port is invalid");
				portValid = false;
			}
			
		} while(portValid == false);
		
		// Setup server socket
		try {
			serverSock = new ServerSocket(port);
		} catch (IOException e) {
			showMSG("Port already in use this server will be terminated");
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
