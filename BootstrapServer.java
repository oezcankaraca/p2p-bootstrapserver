import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BootstrapServer {
    private static Map<String, Socket> peerSocketsMap = Collections.synchronizedMap(new HashMap<>());
    private static boolean firstConnectionReceived = false;

    public static void main(String[] args) {
        int timeoutAfterFirstConnection = 100000; // 100 Sekunden nach der ersten Verbindung
        int initialTimeout = 120000; // 120 Sekunden bis zur ersten Verbindung

        try {
            System.out.println("Bootstrap Server started...");
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                serverSocket.setSoTimeout(initialTimeout);

                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (!firstConnectionReceived) {
                            System.out.println("Erste Verbindung erhalten, starte Timeout.");
                            serverSocket.setSoTimeout(timeoutAfterFirstConnection);
                            firstConnectionReceived = true;
                        }
                        new ClientHandler(clientSocket).start();
                    } catch (SocketTimeoutException e) {
                        if (firstConnectionReceived) {
                            System.out.println("Timeout. Starte das Senden der Datei an alle Peers.");
                            byte[] fileData = readFileAsBytes("/app/mydocument.pdf");
                            sendFileToAllPeers(fileData);
                            break;
                        } else {
                            System.out.println("Keine erste Verbindung innerhalb des Timeouts, Server wird beendet.");
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println("Verbindungsfehler: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Konnte ServerSocket nicht öffnen: " + e.getMessage());
        } finally {
            System.out.println("Server wird heruntergefahren. Insgesamt verbundene Peers: " + peerSocketsMap.size());
            peerSocketsMap.values().forEach(socket -> {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignoriere Fehler beim Schließen des Sockets
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
                System.out.println("Dateiübertragung an " + peerId + " wurde erfolgreich durchgeführt.");
            } catch (IOException e) {
                System.err.println("Fehler beim Senden der Datei an Peer " + peerId + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignoriere Fehler beim Schließen des Sockets
                }
            }
        });
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                String message = (String) in.readObject();
                System.out.println("Message received: " + message);

                String peerId = extractPeerId(message);
                peerSocketsMap.put(peerId, socket);

                // Hier wird nicht geschlossen, um die Verbindung offen zu halten
                // Der ClientHandler schließt Streams und Socket nicht,
                // das wird später im Haupt-Thread getan, nachdem die Datei gesendet wurde.

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Fehler bei der Verarbeitung der Clientanforderung: " + e.getMessage());
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