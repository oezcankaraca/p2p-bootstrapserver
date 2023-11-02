import java.io.*;
import java.net.*;
import java.util.Map;

public class Peer {
    public static void main(String[] args) {
        String serverAddress;
        int serverPort = 8080; // Port des Bootstrap-Servers

        try {
            InetAddress inetAddress = InetAddress.getByName("bootstrapserver");
            serverAddress = inetAddress.getHostAddress();
            System.out.println("Die IP-Adresse des Bootstrap-Servers ist: " + serverAddress);
        } catch (UnknownHostException e) {
            System.err.println("Konnte die IP-Adresse des Bootstrap-Servers nicht abrufen.");
            e.printStackTrace();
            return;
        }

        // Bestimmen des Peer-Namens über eine Umgebungsvariable
        String myAddress = System.getenv("HOSTNAME");
        if (myAddress == null || myAddress.isEmpty()) {
            System.err.println("HOSTNAME Umgebungsvariable ist nicht gesetzt. Verwende lokale Portnummer als Peer-ID.");
            return; // Hier könnten Sie entscheiden, ob Sie stattdessen eine andere ID verwenden oder das Programm beenden möchten.
        }

        while (true) {
            try (Socket socket = new Socket(serverAddress, serverPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Senden einer CONNECT-Nachricht an den Bootstrap-Server mit dem Peer-Namen
                String connectMessage = "CONNECT|" + myAddress + "|true";
                out.writeObject(connectMessage);

                // Empfangen der Peer-Liste vom Bootstrap-Server
                Object obj = in.readObject();
                if (obj instanceof Map) {
                    Map<String, Boolean> peers = (Map<String, Boolean>) obj;
                    System.out.println("Map of peers: " + peers);
                } else {
                    System.err.println("Received object is not a Map<String, Boolean>");
                }

                break; // Verbindung erfolgreich, Schleife verlassen

            } catch (IOException e) {
                System.err.println("Konnte keine Verbindung zum Bootstrap-Server herstellen. Warte 5 Sekunden, dann versuche es erneut.");
                try {
                    Thread.sleep(5000); // Warte 5 Sekunden, bevor ein neuer Versuch unternommen wird
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Setze das Interrupt-Flag des Threads wieder
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
