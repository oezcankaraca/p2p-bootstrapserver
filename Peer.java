import java.io.*;
import java.net.*;

public class Peer {
    private static final int serverPort = 8080; // Port des Bootstrap-Servers
    private static final String fileSavePath = "/app/received-document.pdf";

    public static void main(String[] args) {
        String serverAddress;

        try {
            InetAddress inetAddress = InetAddress.getByName("bootstrapserver");
            serverAddress = inetAddress.getHostAddress();
            System.out.println("Die IP-Adresse des Bootstrap-Servers ist: " + serverAddress);
        } catch (UnknownHostException e) {
            System.err.println("Konnte die IP-Adresse des Bootstrap-Servers nicht abrufen.");
            e.printStackTrace();
            return;
        }

        String myAddress = System.getenv("HOSTNAME");
        if (myAddress == null || myAddress.isEmpty()) {
            System.err.println("HOSTNAME Umgebungsvariable ist nicht gesetzt. Verwende lokale Portnummer als Peer-ID.");
            return;
        }

        while (true) {
            try (Socket socket = new Socket(serverAddress, serverPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                String connectMessage = "CONNECT|" + myAddress;
                out.writeObject(connectMessage);

                // Warten auf das File
                receiveFileFromServer(socket);

                break; // Verbindung erfolgreich, Schleife verlassen

            } catch (IOException e) {
                System.err.println("Konnte keine Verbindung zum Bootstrap-Server herstellen. Warte 5 Sekunden, dann versuche es erneut.");
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
            System.out.println("Datei wurde empfangen und gespeichert: " + fileSavePath);
        } catch (IOException e) {
            System.err.println("Fehler beim Empfangen der Datei: " + e.getMessage());
        }
    }
}
