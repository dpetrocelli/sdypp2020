package C2_LU.TEst;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

public class ServerHijo implements Runnable{
	Socket cliente;
	
	public ServerHijo (Socket cliente) {
		this.cliente = cliente;
	}

	public void run() {
		
		// Paso 3 - Establecer los canales de comunicación
		// TCP en capa4 - 2 canales, canal de entrada (leer) ; canal de salida (escribir)
		try {
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (this.cliente.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (this.cliente.getOutputStream(), true);
			
			// Paso 4 - Leer algo de un cliente
			String msgEntrada = canalEntrada.readLine();
			
			MensajePrueba mp = new MensajePrueba(2, "David", "Petrocelli");
			
			Gson gson = new Gson();
			
			String jsonPrueba = gson.toJson(mp);
			
			
			msgEntrada+= "/ ACK del server.  Gracias por quedarte en casa ;)";
			
			// Paso 5 - Dar la respuesta al cliente
			canalSalida.println(jsonPrueba);
			
			// Paso 6 - Cerrar la conexión
			this.cliente.close();
			// TODO Auto-generated method stub
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

}
