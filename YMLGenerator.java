import java.io.FileWriter;
import java.io.IOException;

public class YMLGenerator {

    private static final String CONTAINERLAB_TOPOLOGY_DIR = "/home/ozcankaraca/Desktop/p2p-bootstrapserver/containerlab-topology.yml";
    
    public void generateTopologyFile(int numberOfPeers) {
        try (FileWriter fw = new FileWriter(CONTAINERLAB_TOPOLOGY_DIR)) {
            fw.write("name: my-topology\n");
            fw.write("prefix: p2p\n\n");
            fw.write("topology:\n");
            fw.write("  nodes:\n");
            fw.write("    bootstrapserver:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-bootstrapserver\n");
            fw.write("      network-mode: my-network\n");
            fw.write("      binds:\n");
            fw.write("        - /home/ozcankaraca/Desktop/p2p-bootstrapserver/mydocument.pdf:/app/mydocument.pdf\n");
            fw.write("      exec:\n");
            fw.write("        - sleep 5\n");
            fw.write("      cmd: \"java -cp /app BootstrapServer\"\n");
            fw.write("      group: Server\n\n");

            for (int i = 1; i <= numberOfPeers; i++) {
                fw.write("    peer" + i + ":\n");
                fw.write("      kind: linux\n");
                fw.write("      image: image-peer\n");
                fw.write("      network-mode: my-network\n");
                fw.write("      exec:\n");
                fw.write("        - sleep 5\n");
                fw.write("      cmd: \"java -cp /app Peer\"\n");
                fw.write("      group: Peers\n\n");
            }

            fw.write("  links:\n");
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
        // Pass the number of peers you want in the topology
        generator.generateTopologyFile(5); 
    }
}

