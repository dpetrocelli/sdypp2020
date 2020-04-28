package C2_LU.Threads_RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		try {
			// [STEP 1] - Create RMI Server {while true}
			Registry serverRMI = LocateRegistry.createRegistry(9000);
			
			// [STEP 2] - Instantiate RemoteInterface implementation class
			ServerImplementer si = new ServerImplementer();
			ServerImplementer si2 = new ServerImplementer();
			
			// [STEP 3] - Export object as a service
			RemoteInt serviceA = (RemoteInt) UnicastRemoteObject.exportObject(si, 8000);
			RemoteInt serviceB = (RemoteInt) UnicastRemoteObject.exportObject(si2, 8001);
			
			// [STEP 4] - vinculación "bind" de nombre de servicio a objeto
			serverRMI.rebind("Person-Services", serviceA);
			serverRMI.rebind("Array-Services", serviceB);
			
			
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
