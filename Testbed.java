import java.io.*;


public class Testbed {
    public static void main(String[] args) {
        final String DESTINATION_CONTAINER_DIR = "/app/";
      
        final String SOURCE_FILE_DIR = "/home/ozcankaraca/Desktop/p2p-bootstrapserver/mydocument.pdf";

        try {
            // Copy the file from the host to Container2
            executeCommandWithoutDockerAPI("sudo", "docker", "cp", SOURCE_FILE_DIR,
                    "clab-mein_netzwerk-peer2:" + DESTINATION_CONTAINER_DIR);
        } catch (Exception e) {
            e.printStackTrace();
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
}
