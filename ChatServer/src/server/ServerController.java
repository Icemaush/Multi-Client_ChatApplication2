/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;

public class ServerController {

    @FXML private TextField textIP, textPort, textUsername, textMessage;
    @FXML private Button btnStartServer, btnStopServer;
    @FXML private TextArea textareaChat;
    @FXML private PasswordField pwdPassword;
    @FXML private Label lblServerStatus, lblNumUsers;
    @FXML private ListView listviewConnectedUsers;
    
    public Server server;
    private Thread thread;
    private boolean GUIUpdateListener = false;
    public boolean usersUpdated = false;
    
    @FXML
    public void initialize() {
        setIPPort("192.168.58.208", 3001);
        
        // Apply key press events for textareaMessage control
        textMessage.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                if (e.isShiftDown()) {
                    textMessage.appendText(System.getProperty("line.separator"));
                } else {
                    sendMessage();
                }
            }
        });
    }
    
    // Start the server
    @FXML public void btnStart_OnClick(ActionEvent event) {
        if (server.status.equals("OFFLINE")) {
            try {
                thread = new Thread(server);
                thread.start();
                startGUIUpdateListener();
                updateServerStatus("ONLINE");
                btnStartServer.setDisable(true);
                btnStopServer.setDisable(false);
            } catch (Exception ex) {
                appendMessage(ex + "\n*Unable to start server.");
            }
        }
    }
    
    // Stop the server
    @FXML public void btnStop_OnClick(ActionEvent event) {
        if (server.status.equals("ONLINE")) {
            try {
                server.stopServer();
                updateServerStatus("OFFLINE");
                updateConnectedUsers(null);
                btnStartServer.setDisable(false);
                btnStopServer.setDisable(true);
                GUIUpdateListener = false;
            } catch (Exception ex) {

            }
        }
    }
    
    //Add a user
    @FXML public void btnAddUser_OnClick(ActionEvent event) {
        if (validateUserInfo(textUsername.getText(), pwdPassword.getText())) {
            server.addUser(textUsername.getText(), pwdPassword.getText());
            clearUserFields();
        }
    }
    
    // Remove a user
    @FXML public void btnRemoveUser_OnClick(ActionEvent event) {
        if (textUsername.getText().isBlank()) {
            appendMessage("Enter username.");
        } else {
            server.removeUser(textUsername.getText());
            clearUserFields();
        }
    }
    
    // Reset a users password
    @FXML public void btnResetPassword_OnClick(ActionEvent event) {
        if (!textUsername.getText().isEmpty() || !pwdPassword.getText().isEmpty()) {
            server.resetPassword(textUsername.getText(), pwdPassword.getText());
        } else {
            appendMessage("Username and password required to reset password.");
        }
    }
    
    // Display all users
    @FXML public void btnDisplayUsers_OnClick(ActionEvent event) {
        server.displayUsers();
    }
    
    @FXML public void btnSend_OnClick(ActionEvent event) {
        sendMessage();
    }
    
    // Listen for and handle calls to update GUI
    private void startGUIUpdateListener() {
        final Thread userListener = new Thread(new Runnable() {
            @Override
            public void run() {
                GUIUpdateListener = true;
                while (GUIUpdateListener == true) {
                    System.out.print("");
                    try {
                        if (usersUpdated == true) {
                            updateConnectedUsers(server.getConnectedUsersArray());
                        }
                    } catch (NullPointerException e) {
                        
                    }
                }
            }
        });
        userListener.setDaemon(true);
        userListener.start();
    }
    
    // Update connected user list
    public void updateConnectedUsers(String[] connectedUsers) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                usersUpdated = false;
                if (connectedUsers == null || connectedUsers.length == 0) {
                    lblNumUsers.setText(Integer.toString(0));
                    listviewConnectedUsers.getItems().clear();
                } else if (connectedUsers.length > 0) {
                    ObservableList<String> content = FXCollections.observableArrayList();
                    for (var user : connectedUsers) {
                        content.add(user);
                    }
                    lblNumUsers.setText(Integer.toString(connectedUsers.length));
                    listviewConnectedUsers.getItems().clear();
                    listviewConnectedUsers.setItems(content);
                }
                
            }
        });
    }
    
    // Send message
    private void sendMessage() {
        if (!textMessage.getText().isBlank()) {
            server.sendServerMessage(textMessage.getText());
            textMessage.setText("");
        }
    }
    
    // Display message in chat window
    public void appendMessage(String message) {
        textareaChat.appendText(message + "\n");
    }
    
    // Update server status
    private void updateServerStatus(String status) {
        lblServerStatus.setText(status);
        if (status.equals("ONLINE")) {
            lblServerStatus.setTextFill(Color.GREEN);
        } else {
            lblServerStatus.setTextFill(Color.RED);
        }
    }
    
    // Automatically set IP address and port
    public void setIPPort(String address, int port) {
        textIP.setText(address);
        textPort.setText(Integer.toString(port));
    }
    
    // Validate user information
    private boolean validateUserInfo(String username, String password) {
        if (username.isBlank() && password.isBlank()) {
            appendMessage("Enter username AND password.");
            return false;
        } else if (username.length() < 3) {
            appendMessage("Username must be at least 3 characters.");
            return false;
        } else if (password.length() < 4) {
            appendMessage("Password must be at least 4 characters.");
            return false;
        } else {
            return true;
        }
    }
    
    // Clear user fields
    private void clearUserFields() {
        textUsername.setText("");
        pwdPassword.setText("");
        textUsername.requestFocus();
    }
}
