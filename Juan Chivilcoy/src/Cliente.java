import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

//extends Thread
public class Cliente implements Runnable {

	@Override
	public void run() {
		try {
			// Paso 1 - Conectarse con el servidor
			// TCP capa 4, entonces como me conecto a un server?
			//System.out.println("Cliente: por conectarse");
			Socket s = new Socket ("127.0.0.1", 9000);
			//System.out.println("Cliente: conexion establecida");
			
			// Paso 2 - Establecer los canales de comunicación
			// TCP en capa4 - 2 canales, canal de entrada (leer) ; canal de salida (escribir)
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			
			// Paso 3 - Enviar petición al servidor 
			String msg = "Cliente " + Thread.currentThread().getId();
			canalSalida.println("MSG DEL CLIENTE: " + msg);
			
			// Paso 4 - Recibir la respuesta
			String msgRespuesta = canalEntrada.readLine();
			System.out.println(" MSG RESPUESTA: "+msgRespuesta);
			
			msg = "Cliente " + Thread.currentThread().getId();
			canalSalida.println("2: MSG DEL CLIENTE: " + msg);
			
			// Paso 4 - Recibir la respuesta
			msgRespuesta = canalEntrada.readLine();
			System.out.println("2: MSG RESPUESTA: "+msgRespuesta);

			// Paso 5 - Cerrar la conexión
			s.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ArrayList<Thread> threads = new ArrayList<Thread>();

		Timestamp ts = new Timestamp(System.currentTimeMillis());
		System.out.println("Start: " + ts);
		Thread t = null;
		for (int i = 0; i < 1000; i++) {
			t = new Thread(new Cliente());
			threads.add(t);
			t.start();
		}

		// Barrera
		try {
			for (Thread s : threads) {
				s.join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ts = new Timestamp(System.currentTimeMillis());
		System.out.println("End: " + ts);
		System.out.println("Fin");
	}
}
