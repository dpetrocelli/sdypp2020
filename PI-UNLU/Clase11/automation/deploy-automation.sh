cd ..
mvn clean ; mvn package;
cd target
cp demo-0.0.1-SNAPSHOT.jar ../automation/server.jar
cd ..
cd automation
docker build . -t miprimerwebserver:latest
docker stop server ; docker rm --force server ; docker run -d --name server -p 8080:8080 miprimerwebserver