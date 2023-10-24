1 - Creating images for bootstrapserver
sudo docker build -f dockerfile.bootstrapserver -t image-bootstrapserver .

2 - Creating images for peer
sudo docker build -f dockerfile.peer -t image-peer .

3 - Deploying containerlab
sudo containerlab deploy -t containerlab-topology.yml

4 - Inside of bootstrapserver container
sudo docker exec -it clab-mein_netzwerk-bootstrapserver /bin/sh

5 - Starting bootstrapserver Java
java BootstrapServer

5 - Inside of peer container
sudo docker exec -it clab-mein_netzwerk-peer /bin/sh

6 - Starting peer Java
java Peer


