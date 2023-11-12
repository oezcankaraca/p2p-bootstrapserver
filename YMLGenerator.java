import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for generating a YAML file to describe the topology
 * of a network.
 * It is specifically designed for use with Containerlab to define a
 * peer-to-peer network with
 * a bootstrap server and a dynamic number of peer nodes. Additionally, it
 * includes configurations
 * for monitoring services such as Prometheus, cAdvisor, and Grafana.
 */
public class YMLGenerator {

    // Path to save the generated YAML file
    private static final String CONTAINERLAB_TOPOLOGY_DIR = "/home/ozcankaraca/Desktop/p2p-bootstrapserver/containerlab-topology.yml";

    /**
     * Generates the topology YAML file with the specified number of peer nodes.
     * 
     * @param numberOfPeers The number of peer nodes to include in the topology.
     */
    public void generateTopologyFile(int numberOfPeers) {
        try (FileWriter fw = new FileWriter(CONTAINERLAB_TOPOLOGY_DIR)) {
            // Define the network name
            fw.write("name: topology\n");
            fw.write("prefix: my-p2p\n\n");
            fw.write("topology:\n");

            // Bootstrap server configuration
            fw.write("  nodes:\n");
            fw.write("    bootstrapserver:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-bootstrapserver\n");
            fw.write("      labels:\n");
            fw.write("        role: sender\n");
            fw.write("        group: server\n");
            fw.write("      binds:\n");
            fw.write("        - /home/ozcankaraca/Desktop/p2p-bootstrapserver/mydocument.pdf:/app/mydocument.pdf\n");
            fw.write("      exec:\n");
            fw.write("        - sleep 5\n");
            fw.write("      cmd: \"java -cp /app BootstrapServer\"\n\n");

            // Generate peer node configurations
            for (int i = 1; i <= numberOfPeers; i++) {
                fw.write("    peer" + i + ":\n");
                fw.write("      kind: linux\n");
                fw.write("      image: image-peer\n");
                fw.write("      labels:\n");
                fw.write("        role: receiver\n");
                fw.write("        group: peer\n");
                fw.write("      cmd: \"java -cp /app Peer\"\n\n");
            }

            // Prometheus configuration
            fw.write("    prometheus:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-prometheus\n");
            fw.write("      mgmt-ipv4: 172.20.20.100\n");
            fw.write("      labels:\n");
            fw.write("        role: visualisierung\n");
            fw.write("        group: monitoring\n");
            fw.write("      binds:\n");
            fw.write(
                    "        - /home/ozcankaraca/Desktop/p2p-bootstrapserver/prometheus.yml:/etc/prometheus/prometheus.yml\n");
            fw.write("      ports:\n");
            fw.write("        - \"9090:9090\"\n\n");

            // cAdvisor configuration
            fw.write("    cadvisor:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-cadvisor\n");
            fw.write("      mgmt-ipv4: 172.20.20.101\n");
            fw.write("      labels:\n");
            fw.write("        role: visualisierung\n");
            fw.write("        group: monitoring\n");
            fw.write("      binds:\n");
            fw.write("        - /:/rootfs:ro\n");
            fw.write("        - /var/run:/var/run:ro\n");
            fw.write("        - /sys:/sys:ro\n");
            fw.write("        - /var/snap/docker/common/var-lib-docker/:/var/lib/docker:ro\n");
            fw.write("      ports:\n");
            fw.write("        - \"8080:8080\"\n");
            fw.write("      env:\n");
            fw.write("        PARTICIPATION_DURATION: 30\n\n");
            fw.write("        DISCONNECT_FREQUENCY: 10\n");
            fw.write("        RECONNECT_FREQUENCY: 15\n\n");

            // Grafana configuration
            fw.write("    grafana:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-grafana\n");
            fw.write("      mgmt-ipv4: 172.20.20.102\n");
            fw.write("      labels:\n");
            fw.write("        role: visualisierung\n");
            fw.write("        group: monitoring\n");
            fw.write("      ports:\n");
            fw.write("        - \"3000:3000\"\n\n");

            // Node Exporter configuration
            fw.write("    nodeexporter:\n");
            fw.write("      kind: linux\n");
            fw.write("      image: image-nodeexporter\n");
            fw.write("      mgmt-ipv4: 172.20.20.103\n");
            fw.write("      labels:\n");
            fw.write("        role: visualisierung\n");
            fw.write("        group: monitoring\n");
            fw.write("      ports:\n");
            fw.write("        - \"9100:9100\"\n\n");

            // Define links between bootstrap server and peer nodes
            fw.write("  links:\n");
            for (int i = 1; i <= numberOfPeers; i++) {
                fw.write("    - endpoints: [\"bootstrapserver:eth" + i + "\", \"peer" + i + ":eth1\"]\n");
            }

        } catch (IOException e) {
            // Handle potential IO exceptions such as file not found
            e.printStackTrace();
            System.out.println("An error occurred while generating the topology YAML file.");
        }
    }

    public static void main(String[] args) {
        YMLGenerator generator = new YMLGenerator();
        // Pass the number of peers you want in the topology
        generator.generateTopologyFile(5);
    }
}