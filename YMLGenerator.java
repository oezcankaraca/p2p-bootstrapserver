import java.io.FileWriter;
import java.io.IOException;

public class YMLGenerator {

    private static final String CONTAINERLAB_TOPOLOGY_DIR = "/home/ozcankaraca/Desktop/p2p-bootstrapserver/containerlab-topology.yml";
    public void generateTopologyFile(int numberOfPeers) {
        try (FileWriter fw = new FileWriter(CONTAINERLAB_TOPOLOGY_DIR)) {
            fw.write("name: mein_netzwerk\n\n");
            fw.write("topology:\n");
            fw.write("  nodes:\n");
            fw.write("    bootstrapserver:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-bootstrapserver\n");
            fw.write("      cmd: \"java -cp /app BootstrapServer\"\n\n");

            for (int i = 1; i <= numberOfPeers; i++) {
                fw.write("    peer" + i + ":\n");
                fw.write("      kind: linux\n");
                fw.write("      image: image-peer\n");
                fw.write("      cmd: \"java -cp /app Peer\"\n");
            }

            fw.write("\n  links:\n");
            for (int i = 1; i <= numberOfPeers; i++) {
                fw.write("    - endpoints: [\"bootstrapserver:eth" + i + "\", \"peer" + i + ":eth1\"]\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while generating the topology YML file.");
        }
    }

    public static void main(String[] args) {
        YMLGenerator generator = new YMLGenerator();
        generator.generateTopologyFile(10); // Generate for 50 peers
        System.out.println("\nYML topology file generated successfully for 50 peers.");
    }
}

