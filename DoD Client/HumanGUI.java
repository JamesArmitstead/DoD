/*
 * This class creates a GUI that can be used to control a player
 * in the dungeon. This class also outputs the players commands
 * to the server and shows the server's response.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;

public class HumanGUI {
	
	PrintWriter serverWriter = null;
	String[] lookWindow = null;
	int goldRequired = -1;
	int goldPickedUp = 0;
	private boolean firstTime = true;
	private static final String[] COMMANDS = {"MOVE N", "MOVE W", "PICKUP", "MOVE E", "MOVE S", "LOOK", "HELLO"};
	JPanel mapPanel = new JPanel();
	JProgressBar goldProgress = new JProgressBar();
	
	/*
	 * This constructor sets up the print writer and creates a new thread
	 * to handle creating the GUI.
	 */
	public HumanGUI(Socket sock) {
		
		try {
			this.serverWriter = new PrintWriter(sock.getOutputStream(), true);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Connection to server was lost the game will now exit."
					,"Server Connection Error",JOptionPane.WARNING_MESSAGE);
			System.exit(-1);
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUI();
			}
		});
	}
	
	/*
	 * This method creates the GUI and all the components used to control the player and
	 * shows the data received from the server.
	 */
	private void createGUI() {
		
		// Send LOOK and HELLO commands to setup the GUI look window and gold progress bar
		outputToServer(COMMANDS[6]);
		outputToServer(COMMANDS[5]);
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
		JFrame frame = new JFrame("Dungeon of Doom");
		frame.setPreferredSize(new Dimension(500,718));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		frame.setLayout(flowLayout);
		
		// Map panel is a global variable as other methods need to access it
		mapPanel.setPreferredSize(new Dimension(500,500));
		mapPanel.setLayout(flowLayout);
		
		// Give the server time to respond then update the map
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		updateMap();
		
		// Create panels to fill empty space
		JPanel vertPadPanel = new JPanel();
		vertPadPanel.setPreferredSize(new Dimension(500,5));
		JPanel horizPadPanel = new JPanel();
		horizPadPanel.setPreferredSize(new Dimension(20,180));
		
		GridLayout gridLayout = new GridLayout(0, 3);
		JPanel commandPanel = new JPanel();
		commandPanel.setPreferredSize(new Dimension(180,180));		
		commandPanel.setLayout(gridLayout);
		
		// Create 9 components to create a 3x3 grid with 5 buttons and 4 empty spaces
		ArrayList<JButton> commandButtons = new ArrayList<JButton>();
		int buttonCounter = 0;
		JButton button;
		JLabel fillerLabel;
		for(int i = 0; i < 9; i++) {
			if(i == 0 || i == 2 || i == 6 || i== 8) {
				fillerLabel = new JLabel();
				fillerLabel.setPreferredSize(new Dimension(55,55));
				commandPanel.add(fillerLabel);
			}
			else {
				button = new JButton();
				button.setPreferredSize(new Dimension(55, 55));
				button.setMargin(new Insets(0, 0, 0, 0));
				button.setFont(new Font("Arial", Font.PLAIN, 12));
				button.setText(COMMANDS[buttonCounter]);
				commandButtons.add(button);
				commandPanel.add(button);
				buttonCounter++;
			}
		}
		
		// Send MOVE NORTH command and a LOOK command after
		commandButtons.get(0).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputToServer(COMMANDS[0]);
				outputToServer(COMMANDS[5]);
				updateMap();
			}
		});
		
		// Send MOVE WEST command and a LOOK command after
		commandButtons.get(1).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputToServer(COMMANDS[1]);
				outputToServer(COMMANDS[5]);
				updateMap();
			}
		});
		
		// Send PICKUP command to server
		commandButtons.get(2).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputToServer(COMMANDS[2]);
			}
		});
		
		// Send MOVE EAST command and a LOOK command after
		commandButtons.get(3).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputToServer(COMMANDS[3]);
				outputToServer(COMMANDS[5]);
				updateMap();
			}
		});
		
		// Send MOVE SOUTH command and a LOOK command after
		commandButtons.get(4).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputToServer(COMMANDS[4]);
				outputToServer(COMMANDS[5]);
				updateMap();
			}
		});
		
		
		FlowLayout altFlowLayout = new FlowLayout(FlowLayout.LEFT, 0, 5);
		
		JPanel altHorizPadPanel = new JPanel();
		altHorizPadPanel.setPreferredSize(new Dimension(33,180));		
		
		// Setup panel to display current gold collection progress 
		JPanel objectivePanel = new JPanel();
		objectivePanel.setPreferredSize(new Dimension(240,180));
		objectivePanel.setLayout(altFlowLayout);
		
		JTextArea progressBarText = new JTextArea("\nThis bar shows how much gold you need\n"
				+ "          to collect to exit the dungeon.\n");
		progressBarText.setOpaque(false);
		
		objectivePanel.add(progressBarText);
		
		goldProgress.setPreferredSize(new Dimension(226,30));
		goldProgress.setStringPainted(true);
		goldProgress.setForeground(Color.BLUE);
		goldProgress.setValue(0);
		objectivePanel.add(goldProgress);
		
		// Create label to pad empty space
		JLabel paddingLabel = new JLabel();
		paddingLabel.setPreferredSize(new Dimension(240, 10));
		objectivePanel.add(paddingLabel);
		
		JButton quitButton = new JButton("Quit");
		quitButton.setMargin(new Insets(0, 0, 0, 0));
		quitButton.setFont(new Font("Arial", Font.PLAIN, 18));
		quitButton.setPreferredSize(new Dimension(226,55));
		objectivePanel.add(quitButton);
		
		
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(-1);
			}
		});
		
		// Add all the panels to the frame
		frame.add(mapPanel);
		frame.add(vertPadPanel);
		frame.add(horizPadPanel);
		frame.add(commandPanel);
		frame.add(altHorizPadPanel);
		frame.add(objectivePanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		
	}
	
	/*
	 * This method updates the map everytime the server sends the response
	 * to a LOOK command.
	 */
	private void updateMap() {
		
		// If running for first time an exception will occur as mapPanel is empty
		if(firstTime != true) {
			mapPanel.removeAll();
		}
		firstTime = false;
		
		ImageIcon icon = null;
		
		// Fill 5x5 grid with pictures representing tiles
		for(int i = 0; i < 25; i++) {
			switch (lookWindow[i / 5].charAt(i % 5)) {
			case 'X':
				icon = new ImageIcon("Wall.png");
				break;
				
			case '#':
				icon = new ImageIcon("Wall.png");
				break;	
				
			case '.':
				icon = new ImageIcon("Floor.png");
				break;
				
			case 'P':
				icon = new ImageIcon("Player.png");
				break;
				
			case 'G':
				icon = new ImageIcon("Gold.png");
				break;
				
			case 'E':
				icon = new ImageIcon("Exit.png");
				break;
				
			}
			mapPanel.add(new JLabel(icon));
		}
		mapPanel.revalidate();
		mapPanel.repaint();
	}
	
	/*
	 * This method updates the gold progress bar when a player picks up gold.
	 * It also sets the amount of gold needed when the player connects to the server.
	 */
	public void updateGold(String goldInput) {
		
		// First time this method is run read in the number of gold required
		if(goldRequired == -1) {
			goldRequired = Integer.parseInt(goldInput.substring(6));
			if(goldRequired == 0) {
				goldProgress.setValue(100);
			}
		}
		
		else {
			goldPickedUp++;
			// Protect against divide by 0 error
			if(goldProgress.getValue() != 100 && goldRequired != 0) {
				goldProgress.setValue((100 * goldPickedUp) / goldRequired);
			}
		}
	}
	
	/*
	 * This method is used to set the look output from the server.
	 */
	public void updateLook(String[] lookWindow) {
		this.lookWindow = lookWindow;
	}
	
	/*
	 * This method outputs commands to the server and then waits to give
	 * the server time to respond before displaying the output.
	 */
	private void outputToServer(String output) {
		serverWriter.println(output);

		try {
			Thread.sleep(35);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
