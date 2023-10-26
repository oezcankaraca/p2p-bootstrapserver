import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Peer {
    private String peerId;

    public Peer(String peerId) {
        this.peerId = peerId;
    }

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

        String serverAddressBootstrapServer;

        try {
            // Verwenden Sie den Netzwerkalias, um die IP-Adresse zu erhalten
            InetAddress inetAddressBootstrapServer = InetAddress.getByName("bootstrapserver");
            serverAddressBootstrapServer = inetAddressBootstrapServer.getHostAddress();
            System.out.println("Die IP-Adresse des Bootstrap-Servers ist: " + serverAddressBootstrapServer);

        } catch (UnknownHostException e) {
            System.err.println("Konnte die IP-Adresse des Bootstrap-Servers nicht abrufen.");
            e.printStackTrace();
            return;
        }

        int serverPort = 8080; // Port des Bootstrap-Servers

        try (Socket socket = new Socket(serverAddressBootstrapServer, serverPort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            String myAddress = "Peer_" + socket.getLocalPort(); // Unique address/ID for this peer

            // Sending a CONNECT message to the Bootstrap Server
            String connectMessage = "CONNECT|" + myAddress + "|" + hasFile; // Assuming the peer doesn't have the file
            out.writeObject(connectMessage);

            // Receiving the peer status map from the Bootstrap Server
            Map<String, Boolean> peerStatusMap = (Map<String, Boolean>) in.readObject();
            System.out.println("Map of peers: " + peerStatusMap);

            // If the peer has the file, start the server to send the file to other peers
            if (hasFile) {
                System.out.println("Dateiübertragung started");
                startServer(5000, fullFilePath);
            } else {
                String serverAddressPeer;

                try {
                    // Verwenden Sie den Netzwerkalias, um die IP-Adresse zu erhalten
                    InetAddress inetAddressPeer = InetAddress.getByName("peer2");
                    serverAddressPeer = inetAddressPeer.getHostAddress();
                    System.out.println("Die IP-Adresse des Peer2 ist: " + serverAddressPeer);

                } catch (UnknownHostException e) {
                    System.err.println("Konnte die IP-Adresse des Peer1 nicht abrufen.");
                    e.printStackTrace();
                    return;
                }
                receiveFile(serverAddressPeer, 5000, currentWorkingDir + "/");
                System.out.println("Dateiübertragung beendet");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startServer(int port, String filePath) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();
                FileInputStream fis = new FileInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream os = socket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
                os.write(buffer, 0, bytesRead);
                os.flush();
            }
        }
    }

    public static void receiveFile(String serverIp, int serverPort, String savePath) throws IOException {
        String fileName = "receivedFile.pdf"; // Sie können den Dateinamen dynamisch festlegen oder erhalten

        try (Socket socket = new Socket(serverIp, serverPort);
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream(savePath + fileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, bytesRead);
                bos.flush();
            }
        }
    }

}
