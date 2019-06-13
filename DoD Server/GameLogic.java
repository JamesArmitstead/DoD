/*
 * This class handles all the logic of the game. It handles
 * spawning, moving, winning, picking up gold, collision
 * detection and despawning players.
 */

import java.util.Random;

public class GameLogic {
	
	ServerGUI GUI = null;
	private Map gameMap = null;
	private int[] playersPositions = new int[20];
	private int[] collectedGold = new int[10];
	private int playerCount = 0;
	private int port;
	private boolean active;
	
	/*
	 * Instantiates the map class and stores the port number
	 */
	public GameLogic(int port) {
		this.port = port;
		gameMap = new Map();
	}
	
	/*
	 * This method reads in a map and instantiates the ServerGUI class.
	 */
	public ServerGUI setMap(String mapName) {
		char[][] map;
		map = gameMap.readMap(mapName);
		active = true;
		GUI = new ServerGUI(map, port);
		return GUI;
	}
	

	/*
	 * This method returns how much gold is still required to win.
	 */
	public String hello(int player) {
		if (gameMap.getWin() < collectedGold[player]) {
			return "GOLD: 0";
		}
		return "GOLD: " + (gameMap.getWin() - collectedGold[player]);
	}

	/*
	 * By proving a character direction from the set of {N,S,E,W} the gamelogic
	 * checks if this location can be visited by the player. If it is true, the
	 * player is moved to the new location. It also updates the server GUI in a new thread
	 * to show the player's new position.
	 */
	public synchronized String move(char direction, int player) {

		int[] newPosition = {playersPositions[player * 2] ,playersPositions[player * 2 + 1]};
		final int[] oldPosition = {playersPositions[player * 2] ,playersPositions[player * 2 + 1]};
	
		// Edit player's position based on direction of movement
		switch (direction) {
		case 'N':
			newPosition[0] -= 1;
			break;
		case 'E':
			newPosition[1] += 1;
			break;
		case 'S':
			newPosition[0] += 1;
			break;
		case 'W':
			newPosition[1] -= 1;
			break;
		default:
			break;
		}
		
		final int[] finalNewPosition = newPosition;
		
		// Check to ensure player doesn't move into wall or another player
		if (gameMap.lookAtTile(newPosition[0], newPosition[1]) != '#' && !isOccupied(newPosition)) {
			
			playersPositions[player * 2] = newPosition[0];
			playersPositions[player * 2 + 1] = newPosition[1];
			
			// Use a new thread to move the player on the server GUI
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					GUI.movePlayer(oldPosition, finalNewPosition);
					return;
				}
			});
			
			return "SUCCESS";
		} else {
			return "FAIL";
		}
	}
	
	/*
	 * This method returns the tiles of the dungeon that are around the player's location
	 * and is called when the look command is used.
	 */
	public String look(int player) {
		String output = "";
		
		// Get look window from map class
		char[][] lookReply = gameMap.lookWindow(playersPositions[player * 2], playersPositions[player * 2 + 1], 5,
				playersPositions, playerCount);
		
		// Concatenate the look window into one big string
		for (int i = 0; i < lookReply[0].length; i++) {

			for (int j = 0; j < lookReply[0].length; j++) {
				output += lookReply[j][i];
			}
			output += "\n";
		}
		return output;
	}
	
	/*
	 * This method is called when a player tries to pickup. It is
	 * synchronized so that the gold array isn't access while being edited.
	 */
	public synchronized String pickup(int player) {
		
		// Check if the player is stood on gold
		if (gameMap.lookAtTile(playersPositions[player * 2], playersPositions[player * 2 + 1]) == 'G') {
			collectedGold[player]++;
			gameMap.replaceTile(playersPositions[player * 2], playersPositions[player * 2 + 1], '.');
			return "SUCCESS, GOLD COINS: " + collectedGold[player];
		}

		return "FAIL" + "\n" + "There is nothing to pick up...";
	}

	/*
	 * This code erases a player from the server when they either win, 
	 * disconnect or quit. It also updates the server GUI in a new thread
	 * so that the player no longer shows up.
	 */
	public synchronized void erasePlayer(int player) {
		
		// Reset the player's variables
		final int[] playerPosition = {playersPositions[player * 2], playersPositions[player * 2 + 1]};
		playersPositions[player * 2] = -1;
		playersPositions[player * 2 + 1] = -1;
		collectedGold[player] = 0;
		
		// Use a new thread to erase the player from the server GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUI.erasePlayer(playerPosition);
				return;
			}
		});
	}

	/*
	 * This method controls the spawning of players to ensure players
	 * don't spawn at exactly the same time it is synchronized. It also updates
	 * the ServerGUI to show newly spawned players.
	 */
	public synchronized void setPlayerPosition(int player) {
		
		playerCount = player;
		final int[] newPlayerPos = pickSpawn();
		playersPositions[playerCount * 2] = newPlayerPos[0];
		playersPositions[playerCount * 2 + 1] = newPlayerPos[1];
		
		// Create a new thread to handle the updating of the server GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUI.placePlayer(newPlayerPos);
				return;
			}
		});
		
	}
	
	/*
	 * This method finds a random position for the player to spawn on the map.
	 */
	private synchronized int[] pickSpawn() {
		int[] pos = new int[2];
		Random rand = new Random();
		
		pos[0] = rand.nextInt(gameMap.getMapHeight());
		pos[1] = rand.nextInt(gameMap.getMapWidth());
		int counter = 0;
		
		// Ensure the tile isn't occupied by a player or a wall
		while (gameMap.lookAtTile(pos[0], pos[1]) == '#' || isOccupied(pos)) {
			pos[0] = rand.nextInt(gameMap.getMapHeight());
			pos[1] = rand.nextInt(gameMap.getMapWidth());
			counter++;
			// If all tiles are occupied then return null
			if (counter > 99999) {
				return null;
			}
		}

		return pos;
	}

	/*
	 * This method checks to see if a tile is occupied by a player, it
	 * is synchronized so player positions can't change when checking a tile.
	 */
	private synchronized boolean isOccupied(int[] newPosition) {

		for (int i = 0; i <= playerCount; i++) {
			
			if (playersPositions[i * 2] == newPosition[0] && (playersPositions[i * 2 + 1] == newPosition[1])) {
				return true;
			}
		}

		return false;
	}
	
	
	/*
	 * This method checks if the player collected enough GOLD to win and is on the exit tile.
	 */
	public synchronized boolean checkWin(int player) {
		if (collectedGold[player] >= gameMap.getWin()
				&& gameMap.lookAtTile(playersPositions[player * 2], playersPositions[player * 2 + 1]) == 'E') {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/*
	 * This method returns if the server is currently running
	 */
	public boolean isRunning() {
		return active;
	}

}
