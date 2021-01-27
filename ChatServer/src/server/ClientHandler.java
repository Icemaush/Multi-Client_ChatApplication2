/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Server server;
    private final ServerController serverController;
    private User user;
    private final Socket clientSocket;
    private String clientName;
    private BufferedReader inStream = null;
    private DataOutputStream outStream = null;
    private boolean listening = true;
    
    
    public ClientHandler(Server server, ServerController serverController, Socket clientSocket) {
        this.server = server;
        this.serverController = serverController;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        
        listen();
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    private void listen() {
        String line;
        while (listening == true) {
            try {
                line = inStream.readLine();
                
                if (line.startsWith("/uname ")) {
                    verifyUserCredentials(line);
                    continue;
                }
                
                if (line.startsWith("/w ")) {
                    String receiver = line.split(" ")[1];
                    String message = line.substring(3 + receiver.length() + 1);
                    server.whisperMessage(this, receiver, message);
                    continue;
                }
                
                if (line.startsWith("/clientquit")) {
                    disconnect(line);
                    clientSocket.close();
                    break;
                }
                
                if (line.startsWith("/serveroffline")) {
                    disconnect(line);
                    clientSocket.close();
                    break;
                }
                
                if (!line.isBlank() && !line.startsWith("/")) {
                    serverController.appendMessage("[" + user.getUsername() + "]: " + line);
                    server.broadcastMessage(this, line);
                }
            } catch (IOException e) {
                System.out.println(clientName + " has disconnected. " + e.getMessage());

                return;
            }
        }
    }
    
    // Disconnect from server
    public void disconnect(String line) {
        try {
            if (!line.equals("/serveroffline")) {
                sendToClient("/disconnect");
            }
            clientSocket.close();
            server.updateConnectedUsers("Remove", user, line);
            
            if (!user.getUsername().isBlank()) {
                server.sendUserDisconnectedMessage(getUser().getUsername());
            }
        } catch (IOException | NullPointerException e) {

        } finally {
            listening = false;
            server.addToRemoveList(this);
        }
    }
    
    // Send message to client
    public void sendToClient(String message) {
        try {
            outStream.writeBytes(message + "\r\n");
            outStream.flush();
        } catch (IOException e) {

        }
    }
    
    // Verify user credentials
    private void verifyUserCredentials(String line) {
        String uname = line.split(":")[0].split(" ")[1];
        String pword = line.split(":")[1].split(" ")[1];
        
        if (server.verifyUser(uname, pword)) {
            user = server.findUser(uname);
            server.updateConnectedUsers("Add", user, "/newuser");
            sendToClient("/uname 0");
            server.sendUserConnectedMessage(uname);
        } else {
            sendToClient("/uname 1");
            disconnect("/quit");
        }
    }

    // Get user assosciated with the clienthandler
    public User getUser() {
        return user;
    }
}
