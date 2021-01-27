/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import server.Emote;

public class ClientController {

    @FXML private TextArea textareaChat;
    @FXML private TextField textUsername;
    @FXML private PasswordField pwdPassword;
    @FXML private TextField textServerAddress;
    @FXML private TextField textMessage;
    @FXML private ListView listviewUsers;
    @FXML private Button btnSend;
    @FXML private Button btnConnect;
    @FXML private Button btnDisconnect;
    public Client client;
    private boolean GUIUpdateListener = false;
    private boolean connected = false;
    public boolean usersUpdated = false;
    public String usersUpdatedString = null;

    @FXML
    public void initialize() {
        textServerAddress.setText("192.168.58.208:3001");
        disableChatControls();
    }
    
    // Connect to server
    @FXML public void btnConnect_OnClick(ActionEvent event) {
        try {
            if (!textUsername.getText().isEmpty() && !pwdPassword.getText().isEmpty() && !textServerAddress.getText().isEmpty()) {
                client.username = textUsername.getText();
                String password = pwdPassword.getText();
                String serverAddress = textServerAddress.getText();
                String serverIP = serverAddress.split(":")[0];
                int serverPort = Integer.parseInt(serverAddress.split(":")[1]);
                
                try {
                    client.socket = new Socket(serverIP, serverPort);
                    client.inStream = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
                    client.outStream = new DataOutputStream(client.socket.getOutputStream());
                } catch (IOException e) {
                    appendMessage("Unable to connect to server.");
                    client.socket = null;
                    return;
                }
                
                try {
                    client.registry = LocateRegistry.getRegistry(3002);
                    client.stub = (Emote)client.registry.lookup("Emote");
                } catch (RemoteException | NotBoundException e) {
                    
                }
                
                connected = true;
                startGUIUpdateListener();
                
                Listener listener = new Listener(client, this);
                client.listener = listener;
                Thread thread = new Thread(listener);
                thread.start();
                client.sendToServer("/uname " + client.username + ":/password " + password);
                disableLoginControls();
                enableChatControls();
                appendMessage("Connected to server.");
                textMessage.requestFocus();
            } else {
                appendMessage("Username, password and server address required!");
            }
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            client.socket = null;
            appendMessage("Unable to connect to server.");
        }
    }
    
    @FXML public void btnDisconnect_OnClick(ActionEvent event) {
        disconnect(false);
    }
    
    // Send message to server
    @FXML public void btnSend_OnClick(ActionEvent event) {
        String message = textMessage.getText();
        if (!message.isEmpty()) {
            client.sendToServer(message);
            // If standard message
            if (!message.startsWith("/")) {
                appendMessage("[" + client.username + "]: " + message);
            //If whisper
            } else if (message.startsWith("/w ")) {
                    appendMessage("To [" + message.split(" ")[1] + "]: " + message.substring(3 + message.split(" ")[1].length() + 1));
            // If emote
            } else {
                String emote = message.split(" ")[0].replace("/", "");
                String receiver;
                
                if (emote.equals("emotes")) {
                    client.callEmote(emote, null);
                } else {
                    try {
                        receiver = message.split(" ")[1];
                        receiver = receiver.substring(0, 1).toUpperCase() + receiver.substring(1);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        receiver = null;
                    }
                    client.callEmote(emote, receiver);
                }
            }
            textMessage.setText("");
        }
    }
    
    @FXML public void btnLeave_OnClick(ActionEvent event) {
        disconnect(false);
        Platform.exit();
    }
    
    // Display message in chat window
    public void appendMessage(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textareaChat.appendText(message + "\n");
            }
        });
    }
    
    // Update user list
    public void updateUsers() {
        Platform.runLater(new Runnable() {
            @Override
            @SuppressWarnings("ManualArrayToCollectionCopy")
            public void run() {
                try {
                    if (usersUpdatedString == null || usersUpdatedString.length() == 0) {
                        listviewUsers.getItems().clear();
                    } else {
                        String[] users = usersUpdatedString.substring(1, usersUpdatedString.length() - 1).split(", ");
                        ObservableList<String> content = FXCollections.observableArrayList();
                        for (var user : users) {
                            content.add(user);
                        }
                        listviewUsers.getItems().clear();
                        listviewUsers.setItems(content);
                    }
                } catch (NullPointerException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }
    
    // Listen for and handle calls to update GUI
    private void startGUIUpdateListener() {
        final Thread GUIUdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                GUIUpdateListener = true;
                
                while (GUIUpdateListener == true) {
                    System.out.print("");
                    if (usersUpdated == true) {
                        updateUsers();
                    }
                    usersUpdated = false;
                    
                    if (connected == false) {
                        disconnectUpdateGUI();
                    } else {
                        connectUpdateGUI();
                    }
                }
            }
        });
        GUIUdateThread.setDaemon(true);
        GUIUdateThread.start();
    }
    
    // Disconnect from server
    public void disconnect(boolean serverOffline) {
        connected = false;
        GUIUpdateListener = false;
        client.disconnect(serverOffline);
    }
    
    // Update GUI on disconnect
    public void disconnectUpdateGUI() {
        disableChatControls();
        enableLoginControls();
    }
    
    // Update GUI on connect
    public void connectUpdateGUI() {
        enableChatControls();
        disableLoginControls();
    }
    
    // Disable chat controls
    private void disableChatControls() {
        textareaChat.setDisable(true);
        textMessage.setDisable(true);
        listviewUsers.setDisable(true);
        btnSend.setDisable(true);
        btnSend.setDefaultButton(false);
        btnConnect.setDefaultButton(true);
    }
    
    // Disable login controls
    private void disableLoginControls() {
        textUsername.setDisable(true);
        pwdPassword.setDisable(true);
        textServerAddress.setDisable(true);
        btnConnect.setDisable(true);
        btnConnect.setDefaultButton(false);
    }
    
    // Enable chat controls
    private void enableChatControls() {
        textareaChat.setDisable(false);
        textMessage.setDisable(false);
        listviewUsers.setDisable(false);
        btnSend.setDisable(false);
        btnSend.setDefaultButton(true);
        btnConnect.setDefaultButton(false);
    }
    
    // Enable login controls
    private void enableLoginControls() {
        textUsername.setDisable(false);
        pwdPassword.setDisable(false);
        textServerAddress.setDisable(false);
        btnConnect.setDisable(false);
        btnConnect.setDefaultButton(true);
    }
    
    
}