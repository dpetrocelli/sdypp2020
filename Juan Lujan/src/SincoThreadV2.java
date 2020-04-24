
public class SincoThreadV2 implements Runnable {
	private ObjetoCompartido obj;
	
	public SincoThreadV2(ObjetoCompartido o) {
		obj = o;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("T2 entrando");
		synchronized (obj) {
			//////
			//synchronized (obj2)
			System.out.println("T2 entre");
			int valor = obj.getDato();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// procesamiento (valor)
			valor = valor + 5;
			obj.setDato(valor);
		}
		System.out.println("T2 saliendo");
	}

}
