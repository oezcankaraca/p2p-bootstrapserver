import java.io.*;
import java.net.*;
import java.util.List;

public class Peer {
    public static void main(String[] args) {
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

            String myAddress = "Peer_" + socket.getLocalPort(); // Eindeutige Adresse/ID für diesen Peer
            out.writeObject(myAddress);

            Object obj = in.readObject();
            if (obj instanceof List) {
                List<String> peers = (List<String>) obj;
                System.out.println("List of peers: " + peers);
            } else {
                System.err.println("Received object is not a List<String>");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
