/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package client;

import java.io.IOException;

public class Listener extends Thread {
    private Client client;
    private ClientController clientController;
    public boolean listening = true;
    
    public Listener(Client client, ClientController clientController) {
        this.client = client;
        this.clientController = clientController;
    }
    
    @Override
    public void run() {
        String line;
        while (listening == true) {
            try {
                // Read from server
                line = client.inStream.readLine();
                
                // Check for username command
                if (line.startsWith("/uname")) {
                    client.checkUsernameStatus(line);
                    continue;
                }
                
                // Listen for server offline message
                if (line.startsWith("/offline")) {
                    String message = line.split(" ", 2)[1];
                    clientController.appendMessage(message);
                    clientController.disconnect(true);
                    continue;
                }
                
                // Check for user connected/disconnected message
                if (line.startsWith("/con") || line.startsWith("/dcon")) {
                    String message = line.split(" ", 2)[1];
                    clientController.appendMessage(message);
                    continue;
                }
                
                // Check for user update message
                if (line.startsWith("/updateusers")) {
                    line = line.substring(13);
                    clientController.usersUpdatedString = line;
                    clientController.usersUpdated = true;
                    continue;
                }
                
                // Check for whisper command
                if (line.startsWith("/w ")) {                  
                    line = line.substring(3);
                    clientController.appendMessage(line);
                    continue;
                }
                
                // Check for disconnect command
                if (line.startsWith("/disconnect")) {
                    clientController.disconnect(false);
                    break;
                }
                
                // Check for emotes
                if (line.startsWith("/emotes")) {                  
                    String[] emotes = line.substring(9, line.length() - 1).split(", ");

                    try {
                        clientController.appendMessage("List of available emotes:");
                        for (String emote : emotes) {
                            clientController.appendMessage(emote);
                        }
                    } catch (NullPointerException e) {
                        
                    }
                    continue;
                }
                
                clientController.appendMessage(line);
                
            } catch (IOException e) {

            }
        }
    }
}
