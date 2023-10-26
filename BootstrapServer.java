import java.io.*;
import java.net.*;
import java.util.*;

public class BootstrapServer {
    // Eine Map, um die Dateistatusinformationen für jeden Peer zu speichern
    private static Map<String, Boolean> peerStatusMap = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Bootstrap Server started...");

            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                String message = (String) in.readObject();
                System.out.println("Message received: " + message);

                if (message.startsWith("CONNECT") || message.startsWith("UPDATE_STATUS")) {
                    String peerId = extractPeerId(message);
                    boolean hasFile = extractFileStatus(message);
                    peerStatusMap.put(peerId, hasFile);
                    out.writeObject(peerStatusMap); // sendet die aktualisierte Map zurück an den Peer
                } else if (message.startsWith("GET_PEER_LIST")) {
                    out.writeObject(peerStatusMap); // sendet die Map der Peers und deren Status
                }                

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // Methode zum Extrahieren der Peer-ID aus der Nachricht
    private static String extractPeerId(String message) {
        // Teilt die Nachricht anhand des Trennzeichens "|"
        String[] parts = message.split("\\|");

        // Überprüft, ob die Nachricht mindestens 3 Teile hat (COMMAND, PEER_ID,
        // FILE_STATUS)
        if (parts.length >= 2) {
            return parts[1]; // Gibt den zweiten Teil als Peer-ID zurück
        } else {
            // Gibt eine leere Zeichenkette zurück, wenn die Nachricht nicht das erwartete
            // Format hat
            return "";
        }
    }

    // Methode zum Extrahieren des Dateistatus aus der Nachricht
    private static boolean extractFileStatus(String message) {
        // Teilt die Nachricht anhand des Trennzeichens "|"
        String[] parts = message.split("\\|");

        // Überprüft, ob die Nachricht mindestens 3 Teile hat
        if (parts.length >= 3) {
            return "true".equalsIgnoreCase(parts[2]); // Überprüft, ob der dritte Teil "true" ist
        } else {
            // Gibt false zurück, wenn die Nachricht nicht das erwartete Format hat
            return false;
        }
    }

}
