package C2_LU.Threads;

import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ArrayList<Thread> tlist = new ArrayList<Thread>();
        
        // [STEP 1 ] - Instancio Clase (Obje)
        Hijo hi = new Hijo (10000);
        // [STEP 2 ] - creo Thread y asigno objeto
        Thread hiThread = new Thread (hi);
        System.err.println("[THSTATE]: "+hiThread.getState().toString());
        tlist.add(hiThread);
        hiThread.setName("HILO1");
        // [STEP 3] - run 
        hiThread.start();
        System.err.println("[THSTATE]: "+hiThread.getState().toString());
        System.out.println(" SUPER INTERRUPT");
       /* try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        //hiThread.interrupt();
        
        System.out.println(" PASAMOS EL INTERRUPT PEERO SIGUE LOOPEANDO EL HIJO");
       */
        
     
        
        
        try {
        	while (!(tlist.isEmpty())) {
        		for (Thread thread : tlist) {
    				if (thread.getState().toString().startsWith("TER")) {
    					tlist.remove(thread);
    					System.err.println(" ELIMINAMOS EL THREAD: "+thread.getId());
    					Thread.sleep(10);
    				}
            		//thread.join(); // 10000 ; 500 ; 300 ; 150
    				// 
    				//tlist.remove(thread);
    			}
        	}
        	
        	
		} catch (Exception e) {
			// TODO: handle exception
		}
        System.err.println("[THSTATE]: "+hiThread.getState().toString());
        
        /*
        try {
			hiThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        // ESPERO A QUE EL THREAD TERMINE PARA SEGUIR
        System.out.println(" SOY CLASE PADRE Y ESTOY ESPERANDO A MI HIJO QUE ME ESCRIBA");
        //
    }
}
