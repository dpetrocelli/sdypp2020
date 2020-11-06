# 1 crear las folder
sudo mkdir /tmp/rabbit; sudo mkdir /tmp/rabbit/stats ; sudo mkdir /tmp/rabbit/node1 ; sudo mkdir /tmp/rabbit/node2 ; sudo mkdir /tmp/rabbit/node3; sudo chmod 777 -R /tmp/rabbit

# 2 bajar la version vieja
docker-compose -f rabbit-prueba-compose.yaml down

sleep 5

# 3 levantar la version nueva
docker-compose -f rabbit-prueba-compose.yaml up
