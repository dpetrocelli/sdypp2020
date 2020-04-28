import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Worker implements Runnable {
	private Socket s;
	
	public Worker(Socket sock ) {
		s = sock;
	}
	
	@Override
	public void run() {
		try {
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			System.out.println("Por leer del socket");
			String msg = canalEntrada.readLine();
			System.out.println("Lei del socket");
			// Simular un procesamiento
			Thread.sleep(500);
			canalSalida.println(msg);
	
			System.out.println("2: Por leer del socket");
			msg = canalEntrada.readLine();
			System.out.println("2: Lei del socket");
			canalSalida.println(msg);

			s.close();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
	}

}
