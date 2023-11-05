import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BootstrapServer {
    private static Map<String, Socket> peerSocketsMap = Collections.synchronizedMap(new HashMap<>());
    private static boolean firstConnectionReceived = false;

    public static void main(String[] args) {
        // Set a timeout for 100 seconds after the first connection is received.
        int timeoutAfterFirstConnection = 100000;
        // Set an initial timeout for 120 seconds to wait for the first connection.
        int initialTimeout = 120000;

        try {
            System.out.println("Bootstrap Server started...");
            // Create a server socket that listens on port 8080.
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                // Set the initial timeout for accepting connections.
                serverSocket.setSoTimeout(initialTimeout);

                while (true) {
                    try {
                        // Accept a client connection.
                        Socket clientSocket = serverSocket.accept();
                        // If this is the first connection, set a new timeout and indicate that the first connection has been received.
                        if (!firstConnectionReceived) {
                            System.out.println("First connection received, starting timeout.");
                            serverSocket.setSoTimeout(timeoutAfterFirstConnection);
                            firstConnectionReceived = true;
                        }
                        // Start a new thread to handle the client connection.
                        new ClientHandler(clientSocket).start();
                    } catch (SocketTimeoutException e) {
                        // If the timeout occurs after the first connection, start sending the file to all peers.
                        if (firstConnectionReceived) {
                            System.out.println("Starting to send the file to all peers.");
                            byte[] fileData = readFileAsBytes("/app/mydocument.pdf");
                            sendFileToAllPeers(fileData);
                            break;
                        } else {
                            // If no first connection is made within the initial timeout, shut down the server.
                            System.out.println("No first connection within the timeout, shutting down the server.");
                            break;
                        }
                    } catch (IOException e) {
                        // Log a connection error.
                        System.out.println("Connection error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            // Log an error if the server socket cannot be opened.
            System.out.println("Could not open ServerSocket: " + e.getMessage());
        } finally {
            // Shut down the server and close all peer sockets.
            System.out.println("Server is shutting down. Total connected peers: " + peerSocketsMap.size());
            // Close all sockets in the peerSocketsMap.
            peerSocketsMap.values().forEach(socket -> {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore errors when closing the socket.
                }
            });
        }
    }

    // This helper method reads the file at the given filePath into a byte array.
    private static byte[] readFileAsBytes(String filePath) throws IOException {
        // Use the Files class from NIO to read all bytes from the file into a byte array.
        return Files.readAllBytes(Paths.get(filePath));
    }

    // This method sends the file data to all connected peers.
    private static void sendFileToAllPeers(byte[] fileData) {
        // Iterate over all peer sockets using the peerSocketsMap.
        peerSocketsMap.forEach((peerId, socket) -> {
            try {
                // Get the output stream of the socket to send data to the peer.
                OutputStream out = socket.getOutputStream();
                // Write the file data to the output stream.
                out.write(fileData);
                // Flush the stream to ensure all data is sent.
                out.flush();
                // Log a message indicating successful file transfer to the peer.
                System.out.println("File transfer from Server to " + peerId + " was successful.");
            } catch (IOException e) {
                // Log an error message if there's an issue sending the file to the peer.
                System.err.println("Error sending file to peer " + peerId + ": " + e.getMessage());
            } finally {
                try {
                    // Attempt to close the socket after the file transfer.
                    socket.close();
                } catch (IOException e) {
                    // Ignore errors that occur while closing the socket.
                }
            }
        });
    }

    /**
     * The ClientHandler class extends Thread, allowing it to run in its own thread
     * and handle client connections.
     */
    private static class ClientHandler extends Thread {
        private Socket socket;

        // Constructor that takes a socket connection to a client.
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        // The run method is called when the thread starts.
        public void run() {
            try {
                // Set up the output stream to send data to the client.
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                // Set up the input stream to receive data from the client.
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Read the message object from the client.
                String message = (String) in.readObject();
                // Print out the received message.
                System.out.println("Message received: " + message);

                // Extract the peer ID from the received message.
                String peerId = extractPeerId(message);
                // Store the client's socket in a map using the peer ID as the key.
                peerSocketsMap.put(peerId, socket);

                /**
                 * Streams and socket are not closed here to keep the connection open.
                 * The ClientHandler does not close the streams and socket,
                 * it will be done later in the main thread after the file has been sent.
                 */

            } catch (IOException | ClassNotFoundException e) {
                // Print an error message if there's a problem processing the client request.
                System.out.println("Error processing client request: " + e.getMessage());
            }
        }
    }

    /**
     * This method extracts the peer ID from a message string that is expected to be
     * in the format "COMMAND|CONTAINERNAME".
     */
    private static String extractPeerId(String message) {
        // Split the message into parts using the pipe character as a delimiter.
        String[] parts = message.split("\\|");
        // Check if the message contains at least two parts after splitting.
        if (parts.length >= 2) {
            // Return the second part of the message, which is the peer ID.
            return parts[1];
        } else {
            // If the message does not have two parts, return an empty string.
            return "";
        }
    }

}