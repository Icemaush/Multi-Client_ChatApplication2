/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;

public class Server extends Application implements Runnable, Emote {
    static List<User> users = new ArrayList();
    public List<User> connectedUsers = new ArrayList();
    static List<ClientHandler> clients = new ArrayList();
    static List<ClientHandler> removeClients = new ArrayList();
    private static ServerSocket serverSocket;
    private static Socket clientSocket = null;
    private static ServerController serverController;
    public int serverPort = 3001;
    public int rmiPort = 3002;
    boolean listening;
    public String status = "OFFLINE";
    boolean exit = false;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
        Parent root = loader.load();
        serverController = loader.getController();
        serverController.server = this;
        
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(eh -> {
            exit = true;
            stopServer();
        });
        primaryStage.show();
    }
    
    @Override
    public void run() {
        startServer();
    }
    
    public static void main(String[] args) {      
        launch(args);
    }
    
    // Starts the server
    public void startServer() {
        try {
            loadUsers();
            serverSocket = new ServerSocket(serverPort);
            setupEmoteRMI();
            status = "ONLINE";
            listening = true;
            serverController.appendMessage("Server started. Listening on port: " + serverSocket.getLocalPort());
            
            while (listening == true) {
                try {
                    clientSocket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(this, serverController, clientSocket);
                    client.start();
                    clients.add(client);
                } catch (IOException e) {

                }
            }
            
            serverSocket = null;
            clientSocket = null;
        } catch (IOException e) {

        }
    }
    
    // Setup Emote RMI
    private void setupEmoteRMI() {
        try {
            Emote stub = (Emote)UnicastRemoteObject.exportObject(this, 0);
            
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.bind("Emote", stub);
            serverController.appendMessage("RMI registry created. Listening on port: " + rmiPort);
        } catch (RemoteException | AlreadyBoundException e) {

        }
    }
    
    // Stops the server
    public void stopServer() {
        try {
            status = "OFFLINE";
            saveUsers();
            sendServerOfflineMessage();
            removeClients();

            //serverController.updateConnectedUsers(new String[0]);

            listening = false;            
            serverController.appendMessage("Server stopped.");

            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {

            } finally {
                serverSocket = null;
                clientSocket = null;
                if (exit == true) {
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    // Remove client
    public void removeClients() {
        clients.removeAll(removeClients);
        connectedUsers.clear();
    }
    
    // Add client to removeClients list
    public void addToRemoveList(ClientHandler client) {
        removeClients.add(client);
    }
    
    // Add a new user
    public void addUser(String username, String password) {
        if (findUser(username) == null) {
            User user = new User(username, password);
            users.add(user);
            displayStatusMessage("User added: " + username);
        } else {
            displayStatusMessage("User already exists.");
        }
    }
    
    // Remove a user
    public void removeUser(String username) {
        if (users.remove(findUser(username))) {
            try {
                findClient(username).disconnect("");
            } catch (Exception e) {

            }
            
            displayStatusMessage("User removed: " + username);
        } else {
            displayStatusMessage("User not found.");
        }
    }
    
    // Reset password
    public void resetPassword(String username, String password) {
        if (findUser(username).resetPassword(password)) {
            displayStatusMessage("User: " + username + ", password has been reset.");
        } else {
            displayStatusMessage("User: " + username + ", does not exist.");
        }
    }
    
    // Find user
    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    // Find client
    private ClientHandler findClient(String username) {
        for (ClientHandler client : clients) {
            if (client.getUser().getUsername().equals(username)) {
                return client;
            }
        }
        return null;
    }
    
    // Disconnect all client
    public void disconnectAll() {
        for (ClientHandler client : clients) {
            try {
                client.disconnect("");
            } catch (Exception e) {

            }
        }
        removeClients();
    }
    
    // Display users
    public void displayUsers() {
        if (users.isEmpty()) {
            displayStatusMessage("No users registered.");
        } else {
            for (User user : users) {
                displayStatusMessage(user.getUsername());
            }
        }
    }

    // Save user information to file
    private void saveUsers() {
        try (FileOutputStream fileOut = new FileOutputStream("users.bin")) {
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(users);
        } catch (IOException e) {

        }
    }
    
    // Load users from file
    private void loadUsers() {
        try (FileInputStream fileIn = new FileInputStream("users.bin")) {
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            users = (ArrayList<User>)objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {

        }
    }
    
    // Verify user
    public boolean verifyUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.verifyPassword(password);
            }
        }
        return false;
    }
    
    // Update connected users list
    public void updateConnectedUsers(String status, User user, String reason) {
        if (status.equals("Add")) {
            connectedUsers.add(user);
        } else {
            connectedUsers.remove(user);
        }
        
        serverController.usersUpdated = true;
        
        if (!reason.equals("/serveroffline")) {
            //serverController.updateConnectedUsers(getConnectedUsersArray());
        }
    }
    
    public String[] getConnectedUsersArray() {
        String[] conUsers;
        if (connectedUsers.isEmpty()) {
            conUsers = new String[0];
        } else {
            conUsers = new String[connectedUsers.size()];
            for (int i = 0; i < connectedUsers.size(); i++) {
                conUsers[i] = connectedUsers.get(i).getUsername();
            }
        }
        
        return conUsers;
    }
    
// MESSAGING METHODS
// <editor-fold>    
    // Display server status message
    public void displayStatusMessage(String message) {
        serverController.appendMessage(message);
    }
    
    // Send server offline message
    private void sendServerOfflineMessage() {
        for (ClientHandler client : clients) {
            client.sendToClient("/offline << Server is offline >>");
        }
    }
    
    // Send user connected message
    public void sendUserConnectedMessage(String name) {
        for (ClientHandler client : clients) {
            client.sendToClient("/con [#Server]: " + name + " has connected.");
            client.sendToClient("/updateusers " + Arrays.toString(getConnectedUsersArray()));
        }
        serverController.appendMessage("[#Server]: " + name + " has connected.");
    }
    
    // Send user disconnected message
    public void sendUserDisconnectedMessage(String name) {
        for (ClientHandler client : clients) {
            client.sendToClient("/dcon [#Server]: " + name + " has disconnected.");
            client.sendToClient("/updateusers " + Arrays.toString(getConnectedUsersArray()));
        }
        serverController.appendMessage("[#Server]: " + name + " has disconnected.");
    }
    
    // Send system (server) message to all users
    public void sendServerMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendToClient("[#Server]: " + message);
        }
        serverController.appendMessage("[#Server]: " + message);
    }
    
    // Broadcast message
    public void broadcastMessage(ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            if (!client.equals(sender)) {
                client.sendToClient("[" + sender.getUser().getUsername() + "]: " + message);
            }
        }
    }
    
    // Send whisper to client
    public void whisperMessage(ClientHandler sender, String receiver, String message) {
        for (ClientHandler client : clients) {
            if (client.getUser().getUsername().equals(receiver)) {
                client.sendToClient("/w [" + sender.getUser().getUsername() + "] whispers: " + message);
                return;
            }
        }
        sender.sendToClient("ERROR: Unable to send to '" + receiver + "'.");
    }
    
    // Send user message back to sending client
    public void sendUserMessage(String username, String message) {
        for (ClientHandler client : clients) {
            if (client.getUser().getUsername().equals(username)) {
                client.sendToClient(message);
            }
        }
    }
    
    // Send emote message
    public void emoteMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendToClient("~ " + message);
        }
        serverController.appendMessage("~ " + message);
    }
    
