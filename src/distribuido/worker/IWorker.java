package distribuido.worker;

import java.rmi.Remote;
import java.rmi.RemoteException;

import distribuido.archivo.IArchivo;

public interface IWorker extends Remote {

	public void cargarVideo(IArchivo video) throws RemoteException;
	
	public boolean estaDisponible() throws RemoteException;
	
	public boolean conectarConMapper() throws RemoteException;
	
}
