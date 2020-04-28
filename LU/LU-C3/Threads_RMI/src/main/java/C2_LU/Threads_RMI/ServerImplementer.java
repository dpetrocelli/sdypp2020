package C2_LU.Threads_RMI;

import java.rmi.RemoteException;

public class ServerImplementer implements RemoteInt {

	public String getName(Person p) throws RemoteException {
		// TODO Auto-generated method stub
		return p.getName();
	}

	
	
	

}
