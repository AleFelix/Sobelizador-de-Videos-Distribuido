package distribuido.mapper;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import distribuido.archivo.IArchivo;

public interface IMapper extends Remote {

	public void devolverFrames(ArrayList<IArchivo> framesSobelizados) throws RemoteException;
	
	public boolean setVideo(String nombreVideo, IArchivo video) throws RemoteException;
	
	public boolean iniciarTrabajo() throws RemoteException;
	
	public boolean addWoker(String ip, int puerto, String nombre) throws RemoteException;
	
	public boolean removeWorker(String ip, int puerto, String nombre) throws RemoteException;
	
}
