var http = require('http'); // Import Node.js core module

var server = http.createServer(function (req, res) {   //create web server
    if (req.url == '/') { //check the URL of the current request
        
        // set response header
        res.writeHead(200, { 'Content-Type': 'text/html' }); 
        
        // set response content    
        res.write('<html><body><p>This is home Page.</p></body></html>');
        res.end();
    
    }
    else if (req.url == "/notls") {
        
        res.writeHead(200, { 'Content-Type': 'text/html' });
        var os = require("os");
        var value = os.hostname();
        res.write('<html><body><p>'+value+'</p><p>Ejemplo de servidor WebServer en JS en K8s</p><p>+ Est&aacute; corriendo en un k3s (versi&oacute;n ARMF o linux x86), quitando la versi&oacute;n de traefik v 1.7<br />+ Se configur&oacute; un ingress controller con traefik v2, que implica un CRD (CustomResourceDefinition)<br />** El ingress redirecciona los puertos 8000 como proxy reverso, en este caso en /notls<br />+ Se hizo un tunel http 8000 para hacer acceder desde internet (ngrok)</p><body><html>');
        
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

server.listen(80); //6 - listen for any incoming requests

console.log('Node.js web server at port 5000 is running..')