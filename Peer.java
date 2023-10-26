import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Peer {
    public static void main(String[] args) {
    
        // Get the current working directory
        String currentWorkingDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDir);

        // Path to the file you want to check, relative to the current working directory
        String relativeFilePath = "/mydocument.pdf";

        // Combine the paths to get the full path to the file
        String fullFilePath = currentWorkingDir + relativeFilePath;
        System.out.println(fullFilePath);

        // Check if the file exists
        boolean hasFile = Files.exists(Paths.get(fullFilePath));

        if (hasFile) {
            System.out.println("The file exists in the current working directory.");
        } else {
            System.out.println("The file does not exist in the current working directory.");
        }

        String serverAddress;

        try {
            // Verwenden Sie den Netzwerkalias, um die IP-Adresse zu erhalten
            InetAddress inetAddress = InetAddress.getByName("bootstrapserver");
            serverAddress = inetAddress.getHostAddress();
            System.out.println("Die IP-Adresse des Bootstrap-Servers ist: " + serverAddress);

        } catch (UnknownHostException e) {
            System.err.println("Konnte die IP-Adresse des Bootstrap-Servers nicht abrufen.");
            e.printStackTrace();
            return;
        }

        int serverPort = 8080; // Port des Bootstrap-Servers

        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            String myAddress = "Peer_" + socket.getLocalPort(); // Unique address/ID for this peer

            // Sending a CONNECT message to the Bootstrap Server
            String connectMessage = "CONNECT|" + myAddress + "|" + hasFile; // Assuming the peer doesn't have the file
            out.writeObject(connectMessage);

            // Receiving the peer status map from the Bootstrap Server
            Map<String, Boolean> peerStatusMap = (Map<String, Boolean>) in.readObject();
            System.out.println("Map of peers: " + peerStatusMap);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}