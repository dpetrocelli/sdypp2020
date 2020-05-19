
public class Wrk implements Runnable {

	private ObjetoCompartido obj;
	private int sec;
	
	public Wrk(ObjetoCompartido o, int sec) {
		obj = o;
		this.sec = sec;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		// obj.setDato(obj.getDato()+3);
		
		System.out.println("Entrando:" + Thread.currentThread().getId());
		synchronized (obj) {
			System.out.println("Entre:" + Thread.currentThread().getId());
			int aux = obj.getDato();
			// Simulo un procesamiento
			try {
				Thread.sleep(sec * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Sumando:" + Thread.currentThread().getId());		
			aux = aux + 3;
			obj.setDato(aux);
		}
		System.out.println("Saliendo:" + Thread.currentThread().getId());
	}

}
