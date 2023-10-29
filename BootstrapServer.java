import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BootstrapServer
 * Author: [Özcan Karaca]
 * 
 * The BootstrapServer is responsible for managing peer statuses in a peer-to-peer network.
 * It accepts connections from peers, receives status updates, and maintains a map of peer statuses.
 * Each peer communicates its status, including whether it has a specific file or not.
 */
public class BootstrapServer {

    // A thread-safe map to store the status of each connected peer
    private static final Map<String, Boolean> peerStatusMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Bootstrap Server started...");

            // Continuously accept connections from new peers
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        }
    }

    /**
     * ClientHandler Class
     * Handles communication with connected peers.
     */
    private static class ClientHandler extends Thread {
        private final Socket socket;
    
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
    
        public void run() {
            System.out.println("Message Format --> Message received: Status of Connection|Name of Container|Status of File Transfer");
            
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                
                // Continuously listen for messages from the peer
                while (true) {
                    String message = (String) in.readObject();
                    System.out.println("Message received: " + message);
                    
                    // Splitting the received message into parts
                    String[] parts = message.split("\\|");
        
                    // Validate the message format
                    if (parts.length < 3) {
                        System.err.println("Invalid message format. Expected at least 3 parts, but got: " + parts.length);
                        break;
                    }
        
                    String command = parts[0];
                    String peerId = parts[1];
                    boolean hasFile = Boolean.parseBoolean(parts[2]);
                    
                    // Update the peer status map based on the received command
                    if ("CONNECT".equals(command) || "UPDATE_STATUS".equals(command)) {
                        peerStatusMap.put(peerId, hasFile);
                    }
                    
                    // Send the updated map back to the peer
                    out.writeObject(peerStatusMap);
                }
                
            } catch (EOFException e) {
                // End of stream reached, closing connection
            } catch (SocketException e) {
                System.err.println("Connection reset by peer");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                // Ensure the socket is closed when the handler thread exits
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }        
}
