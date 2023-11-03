import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BootstrapServer {
    private static Map<String, Socket> peerSocketsMap = Collections.synchronizedMap(new HashMap<>());
    private static boolean firstConnectionReceived = false;

    public static void main(String[] args) {
        int timeoutAfterFirstConnection = 100000; // 100 seconds after the first connection
        int initialTimeout = 120000; // 120 seconds until the first connection

        try {
            System.out.println("Bootstrap Server started...");
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                serverSocket.setSoTimeout(initialTimeout);

                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (!firstConnectionReceived) {
                            System.out.println("First connection received, starting timeout.");
                            serverSocket.setSoTimeout(timeoutAfterFirstConnection);
                            firstConnectionReceived = true;
                        }
                        new ClientHandler(clientSocket).start();
                    } catch (SocketTimeoutException e) {
                        if (firstConnectionReceived) {
                            System.out.println("Timeout reached. Starting to send the file to all peers.");
                            byte[] fileData = readFileAsBytes("/app/mydocument.pdf");
                            sendFileToAllPeers(fileData);
                            break;
                        } else {
                            System.out.println("No first connection within timeout, server will terminate.");
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println("Connection error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not open ServerSocket: " + e.getMessage());
        } finally {
            System.out.println("Server is shutting down. Total connected peers: " + peerSocketsMap.size());
            peerSocketsMap.values().forEach(socket -> {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore errors when closing the socket
                }
            });
        }
    }

    private static byte[] readFileAsBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    private static void sendFileToAllPeers(byte[] fileData) {
        peerSocketsMap.forEach((peerId, socket) -> {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(fileData);
                out.flush();
                System.out.println("File transfer to " + peerId + " was successful.");
            } catch (IOException e) {
                System.err.println("Error sending file to peer " + peerId + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore errors when closing the socket
                }
            }
        });
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
    
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
    
        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
    
                String message = (String) in.readObject();
                System.out.println("Message received: " + message);
    
                String peerId = extractPeerId(message);
                peerSocketsMap.put(peerId, socket);
    
                // Here it is not closed to keep the connection open
                // The ClientHandler does not close streams and socket,
                // it will be done later in the main thread after the file has been sent.
    
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error processing client request: " + e.getMessage());
            }
        }
    }    

    private static String extractPeerId(String message) {
        String[] parts = message.split("\\|");
        if (parts.length >= 2) {
            return parts[1];
        } else {
            return "";
        }
    }
}
