import java.io.*;
import java.security.*;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Testbed {
    public static void main(String[] args) {
        final String DESTINATION_CONTAINER_DIR = "/app/";
        final String SOURCE_FILE_DIR = "/home/ozcankaraca/Desktop/p2p-bootstrapserver/mydocument.pdf";

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nPlease select an option:\n");
                System.out.println("1 - Copy the file from the host to Container2");
                System.out.println("2 - Hash Werte matchen");
                System.out.println("3 - No action");

                System.out.print("\nEnter the number of your choice: ");

                int choiceOfOptions = -1;
                boolean validInput = false;
                while (!validInput) {
                    try {
                        choiceOfOptions = scanner.nextInt();
                        validInput = true;
                    } catch (InputMismatchException e) {
                        System.out.println("The input is invalid. Please enter a valid number.");
                        scanner.nextLine();
                    }
                }

                switch (choiceOfOptions) {
                    case 1:
                        try {
                            // Copy the file from the host to Container2
                            executeCommandWithoutDockerAPI("sudo", "docker", "cp", SOURCE_FILE_DIR,
                                    "clab-mein_netzwerk-peer2:" + DESTINATION_CONTAINER_DIR);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        try {
                            // Calculate the hash values of the original and received files for verification
                            String originalHash = calculateFileHash(SOURCE_FILE_DIR);
                            String containerName = "clab-mein_netzwerk-peer1";

                            String[] commands = {
                                "docker", "exec", containerName, "sh", "-c",
                                "sha256sum " + "receivedFile.pdf" + " | awk '{print $1}'"
                        };

                            Process process = Runtime.getRuntime().exec(commands);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String receivedHash = reader.readLine(); // Read the hash value
                            process.waitFor(); // Wait for the process to finish

                            if (receivedHash != null) {
                                System.out.println("The hash value of the file is: " + receivedHash);
                            } else {
                                System.out.println("Could not calculate the hash value.");
                            }

                            System.out.println("Original file hash: " + originalHash);
                            System.out.println("Received file hash: " + receivedHash);

                            // Compare the hash values to verify the integrity of the transferred file
                            if (originalHash.equals(receivedHash)) {
                                System.out.println("Hash values match. File transferred correctly!");
                            } else {
                                System.out.println("Hash values do not match. File transfer failed!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        // Exit without any action
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid action number. Please choose 1, 2 or 3.");
                        break;

                }
            }
        }
    }

    public static void executeCommandWithoutDockerAPI(String... commands) throws Exception {
        Process process = Runtime.getRuntime().exec(commands);
        process.waitFor();

        try (
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;

            // Print the stdout of the command to the console
            while ((line = stdInput.readLine()) != null) {
                System.out.println(line);
            }

            // Print the stderr of the command to the console
            while ((line = stdError.readLine()) != null) {
                System.err.println(line);
            }
        }
    }

    public static String calculateFileHash(String filePath) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        try (FileInputStream fis = new FileInputStream(filePath);
                DigestInputStream dis = new DigestInputStream(fis, sha256)) {

            // Read the file and update the digest
            while (dis.read() != -1) {
                // Empty loop to read the entire file for hashing
            }

            // Compute the hash
            byte[] digest = sha256.digest();
            return bytesToHex(digest);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
