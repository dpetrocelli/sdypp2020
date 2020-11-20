// COMENTADO COMO CORRER LOS DOS PARAMETROS
// Correr versión desktop -> BACKEND_SERVER_NUMBER=ws1 PORT=8085 node server.js
// Correr versión dockerizada -> Hacer docker build -> ws-node-params
// docker run --name ws1 -p 8080:80 --env BACKEND_SERVER_NUMBER=ws1 --env PORT=80 ws-node-params
var http = require('http'); // Import Node.js core module

var server = http.createServer(function (req, res) {   //create web server
    if (req.url == '/') { //check the URL of the current request
        
        // set response header
        res.writeHead(200, { 'Content-Type': 'text/html' }); 
        
        // set response content    
        res.write(`<html><body> HOME: ${process.env.BACKEND_SERVER_NUMBER}  </body></html>`);
        res.end();
    
    }
    else if (req.url == "/davidservice") {
        
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.write('<html><body><h1>Welcome a sitio !</h1>   <p>tipo estoy manejando tu request con ngrok y k3s con traefik ingress crd.</p><p>voy a shorar :D </p><p><em> Esto es una verchionchita de nginx:latest modificada para atender locations.</em></p></body></html>');
        res.end();
    
    }
    else if (req.url == "/admin") {
        
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.write('<html><body><p>This is admin Page.</p></body></html>');
        res.end();
    
    }
    else
        res.end('Invalid Request!');

});

server.listen(process.env.PORT); //6 - listen for any incoming requests

console.log(`Node.js web server at port ${process.env.PORT} is running..`)