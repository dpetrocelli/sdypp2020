package LUWS1.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    public WebController(){

    }
    // Spring Initializr
    //https://start.spring.io/
    @GetMapping("isWorking")
    public ResponseEntity<Boolean> working() throws InterruptedException {
        System.out.println ("LLEGO UNA PETICION");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK); //200 ok
        //return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST); // 404 400 500
    }

    // 1 - por la web vamos a crear una vm - linux - ssh
        // 1. a través del SDK Java (software development kit)
        // API para que vos puedas utilizar los recuersos de ese servicio
        // SDK (API) Azure -> pom - dependencias maven (portal vs codigo)
    // 2 - copiar al server virtual ssh un proyecto (copiar un jar) + copiar un script bash (java)
        // Desde consola: ssh -p usuario@equipo / scp -P filelocal@servidor:/directorioremoto
        // Desde java:
                // Alt 1: Process Builder / Runtime .exec ("comandos") ; ("file.sh/ps1")
                // Alt 2: jsch - Java Secure Channel - pom - maven
    // 3 - empaquetar como dijimos el jar
    // 4 - deploy en docker local
    // 5 - deploy en kubernetes (cloud) - IP PUBLICA + MDB
    // 6 - ffmpeg desde consola
    // 7 - ffmpeg desde java (comprimir un video X en una calidad Y) - // Alt 1: Process Builder
    // 8 - empaquetado de java con ffmpeg (parámetros) para comprimir deployado en un K8s
    // 9 - WebServer diga quien lo resuelve W1 ; W2 ; W3; + LB IP PUB - suba el file al algún lugar
}
