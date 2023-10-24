1 - Creating network for bootstrapserver and peer container
sudo docker network create my_network

2 - Creating images for bootstrapserver
sudo docker build -f dockerfile.bootstrapserver -t image-bootstrapserver .

3 - Creating container for bootstrapserver
sudo docker run --network my_network --network-alias bootstrapserver image-bootstrapserver

4 - Creating images for peer
sudo docker build -f dockerfile.peer -t image-peer .

5 - Creating container for peer 
sudo docker run --network my_network --network-alias peer image-peer




