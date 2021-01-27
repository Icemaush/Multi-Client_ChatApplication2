# Multi-client_ChatApplication2
A JavaFX multi-client chat application.

This application was created using Netbeans, JavaFX and Scenebuilder.

This is a multi-client chat application that handles multiple, simultaneous connections through multi-threading.  
Remote Method Invocation (RMI) is used to implement an emote system for connected clients.  
** The emote system used is from the MMORPG Final Fantasy XI.

Server functionality includes:  
- Start and stop the server  
- Handles incoming client connections simultaneously via multi-threading  
- Adding users  
- Removing users  
- Resetting user passwords  
- Displaying users  
- Send server/admin messages  
- Display connected users dynamically  
- Display status and chat messages  
- Emote system via Remote Method Invocation

Client functionality includes:  
- Connect to and disconnect from the server  
- Send messages to the server for broadcast  
- Send whispers to specific users  
- Call emote methods from server via RMI  
- Display connected users  
