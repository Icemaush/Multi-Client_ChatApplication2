/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Emote extends Remote {
    String[] emoteList = {
        "/amazed", 
        "/angry", 
        "/blush",
        "/bow", 
        "/cheer", 
        "/clap", 
        "/comfort", 
        "/cry",
        "/dance",
        "/dance1",
        "/dance2",
        "/dance3",
        "/dance4",	
        "/disgusted",
        "/doubt",
	"/doze",
	"/farewell",
	"/fume",
	"/goodbye",
        "/grin",
	"/huh",
	"/hurray",
	"/joy",
	"/kneel",
	"/laugh",
	"/muted",
	"/no",
	"/nod",
        "/panic",
	"/point",
	"/poke",
	"/praise",
	"/psych",
	"/salute",
	"/shocked",
	"/sigh",
	"/slap",
        "/smile",
	"/stagger",
	"/stare",
	"/sulk",
	"/surprised",
	"/think",
	"/toss",
	"/upset",
	"/wave",
	"/welcome",
        "/yes"
    };
    
    // Get a list of available emotes
    void emotes(String user) throws RemoteException;
    
    void amazed(String sender, String receiver) throws RemoteException;
    
    void angry(String sender, String receiver) throws RemoteException;
    
    void blush(String sender, String receiver) throws RemoteException;
    
    void bow(String sender, String receiver) throws RemoteException;
    
    void cheer(String sender, String receiver) throws RemoteException;
    
    void clap(String sender, String receiver) throws RemoteException;
    
    void comfort(String sender, String receiver) throws RemoteException;
    
    void cry(String sender, String receiver) throws RemoteException;
    
    void dance(String sender, String receiver) throws RemoteException;
    
    void dance1(String sender, String receiver) throws RemoteException;
    
    void dance2(String sender, String receiver) throws RemoteException;
    
    void dance3(String sender, String receiver) throws RemoteException;
    
    void dance4(String sender, String receiver) throws RemoteException;
    
    void disgusted(String sender, String receiver) throws RemoteException;
    
    void doubt(String sender, String receiver) throws RemoteException;
    
    void doze(String sender, String receiver) throws RemoteException;
    
    void farewell(String sender, String receiver) throws RemoteException;
    
    void fume(String sender, String receiver) throws RemoteException;
    
    void goodbye(String sender, String receiver) throws RemoteException;
    
    void grin(String sender, String receiver) throws RemoteException;
    
    void huh(String sender, String receiver) throws RemoteException;
    
    void hurray(String sender, String receiver) throws RemoteException;
    
    void joy(String sender, String receiver) throws RemoteException;
    
    void kneel(String sender, String receiver) throws RemoteException;
    
    void laugh(String sender, String receiver) throws RemoteException;
    
    void muted(String sender, String receiver) throws RemoteException;
    
    void no(String sender, String receiver) throws RemoteException;
    
    void nod(String sender, String receiver) throws RemoteException;
    
    void panic(String sender, String receiver) throws RemoteException;
    
    void point(String sender, String receiver) throws RemoteException;
    
    void poke(String sender, String receiver) throws RemoteException;
    
    void praise(String sender, String receiver) throws RemoteException;
    
    void psych(String sender, String receiver) throws RemoteException;
    
    void salute(String sender, String receiver) throws RemoteException;
    
    void shocked(String sender, String receiver) throws RemoteException;
    
    void sigh(String sender, String receiver) throws RemoteException;
    
    void slap(String sender, String receiver) throws RemoteException;
    
    void smile(String sender, String receiver) throws RemoteException;
    
    void stagger(String sender, String receiver) throws RemoteException;
    
    void stare(String sender, String receiver) throws RemoteException;
    
    void sulk(String sender, String receiver) throws RemoteException;
    
    void surprised(String sender, String receiver) throws RemoteException;
    
    void think(String sender, String receiver) throws RemoteException;
    
    void toss(String sender, String receiver) throws RemoteException;
    
    void upset(String sender, String receiver) throws RemoteException;
    
    void wave(String sender, String receiver) throws RemoteException;
    
    void welcome(String sender, String receiver) throws RemoteException;
    
    void yes(String sender, String receiver) throws RemoteException;
}
