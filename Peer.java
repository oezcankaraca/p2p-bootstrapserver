import java.io.*;
import java.net.*;

public class Peer {
    private static final int serverPort = 8080; // Port des Bootstrap-Servers
    private static final String fileSavePath = "/app/received-document.pdf";

    public static void main(String[] args) {
        // Resolve the bootstrap server's address.
        String serverAddress = resolveBootstrapServerAddress();
        // If the server address cannot be resolved, exit the program.
        if (serverAddress == null)
            return;

        // Get the local hostname from the environment variable 'HOSTNAME'.
        String myAddress = System.getenv("HOSTNAME");
        // If the 'HOSTNAME' variable is not set, print an error and exit.
        if (myAddress == null || myAddress.isEmpty()) {
            System.err.println("HOSTNAME environment variable is not set. Using local port number as Peer-ID.");
            return;
        }

        // Continuously attempt to connect to the server and receive a file.
        while (true) {
            try (
                    // Establish a socket connection to the server.
                    Socket socket = new Socket(serverAddress, serverPort);
                    // Set up an ObjectOutputStream to send messages to the server.
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    // Set up an ObjectInputStream to receive messages from the server.
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                // Compose a connection message and send it to the server.
                String connectMessage = "CONNECT|" + myAddress;
                out.writeObject(connectMessage);

                // Wait to receive a file from the server.
                receiveFileFromServer(socket);

                // If connection is successful and file is received, exit the loop.
                break;

            } catch (IOException e) {
                // Print an error message if the connection fails.
                System.err.println(
                        "Could not connect to the bootstrap server. Waiting 5 seconds before retrying.");
                try {
                    // Wait for 5 seconds before retrying to connect.
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    // If the thread is interrupted, re-interrupt the thread and exit the program.
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // This method resolves the address of the bootstrap server with retry logic.
    private static String resolveBootstrapServerAddress() {
        String serverAddress;
        int attempts = 0;
        while (attempts < 5) { // Try up to 5 times
            try {
                // Resolve the hostname "bootstrapserver" to an InetAddress object
                InetAddress inetAddress = InetAddress.getByName("bootstrapserver");
                serverAddress = inetAddress.getHostAddress(); // Get the IP address from the InetAddress object

                // Print out the resolved IP address
                System.out.println("The IP address of the bootstrap server is: " + serverAddress);
                // Return the resolved address on success
                return serverAddress;
            } catch (UnknownHostException e) {
                // Print a retry message on UnknownHostException
                System.err.println("Waiting for the bootstrap server, attempt " + (attempts + 1));
                // Increment the attempt counter
                attempts++;
                try {
                    // Wait for 10 seconds before retrying
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    // If the thread is interrupted, re-interrupt the thread and exit
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        /**
         * If the address could not be resolved after several attempts, print an error
         * message
         */
        System.err.println("Could not retrieve the IP address of the bootstrap server after several attempts.");
        return null; // Return null if the server address cannot be resolved
    }

    // This method is used to receive a file from the server via a socket.
    private static void receiveFileFromServer(Socket socket) {
        try {

            InputStream in = socket.getInputStream(); // Obtain the InputStream from the socket to read the data sent by
                                                      // the server.
            byte[] buffer = new byte[4096]; // A buffer to store blocks of data as they are received.
            int bytesRead; // Variable to keep track of the number of bytes read.

            /**
             * Creating a FileOutputStream to write the received data to a file.
             * The 'fileSavePath' variable should be a String that specifies where to save
             * the file.
             */
            try (FileOutputStream fileOut = new FileOutputStream(fileSavePath)) {
                // Read data from the InputStream until the end of stream is reached.
                while ((bytesRead = in.read(buffer)) != -1) {
                    // Write the bytes from the buffer into the file output stream.
                    fileOut.write(buffer, 0, bytesRead);
                }
            }

            /**
             * Print a message indicating that the file has been received and saved
             * successfully.
             */
            System.out.println("File has been received and saved to: " + fileSavePath);
        } catch (IOException e) {
            // In case of an IO exception, print an error message.
            System.err.println("Error receiving the file: " + e.getMessage());
        }
    }

}
