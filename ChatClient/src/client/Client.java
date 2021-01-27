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
import java.net.Socket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.rmi.registry.Registry;
import server.Emote;
import java.rmi.RemoteException;

public class Client extends Application {
    String username = null;
    Socket socket = null;
    public DataOutputStream outStream = null;
    public BufferedReader inStream = null;
    Listener listener = null;
    ClientController clientController;
    Registry registry;
    Emote stub;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Client.fxml"));
        Parent root = loader.load();
        clientController = loader.getController();
        clientController.client = this;
        
        primaryStage.setTitle("Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(eh -> {
            if (socket != null) {
                disconnect(false);
            }
        });
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Send message to server
    public void sendToServer(String message) {
        try {
            outStream.writeBytes(message + "\r\n");
            outStream.flush();
        } catch (IOException e) {

        }
    }
    
    // Disconnect from server
    public void disconnect(boolean serverOffline) {
        try {
            if (socket != null) {
                // Send disconnect message to server
                if (serverOffline) {
                    sendToServer("/serveroffline");
                } else {
                    sendToServer("/clientquit");
                }
                socket.close();
            }
            listener.listening = false;
            socket = null;
            username = null;
            clientController.appendMessage("Disconnected from server.");
        } catch (IOException e) {

        }
    }
    
    // Check username status response from server
    public void checkUsernameStatus(String line) {
        String status = line.split(" ")[1];
        if (status.equals("1")) {
            clientController.disconnect(false);
        }
    }
    
    // EMOTE SWITCH
    // <editor-fold>
    public void callEmote(String emote, String receiver) {
        try {
            switch (emote) {
            case "amazed" -> stub.amazed(username, receiver);
            case "angry" -> stub.angry(username, receiver);
            case "blush" -> stub.blush(username, receiver);
            case "bow" -> stub.bow(username, receiver);
            case "cheer" -> stub.cheer(username, receiver);
            case "clap" -> stub.clap(username, receiver);
            case "comfort" -> stub.comfort(username, receiver);
            case "cry" -> stub.cry(username, receiver);
            case "dance" -> stub.dance(username, receiver);
            case "dance1" -> stub.dance1(username, receiver);
            case "dance2" -> stub.dance2(username, receiver);
            case "dance3" -> stub.dance3(username, receiver);
            case "dance4" -> stub.dance4(username, receiver);
            case "disgusted" -> stub.disgusted(username, receiver);
            case "doubt" -> stub.doubt(username, receiver);
            case "doze" -> stub.doze(username, receiver);
            case "emotes" -> stub.emotes(username);
            case "farewell" -> stub.farewell(username, receiver);
            case "fume" -> stub.fume(username, receiver);
            case "goodbye" -> stub.goodbye(username, receiver);
            case "grin" -> stub.grin(username, receiver);
            case "huh" -> stub.huh(username, receiver);
            case "hurray" -> stub.hurray(username, receiver);
            case "joy" -> stub.joy(username, receiver);
            case "kneel" -> stub.kneel(username, receiver);
            case "laugh" -> stub.laugh(username, receiver);
            case "muted" -> stub.muted(username, receiver);
            case "no" -> stub.no(username, receiver);
            case "nod" -> stub.nod(username, receiver);
            case "panic" -> stub.panic(username, receiver);
            case "point" -> stub.point(username, receiver);
            case "poke" -> stub.poke(username, receiver);
            case "praise" -> stub.praise(username, receiver);
            case "psych" -> stub.psych(username, receiver);
            case "salute" -> stub.salute(username, receiver);
            case "shocked" -> stub.shocked(username, receiver);
            case "sigh" -> stub.sigh(username, receiver);
            case "slap" -> stub.slap(username, receiver);
            case "smile" -> stub.smile(username, receiver);
            case "stagger" -> stub.stagger(username, receiver);
            case "stare" -> stub.stare(username, receiver);
            case "sulk" -> stub.sulk(username, receiver);
            case "surprised" -> stub.surprised(username, receiver);
            case "think" -> stub.think(username, receiver);
            case "toss" -> stub.toss(username, receiver);
            case "upset" -> stub.upset(username, receiver);
            case "wave" -> stub.wave(username, receiver);
            case "welcome" -> stub.welcome(username, receiver);
            case "yes" -> stub.yes(username, receiver);
            default -> {
                }
            }
        } catch (RemoteException e) {
                    
        }
    }
    // </editor-fold>
}
