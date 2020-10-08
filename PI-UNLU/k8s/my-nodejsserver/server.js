var http = require('http'); // Import Node.js core module

var server = http.createServer(function (req, res) {   //create web server
    if (req.url == '/') { //check the URL of the current request
        
        // set response header
        res.writeHead(200, { 'Content-Type': 'text/html' }); 
        
        // set response content    
        res.write('<html><body> HOME </body></html>');
        res.end();
    
    }
    else if (req.url == "/davidservice") {
        
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.write('<html><body><h1>Welcome a tu vieja gato !</h1>   <p>tipo estoy manejando tu request con ngrok y k3s con traefik ingress crd.</p><p>voy a shorar :D </p><p><em> Esto es una verchionchita de nginx:latest modificada para atender locations.</em></p></body></html>');
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

console.log('Node.js web server at port 80 is running..')