import java.io.*;
import java.net.*;

public class Peer {
    private static final int serverPort = 8080; // Port of the Bootstrap Server
    private static final String fileSavePath = "/app/received-document.pdf"; // Path to save the received file

    public static void main(String[] args) {
        String serverAddress;

        try {
            InetAddress inetAddress = InetAddress.getByName("bootstrapserver");
            serverAddress = inetAddress.getHostAddress();
            System.out.println("The IP address of the Bootstrap Server is: " + serverAddress);
        } catch (UnknownHostException e) {
            System.err.println("Could not retrieve the IP address of the Bootstrap Server.");
            e.printStackTrace();
            return;
        }

        String myAddress = System.getenv("HOSTNAME");
        if (myAddress == null || myAddress.isEmpty()) {
            System.err.println("HOSTNAME environment variable is not set. Using local port number as Peer ID.");
            return;
        }

        while (true) {
            try (Socket socket = new Socket(serverAddress, serverPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                String connectMessage = "CONNECT|" + myAddress;
                out.writeObject(connectMessage);

                // Waiting for the file
                receiveFileFromServer(socket);

                break; // Connection successful, exit the loop

            } catch (IOException e) {
                System.err.println("Could not establish a connection to the Bootstrap Server. Waiting 5 seconds, then retry.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void receiveFileFromServer(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            try (FileOutputStream fileOut = new FileOutputStream(fileSavePath)) {
                while ((bytesRead = in.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("File has been received and saved: " + fileSavePath);
        } catch (IOException e) {
            System.err.println("Error receiving the file: " + e.getMessage());
        }
    }
}
