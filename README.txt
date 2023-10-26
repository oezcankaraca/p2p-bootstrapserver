1 - Creating images for bootstrapserver
sudo docker build -f dockerfile.bootstrapserver -t image-bootstrapserver .

2 - Creating images for peer
sudo docker build -f dockerfile.peer -t image-peer .

3 - Deploying containerlab
sudo containerlab deploy -t containerlab-topology.yml

4 - Inside of bootstrapserver container
sudo docker exec -it clab-mein_netzwerk-bootstrapserver /bin/sh

5 - Inside of peer1 container
sudo docker exec -it clab-mein_netzwerk-bootstrapserver /bin/sh

6 - Inside of peer2 container
sudo docker exec -it clab-mein_netzwerk-bootstrapserver /bin/sh

7 - Starting BootstrapServer Java
java BootstrapServer

8 - ls innerhalb peer2 Container ob die Datei schon da ist

8 - Starting Testbed Java innerhalb Host zum Dateisenden
java Testbed

9 - Starting Peer Java innerhalb peer2 Container
java Peer

9 - Starting Peer Java innerhalb peer1 Container
java Peer

10 - Destroying containerlab
sudo containerlab destroy -t containerlab-topology.yml --cleanup

11 - Delete from Images if the class Peer and BootstrapServer change
sudo docker images 
sudo docker rmi 



