import java.io.*;
import java.net.*;
import java.util.*;

public class BootstrapServer {
    // Eine Map, um die Dateistatusinformationen für jeden Peer zu speichern
    private static Map<String, Boolean> peerStatusMap = Collections.synchronizedMap(new HashMap<>());
// Flagge, um zu überprüfen, ob der Server die erste Verbindung erhalten hat
private static boolean firstConnectionReceived = false;

public static void main(String[] args) {
    // Timeout nach der ersten Verbindung (15 Minuten = 900000 Millisekunden)
    int timeoutAfterFirstConnection = 100000;

    // Initialer Timeout vor der ersten Verbindung
    int initialTimeout = 120000;

    try {
        System.out.println("Bootstrap Server started...");

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            serverSocket.setSoTimeout(initialTimeout);  // Setze das anfängliche Timeout für accept

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();  // Warte auf eine Verbindung
                    
                    // Wenn es die erste Verbindung ist, starte den Timeout nach der ersten Verbindung
                    if (!firstConnectionReceived) {
                        System.out.println("Erste Verbindung erhalten, starte Timeout.");
                        serverSocket.setSoTimeout(timeoutAfterFirstConnection);
                        firstConnectionReceived = true;
                    }
                    
                    new ClientHandler(clientSocket).start();      // Verarbeite die Verbindung
                } catch (SocketTimeoutException e) {
                    if (firstConnectionReceived) {
                        System.out.println("Keine weiteren Verbindungen nach der ersten Verbindung innerhalb von " + (timeoutAfterFirstConnection / 1000) + " Sekunden, Server wird beendet.");
                    } else {
                        System.out.println("Keine erste Verbindung innerhalb von " + (initialTimeout / 1000) + " Sekunden, Server wird beendet.");
                    }
                    break;  // Beendet die while-Schleife und damit den Server
                } catch (IOException e) {
                    System.out.println("Verbindungsfehler: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Konnte ServerSocket nicht öffnen: " + e.getMessage());
        }
    } finally {
        // Hier könnten Sie am Ende des Programms weitere Bereinigungen durchführen
        System.out.println("Server wird heruntergefahren.");
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
                System.out.println("Fehler bei der Verarbeitung der Clientanforderung: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Methode zum Extrahieren der Peer-ID aus der Nachricht
    private static String extractPeerId(String message) {
        // Teilt die Nachricht anhand des Trennzeichens "|"
        String[] parts = message.split("\\|");

        // Überprüft, ob die Nachricht mindestens 2 Teile hat (COMMAND, PEER_ID)
        if (parts.length >= 2) {
            return parts[1]; // Gibt den zweiten Teil als Peer-ID zurück
        } else {
            // Gibt eine leere Zeichenkette zurück, wenn die Nachricht nicht das erwartete Format hat
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
