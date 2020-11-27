package Class11.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST})

public class RestServer {
    // -> nombre y tipo del método en escucha (GET /test)
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    // Además le digo que estoy esperando una variable por "header" que es "name"
    //serverip:puerto/test?name="algo"
    public ResponseEntity<String> listJobs(@RequestParam("name") String nombre) throws InterruptedException {
        // ALGO ->
        return new ResponseEntity<String>(("Hola tu nombre es: "+nombre).trim(), HttpStatus.OK);
    }
}
