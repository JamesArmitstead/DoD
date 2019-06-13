/*
 * This class handles the server world GUI which shows the whole map and
 * all the players moving in real time. The GUI also displays the server's
 * IP address and port.
 */

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.*;

public class ServerGUI {
	
	JPanel mapPanel = new JPanel();
	char[][] map;
	boolean firstTime = true;
	int mapWidth;
	int mapLength;
	int port;
	
	/*
	 * This constructor creates a new thread to create the GUI adn also stores
	 * the map and port number.
	 */
	public ServerGUI(char[][] map, int port) {
		this.port = port;
		this.map = map;
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUI();
			}
		});
	}
	
	/*
	 * This method creates the server GUI and all of the components on it.
	 * There is a scrollable pane which allows maps of any size to be
	 * viewed. There is also a server shutdown button and a text area to
	 * display the server's IP and port number.
	 */
	private void createGUI() {
		
		mapWidth = map[0].length;
		mapLength = map.length;
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);

		JFrame frame = new JFrame("Dungeon of Doom");
		
		// Setup frame and scroll pane
		frame.setPreferredSize(new Dimension(973,600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		frame.setLayout(flowLayout);
		
		JScrollPane mapScrollPane = new JScrollPane();
		mapScrollPane.getViewport().setPreferredSize(new Dimension((mapWidth * 50), mapLength * 50));
		mapScrollPane.setPreferredSize(new Dimension(968,468));
		mapScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mapScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		mapPanel.setLayout(flowLayout);
		mapPanel.setPreferredSize(new Dimension(mapWidth * 50, mapLength * 50));
		mapPanel = createMapPanel();
		mapScrollPane.getViewport().add(mapPanel);
		
		FlowLayout altFlowLayout = new FlowLayout(FlowLayout.LEFT, 20, 15);
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(altFlowLayout);
		
		// Get IP address of server
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		JTextArea serverIPText = new JTextArea("Server IP: " + ip.getHostAddress() + ":" + String.valueOf(port));
		serverIPText.setOpaque(false);
		serverIPText.setEditable(false);
		serverIPText.setFont(new Font("Arial", Font.PLAIN, 18));
		settingsPanel.add(serverIPText);
		
		// Create shutdown button and attach an action listener
		JButton shutdownButton = new JButton("Shutdown Server");
		shutdownButton.setPreferredSize(new Dimension(630,75));
		shutdownButton.setMargin(new Insets(0, 0, 0, 0));
		shutdownButton.setFont(new Font("Arial", Font.PLAIN, 18));
		shutdownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(-1);
			}
		});
		settingsPanel.add(shutdownButton);
		
		// Add all the componenets to the frame
		frame.add(mapScrollPane);
		frame.add(settingsPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	/*
	 * This method creates the map view filling it with pictures that
	 * represent map tiles.
	 */
	private JPanel createMapPanel () {
		
		ImageIcon icon = null;
		for(int i = 0; i < mapWidth * mapLength; i++) {
			
			switch (map[i / mapWidth][i % mapWidth]) {
				
			case 'X':
				icon = new ImageIcon("Wall.png");
				break;
				
			case '#':
				icon = new ImageIcon("Wall.png");
				break;	
				
			case '.':
				icon = new ImageIcon("Floor.png");
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
		return mapPanel;
	}
	
	/*
	 * This method is used when a player moves on the server. It updates
	 * the server GUI to show the players new position.
	 */
	public void movePlayer(int[] oldPosition, int[] newPosition ) {
		erasePlayer(oldPosition);
		placePlayer(newPosition);
		mapPanel.revalidate();
		mapPanel.repaint();	
	}
	
	/*
	 * This method updates the server GUI when a plyer moves off a tile or
	 * quits the server. It replaces the player tile with the map tile
	 * they were stood on e.g. gold.
	 */
	public void erasePlayer(int[] playerPosition){
		
		// Use players postion to figure out which tile to change
		mapPanel.remove(playerPosition[0] * mapWidth + playerPosition[1]);
		
		// Select which tile needs to replace the player
		switch(map[playerPosition[0]][playerPosition[1]]) {
			
		case '.':
			mapPanel.add(new JLabel(new ImageIcon("Floor.png")), playerPosition[0] * mapWidth + playerPosition[1]);
			break;
			
		case 'G':
			mapPanel.add(new JLabel(new ImageIcon("Gold.png")), playerPosition[0] * mapWidth + playerPosition[1]);
			break;
			
		case 'E':
			mapPanel.add(new JLabel(new ImageIcon("Exit.png")), playerPosition[0] * mapWidth + playerPosition[1]);
			break;
		}
		mapPanel.revalidate();
		mapPanel.repaint();	
	}
	
	/*
	 * This method updates the server GUI when a player moves onto a tile
	 * or spawns onto a tile. The tile the player is stood on is replaced
	 * with a player tile.
	 */
	public void placePlayer(int [] playerPosition){
		
		// Use players postion to calculate which tile to change
		mapPanel.remove(playerPosition[0] * mapWidth + playerPosition[1]);
		mapPanel.add(new JLabel(new ImageIcon("Player.png")), playerPosition[0] * mapWidth + playerPosition[1]);
		mapPanel.revalidate();
		mapPanel.repaint();	
	}
}
