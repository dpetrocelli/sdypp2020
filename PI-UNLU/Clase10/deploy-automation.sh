# Paso 0, parar y eliminar todos los contenedores, para arrancar 0 
docker stop $(docker ps -a -q) ; docker rm $(docker ps -a -q)
rm ngrok-stable-linux-amd64.zip 
# Paso 1, me voy a la carpeta balancer
cd balancer
sleep 2
# Paso 2, hago el build de la imagen de LDB:NGINX
docker build . -t loadbalancernginx:latest
sleep 2
# Paso 3, vuelvo al raiz
cd ..
# Paso 4, me voy a la carpeta del server nodejs 
cd nodejsserver
# Paso 5, hago el build de la imagen node
docker build . -t ws-node-params:latest
# Paso 6, me vuelvo a raiz
cd ..
# Paso 7, levanto los servicios
export number=8080
for i in {1..3}
do
number=$(($number+1))
docker run -d --name ws$i -p $number:80 --env BACKEND_SERVER_NUMBER=ws$i --env PORT=80 ws-node-params
done
sleep 2
# Paso 8, levanto el balanceador
docker run -d --name loadbalancer -p 8080:80 loadbalancernginx:latest
sleep 2
echo "Se levanto la arquitectura HA del servidor WS"

#Paso 9, levantar el tunel ngrok para dar acceso publico
wget https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip -nc
unzip -f ngrok-stable-linux-amd64.zip 
./ngrok authtoken 1iHB3cmPx7ddxusFcGurSlIW67T_2VoUUefzjrdHBvtAmAuX5
./ngrok http 8080