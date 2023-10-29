import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Peer
 * Author: [Özcan Karaca]
 *
 * The Peer class represents a node in a peer-to-peer network, capable of
 * sending
 * and receiving files. It connects to a Bootstrap Server for coordination and
 * can
 * communicate with other peers to exchange files.
 */

public class Peer {

    public static void main(String[] args) {

        // Getting the directory where the program is currently running
        String currentWorkingDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDir);

        // Defining relative paths for the source and received files
        String relativeFilePathSource = "/mydocument.pdf";
        String relativeFilePathReceive = "/receivedFile.pdf";

        // Constructing full paths by combining the current directory with the relative
        // paths
        String fullFilePathSource = currentWorkingDir + relativeFilePathSource;
        System.out.println(fullFilePathSource);

        // Checking if the source file exists in the current directory
        boolean hasFileSource = Files.exists(Paths.get(fullFilePathSource));

        // Displaying whether the file exists or not
        if (hasFileSource) {
            System.out.println("The file exists in the current working directory.");
        } else {
            System.out.println("The file does not exist in the current working directory.");
        }

        String serverAddressBootstrapServer;

        try {
            // Resolving the IP address of the bootstrap server using its alias
            InetAddress inetAddressBootstrapServer = InetAddress.getByName("bootstrapserver");
            serverAddressBootstrapServer = inetAddressBootstrapServer.getHostAddress();
            System.out.println("The IP address of the Bootstrap Server is: " + serverAddressBootstrapServer);

        } catch (UnknownHostException e) {
            System.err.println("Could not retrieve the IP address of the Bootstrap Server.");
            e.printStackTrace();
            return;
        }

        int serverPort = 8080; // Defining the port number for the bootstrap server

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            // Establishing a socket connection to the bootstrap server
            socket = new Socket(serverAddressBootstrapServer, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Getting the hostname or creating a unique address if the hostname is null
            String myAddress = System.getenv("HOSTNAME");
            if (myAddress == null || myAddress.isEmpty()) {
                myAddress = "Peer_" + socket.getLocalPort(); // Unique address/ID for this peer
            }

            // Sending a CONNECT message to the Bootstrap Server
            String connectMessage = "CONNECT|" + myAddress + "|" + hasFileSource;
            out.writeObject(connectMessage);

            // Pausing the execution for a short period
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If the file exists, starting a server to share the file
            if (hasFileSource) {
                System.out.println("File transfer started");
                startServer(5000, fullFilePathSource);
            } else {
                // Receiving the file if it doesn't exist locally
                String serverAddressPeer;

                try {
                    // Resolving the IP address of the peer using its alias
                    InetAddress inetAddressPeer = InetAddress.getByName("peer2");
                    serverAddressPeer = inetAddressPeer.getHostAddress();
                    System.out.println("The IP address of Peer2 is: " + serverAddressPeer);

                } catch (UnknownHostException e) {
                    System.err.println("Could not retrieve the IP address of Peer1.");
                    e.printStackTrace();
                    return;
                }
                receiveFile(serverAddressPeer, 5000, currentWorkingDir + "/");
                System.out.println("File transfer completed");

            }

            // Delay to ensure that the file transfer is complete
            Thread.sleep(3000);
            String fullFilePathReceive = currentWorkingDir + relativeFilePathReceive;
            System.out.println(fullFilePathReceive);

            // Checking if the received file exists
            boolean hasFileReceive = Files.exists(Paths.get(fullFilePathReceive));

            System.out.println(hasFileReceive);

            // If the received file exists, reconnecting to the bootstrap server
            if (hasFileReceive) {
                System.out.println("I am here");
                String reconnectMessage = "CONNECT|" + myAddress + "|" + hasFileReceive;
                out.writeObject(reconnectMessage);
                out.flush(); // Ensuring that the message is sent
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Closing all the resources properly
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method starts a server that listens on a specified port and sends a file
     * to a connecting client.
     * The file is read from a specified file path and sent in chunks of data.
     *
     * @param port     The port on which the server listens for incoming
     *                 connections.
     * @param filePath The path of the file to be sent to the client.
     * @throws IOException If an I/O error occurs while handling the file or socket
     *                     connections.
     */
    public static void startServer(int port, String filePath) throws IOException {
        // Creating a ServerSocket to accept client connections on the specified port
        try (ServerSocket serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept(); // Accepting a connection from a client
                FileInputStream fis = new FileInputStream(filePath); // Reading the file to be sent
                BufferedInputStream bis = new BufferedInputStream(fis); // Buffering the file input stream
                OutputStream os = socket.getOutputStream()) { // Getting the output stream to send data to the client

            byte[] buffer = new byte[1024]; // Buffer to hold data chunks read from the file
            int bytesRead; // Variable to hold the number of bytes read from the file

            // Reading the file and sending it in chunks to the client
            while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
                os.write(buffer, 0, bytesRead); // Sending a chunk of data to the client
                os.flush(); // Flushing the output stream to ensure that the data is sent
            }
        }
    }

    /**
     * This method connects to a server, receives a file, and saves it to a
     * specified directory.
     * The received file data is written to the specified path with a predefined
     * name.
     *
     * @param serverIp   The IP address of the server from which the file is
     *                   received.
     * @param serverPort The port on which the server is listening for connections.
     * @param savePath   The directory where the received file will be saved.
     * @throws IOException If an I/O error occurs while handling the file or socket
     *                     connections.
     */
    public static void receiveFile(String serverIp, int serverPort, String savePath) throws IOException {
        String fileName = "receivedFile.pdf"; // Defining the name of the file to be saved

        // Creating a Socket to connect to the server
        try (Socket socket = new Socket(serverIp, serverPort); // Connecting to the server
                InputStream is = socket.getInputStream(); // Getting the input stream to receive data from the server
                FileOutputStream fos = new FileOutputStream(savePath + fileName); // Creating a file output stream to
                                                                                  // save the received file
                BufferedOutputStream bos = new BufferedOutputStream(fos)) { // Buffering the file output stream

            byte[] buffer = new byte[1024]; // Buffer to hold data chunks received from the server
            int bytesRead; // Variable to hold the number of bytes received from the server

            // Receiving the file in chunks and saving it
            while ((bytesRead = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, bytesRead); // Writing a chunk of data to the file
                bos.flush(); // Flushing the output stream to ensure that the data is written
            }
        }
    }

}
