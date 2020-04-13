# Tutorial parte 2 
## Sección 1 -- Escalar Docker con Docker compose

Bienvenidos a la segunda parte del tutorial de Docker network. Este tutorial te guiará en:
- Continuar avanzando con los conceptos para trabajar con tus propias imágenes
- Continuar configurando contenedores utilizando Dockerfiles
- Trabajar con volúmenes compartidos (en Host) para persistir elementos en contenedores y actualizar cambios
- Comprender como pasar parámetros a los contenedores (ajustando el código java) desde DockerCompose
- Diseñar e implementar servicios distribuidos sobre Docker 
- Implementar un balanceador de cargas en el Host para redireccionar peticiones

## Sección 2 -- Manejo de parámetros en DockerCompose
Existen varias maneras para pasar parámetros a las aplicaciones desde DockerCompose.
En este caso nosotros utilizaremos las variables de entorno (environment) 
```
version: '3'
services:
  server:
    build: ./servidor
    ports:
      - "4444:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
      - nombre=Servidor 1
      - logName=logfile1
      - port=4444
```

