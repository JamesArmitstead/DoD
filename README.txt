The objective of the game is to move around the map picking up gold until
you have enough to win the game. To win the game a player must stand on an
exit tile with at least the amount of gold required to complete the map.

To setup a server

1. Compile the Server java class with "javac Server.java".
2. Run the compiled code with "java Server".
3. Enter a port number for the server 4004 works well.
4. Enter a map name to load a map or leave
   the field blank to load the deafult map.
5. The server GUI is now open and you can see everything going on in the world.
6. The server GUI reports the server IP address and port number.
7. The shut down button shuts the server down.


To play the game

1. Ensure there is a server running.
2. Compile the Human java class with "javac Human.java".
3. Run the compiled code with "java Human".
4. Input the server IP address and port number. The IP is "localhost" if the server
   is on the same network as the client. Port number 4004 works well.
5. The Human GUI will open and you will be able to see the graphical look window.
6. The Human GUI has four buttons for moving 4 different directions.
7. The Human GUI has a pick up button which allows the player to pick up gold.
8. The Human GUI has a quit button which quits the game.