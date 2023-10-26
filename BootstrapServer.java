import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BootstrapServer {

    private static final Map<String, Boolean> peerStatusMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Bootstrap Server started...");

            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                String message = (String) in.readObject();
                System.out.println("Message received: " + message);

                String[] parts = message.split("\\|");
                String command = parts[0];
                String peerId = parts[1];
                boolean hasFile = Boolean.parseBoolean(parts[2]);

                if ("CONNECT".equals(command) || "UPDATE_STATUS".equals(command)) {
                    peerStatusMap.put(peerId, hasFile);
                }

                out.writeObject(peerStatusMap); // Send the updated map back to the peer

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