// </editor-fold>    

// EMOTE METHODS (REMOTE METHOD INVOCATION)
// <editor-fold>
    @Override
    public void emotes(String username) throws RemoteException {
        sendUserMessage(username, "/emotes " + Arrays.toString(emoteList));
    }
    
    @Override
    public void amazed(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks amazed but curious.";
        String targetMessage = sender + ", amazed, motiions questioningly at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void angry(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks angry.";
        String targetMessage = sender + " motions angrily at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void blush(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " blushes in embarrassment.";
        String targetMessage = sender + " looks away from " + receiver + ", embarrassed.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void bow(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " bows.";
        String targetMessage = sender + " bows courteously to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void cheer(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " cheers!";
        String targetMessage = sender + " cheers " + receiver + " on!";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void clap(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " claps.";
        String targetMessage = sender + " claps at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void comfort(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " seems to want to cheer someone up, badly!";
        String targetMessage = sender + " pats " + receiver + " consolingly.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void cry(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + "'s eyes brim over with tears.";
        String targetMessage = sender + " weeps in sorrow before " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void dance(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " dances happily.";
        String targetMessage = sender + " and " + receiver + " happily dance together.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void dance1(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " performs a passionate samba.";
        String targetMessage = sender + " performs a passionate sambe for " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void dance2(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " performs an elegant waltz.";
        String targetMessage = sender + " performs an elegant waltz for " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void dance3(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " performs an intricate jig.";
        String targetMessage = sender + " performs an intricate jig for " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void dance4(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " performs a lively jig.";
        String targetMessage = sender + " performs a lively jig for " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void disgusted(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks disgusted.";
        String targetMessage = sender + " looks at " + receiver + " disgustedly.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void doubt(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks doubtful.";
        String targetMessage = sender + " questions " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void doze(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " dozes off quietly.";
        String targetMessage = sender + " falls asleep in front of " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void farewell(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " waves goodbye.";
        String targetMessage = sender + " waves goodbye to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void fume(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " fumes.";
        String targetMessage = sender + "fumes in front of " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void goodbye(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " waves goodbye.";
        String targetMessage = sender + " waves goodbye to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void grin(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " grins.";
        String targetMessage = sender + " grins roguishly at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void huh(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " gives up, confused.";
        String targetMessage = sender + " stares at " + receiver + ", confused.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void hurray(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " gives a triumphant cry!";
        String targetMessage = sender + " gives a triumphant cry with " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void joy(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " motions joyfully.";
        String targetMessage = sender + " motions joyfully to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void kneel(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " kneels respectfully.";
        String targetMessage = sender + " kneels respectfully before " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void laugh(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " shakes with laughter.";
        String targetMessage = sender + " bursts out laughing beside " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void muted(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " falls silent.";
        String targetMessage = sender + " falls silent in fron of " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void no(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " disagrees.";
        String targetMessage = sender + "disagrees with " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void nod(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " nods.";
        String targetMessage = sender + " nods to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void panic(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " panics!";
        String targetMessage = sender + " looks at " + receiver + " and panics!";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void point(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " points.";
        String targetMessage = sender + " points at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void poke(String sender, String receiver) throws RemoteException {
        String targetMessage = sender + " pokes " + receiver + ".";
        
        if (receiver != null) {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void praise(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " applauds.";
        String targetMessage = sender + "praises " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void psych(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " psychs up!";
        String targetMessage = sender + " psychs up along with " + receiver + "!";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void salute(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " salutes.";
        String targetMessage = sender + " salutes " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void shocked(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks shocked!";
        String targetMessage = sender + " looks at " + receiver + ", shocked!";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void sigh(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " sighs dejectedly.";
        String targetMessage = sender + " sighs dejectedly at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void slap(String sender, String receiver) throws RemoteException {
        String targetMessage = sender + " slaps " + receiver + ".";
        
        if (receiver != null) {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void smile(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " smiles warmly.";
        String targetMessage = sender + " smiles warmly at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void stagger(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " staggers.";
        String targetMessage = sender + " looks at " + receiver + " and staggers.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void stare(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " stares blankly.";
        String targetMessage = sender + " stares at " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void sulk(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " sulks.";
        String targetMessage = sender + " looks away from " + receiver + ", sulking.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void surprised(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks surprised!";
        String targetMessage = sender + " looks at " + receiver + " in surprise.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void think(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " seems lost in thought.";
        String targetMessage = sender + " looks away from " + receiver + ", lost in thought.";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void toss(String sender, String receiver) throws RemoteException {
        String targetMessage = sender + " tosses something at " + receiver + ".";
        
        if (receiver != null) {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void upset(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " looks upset.";
        String targetMessage = sender + " looks upset with " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void wave(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " waves.";
        String targetMessage = sender + " waves to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void welcome(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " motions in welcome.";
        String targetMessage = sender + " welcomes " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }

    @Override
    public void yes(String sender, String receiver) throws RemoteException {
        String selfMessage = sender + " nods.";
        String targetMessage = sender + " nods to " + receiver + ".";
        
        if (receiver == null) {
            emoteMessage(selfMessage);
        } else {
            emoteMessage(targetMessage);
        }
    }
// </editor-fold>
}
