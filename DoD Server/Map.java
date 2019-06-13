/*
 * This map class handles all loadgin and validation of the game map.
 * This class also handles creating a look window so a player can
 * see where they are on the map.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Map {
	
	private char[][] map;
	private String mapName;
	private int totalGoldOnMap;

	public Map() {
		map = null;
		mapName = "";
		totalGoldOnMap = -1;
	}

	
	/*
	 * This method reads in a map from a text file and checks that it is valid
	 */
	public char[][] readMap(String mapName) {
		
		BufferedReader reader = null;
		
		// Get the file path for where the program is being run from
		String mapPath = System.getProperty("user.dir") + "/maps/" + mapName;
		String defaultMapPath = System.getProperty("user.dir") + "/maps/" + "example_map.txt";
		
		try {
			reader = new BufferedReader(new FileReader(new File(mapPath)));
		} catch (FileNotFoundException e) {
			
			// If the custom map isnt found
			if (mapName.isEmpty() == false) {
				showMSG("Map not found the default map will be used.");
			}
			
			// Try to find the example map
			try {
				reader = new BufferedReader(new FileReader(new File(defaultMapPath)));
				
			} catch (FileNotFoundException e1) {
				showMSG("No valid map name given and default file example_map.txt not found.");
				System.exit(-1);
			}
		}
		
		// Validate the map
		try {
			map = loadMap(reader);
			return map;
			
		} catch (IOException e) {
			showMSG("Map file invalid or wrongly formatted.");
			System.exit(-1);
			
		} finally {
			
			try {
				reader.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/*
	 * This method loads a map from a file into array and variables.
	 * This method also validates the map to check it has all the
	 * features a map should such as an exit and enough gold.
	 */
	private char[][] loadMap(BufferedReader reader) throws IOException {

		boolean error = false;
		ArrayList<char[]> tempMap = new ArrayList<char[]>();
		int width = -1;
		
		// Read name
		String in = reader.readLine();
		if (in.startsWith("name")) {
			error = setName(in);
		}

		// Read win criteria
		in = reader.readLine();
		if (in.startsWith("win")) {
			error = setWin(in);
		}
		
		in = reader.readLine();
		if (in.charAt(0) == '#' && in.length() > 1) {
			width = in.trim().length();
		}
		
		// Read in all the map's tiles
		while (in != null && !error) {

			char[] row = new char[in.length()];
			if (in.length() != width)
				error = true;
			
			// Read a row of the map
			for (int i = 0; i < in.length(); i++) {
				row[i] = in.charAt(i);
			}

			tempMap.add(row);

			in = reader.readLine();
		}
		
		// Ensure there has been no errors
		if (error) {
			setName("");
			setWin("");
			return null;
		}
		
		// Store map in 2D array
		char[][] map = new char[tempMap.size()][width];

		for (int i = 0; i < tempMap.size(); i++) {
			map[i] = tempMap.get(i);
		}
		return map;
	}
	
	/*
	 * This method reads in the win criteria of a map and sotres it.
	 */
	private boolean setWin(String in) {
		
		// Ensure correct formatting
		if (!in.startsWith("win "))
			return true;
		int win = 0;
		
		try {
			win = Integer.parseInt(in.split(" ")[1].trim());
			
		} catch (NumberFormatException n) {
			showMSG("The map does not contain a valid win criteria.");
		}
		
		// Ensure valid win criteria
		if (win < 0)
			return true;
		this.totalGoldOnMap = win;

		return false;
	}

	/*
	 * This method reads in the name of a map
	 */
	private boolean setName(String in) {
		
		// Check the map name has length > 0 and that it is formatted correctly
		if (!in.startsWith("name ") && in.length() < 4)
			return true;
		String name = in.substring(4).trim();

		if (name.length() < 1)
			return true;

		this.mapName = name;

		return false;
	}


	/*
	 * This method raplces a map tile with a different tile
	 */
	protected char replaceTile(int y, int x, char tile) {
		char output = map[y][x];
		map[y][x] = tile;
		return output;
	}


	/*
	 * This method returns the map tile at a given location
	 */
	protected char lookAtTile(int y, int x) {
		
		// Check the co-ordinates given are valid
		if (y < 0 || x < 0 || y >= map.length || x >= map[0].length)
			return '#';
		char output = map[y][x];

		return output;
	}


	/*
	 * This method is used to retrieve a map view around a certain location. The
	 * method is used to get the look window around the player location.
	 */
	protected char[][] lookWindow(int y, int x, int radius, int[] playersPositions, int playerCount) {
		
		char[][] reply = new char[radius][radius];
	
		//Loop for size of the look window (default 5x5)
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {

				int posX = x + j - radius / 2;
				int posY = y + i - radius / 2;
				
				// Get the look window tiles
				if (posX >= 0 && posX < getMapWidth() && posY >= 0 && posY < getMapHeight()) {
					reply[j][i] = map[posY][posX];
				} else {
					reply[j][i] = '#';
				}
				
				// Add all players to the look window
				for (int k = 0; k <= playerCount; k++) {
					if (posY == playersPositions[k * 2] && posX == playersPositions[k * 2 + 1]) {
						reply[j][i] = 'P';
					}
				}

			}
		}
		// Fill Xs in corners of the look window
		reply[0][0] = 'X';
		reply[radius - 1][0] = 'X';
		reply[0][radius - 1] = 'X';
		reply[radius - 1][radius - 1] = 'X';
		return reply;
	}
	
	/*
	 * This method shows error messages to the user.
	 */
	private static void showMSG(String errorMSG) {
		JOptionPane.showMessageDialog(null, errorMSG, null, JOptionPane.WARNING_MESSAGE);
	}
	
	public int getWin() {
		return totalGoldOnMap;
	}

	public String getMapName() {
		return mapName;
	}

	public int getMapWidth() {
		return map[0].length;
	}

	public int getMapHeight() {
		return map.length;
	}

}
