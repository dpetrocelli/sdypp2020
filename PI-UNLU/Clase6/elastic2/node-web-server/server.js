var http = require('http');
var url = require('url');
//http://localhost:8080/echo?message=Hello 
var server = new http.Server(function(req, res) {
 
    var urlParsed = url.parse(req.url, true);
    console.log (urlParsed.query.message);
    if (urlParsed.pathname == '/echo' && urlParsed.query.message) {
        res.setHeader('Cache-control', 'no-cache');
        res.end( urlParsed.query.message );
        
    } else {
        res.statusCode = 404; // Not Found
        res.end("Page not found");
        
    }
});
 
server.listen(8080);