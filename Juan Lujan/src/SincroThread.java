
public class SincroThread implements Runnable {
	private ObjetoCompartido obj;
	
	public SincroThread(ObjetoCompartido o) {
		obj = o;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("T1: entrando");
		// synchronized(obj2)
		// -----
		synchronized (obj) {
			System.out.println("T1: entre");
			int valor = obj.getDato();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// procesamiento (valor)
			valor = valor + 3;
			obj.setDato(valor);
		}
		System.out.println("T1: saliendo");
	}

}
