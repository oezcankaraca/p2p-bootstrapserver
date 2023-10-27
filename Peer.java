import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Peer {

    public static void main(String[] args) {

        // Get the current working directory
        String currentWorkingDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDir);

        // Path to the file you want to check, relative to the current working directory
        String relativeFilePathSource = "/mydocument.pdf";
        String relativeFilePathReceive = "/receivedFile.pdf";

        // Combine the paths to get the full path to the file
        String fullFilePathSource = currentWorkingDir + relativeFilePathSource;
        System.out.println(fullFilePathSource);

        // Check if the file exists
        boolean hasFileSource = Files.exists(Paths.get(fullFilePathSource));

        if (hasFileSource) {
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

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket(serverAddressBootstrapServer, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            String myAddress = System.getenv("HOSTNAME");
            if (myAddress == null || myAddress.isEmpty()) {
                myAddress = "Peer_" + socket.getLocalPort(); // Unique address/ID for this peer
            }

            // Sending a CONNECT message to the Bootstrap Server
            String connectMessage = "CONNECT|" + myAddress + "|" + hasFileSource;
            out.writeObject(connectMessage);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // If the peer has the file, start the server to send the file to other peers
            if (hasFileSource) {
                System.out.println("Dateiübertragung started");
                startServer(5000, fullFilePathSource);
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

            Thread.sleep(3000);
            String fullFilePathReceive = currentWorkingDir + relativeFilePathReceive;
            System.out.println(fullFilePathReceive);

            boolean hasFileReceive = Files.exists(Paths.get(fullFilePathReceive));

            System.out.println(hasFileReceive);

            if (hasFileReceive) {
                System.out.println("I am here");
                String reconnectMessage = "CONNECT|" + myAddress + "|" + hasFileReceive;
                out.writeObject(reconnectMessage);
                out.flush(); // Stellen Sie sicher, dass die Nachricht gesendet wird
            }
        
        } catch (IOException  | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
