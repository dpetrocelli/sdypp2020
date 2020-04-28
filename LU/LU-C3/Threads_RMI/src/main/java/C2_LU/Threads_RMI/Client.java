package C2_LU.Threads_RMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Registry clientRMI = LocateRegistry.getRegistry("127.0.0.1", 9000);
			
			String[] services = clientRMI.list();
			
			for (String service : services) {
				System.out.println("Servicio: " + service);
			}
			/*
			 * CODIGO CODIG CODIG
			 */
			RemoteInt ri = (RemoteInt) clientRMI.lookup(services[0]); // clientRMI.lookup ( " nombre " );
			Person p = new Person ("david", "Petrocelli", 30);
			
			
			System.err.println( " DATOS dE RMI : "+ri.getName(p));
			/*
			 * CODIG OCIDOG 
			 */
			
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
