package distribuido.worker;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import distribuido.archivo.IArchivo;
import distribuido.mapper.IMapper;

public class Worker implements IWorker {

	public static final String NOMBRE_CARPETA_VIDEO = "Video";
	public static final String NOMBRE_VIDEO = "VID";

	private boolean disponible;
	private File carpetaVideoFile;
	private IMapper imap;
	private String ip;
	private int puerto;
	private String nombre;

	public Worker(String ip, int puerto, String nombre, String directorio) {
		this.ip = ip;
		this.puerto = puerto;
		this.nombre = nombre;
		carpetaVideoFile = new File(directorio + "/" + NOMBRE_CARPETA_VIDEO);
		carpetaVideoFile.mkdirs();
	}
	
	@Override
	public boolean conectarConMapper() {
		try {
			Registry registro = LocateRegistry.getRegistry("localhost", 9000);
			imap = (IMapper) registro.lookup("Mp01");
			imap.addWoker(ip, puerto, nombre);
			disponible = true;
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public File getCarpetaVideo() {
		return carpetaVideoFile;
	}

	@Override
	public synchronized void cargarVideo(IArchivo video) throws RemoteException {
		disponible = false;
		WorkHandlerParalelo wh = new WorkHandlerParalelo(this, video);
		new Thread(wh).start();
	}

	public void enviarTrabajo(ArrayList<IArchivo> framesSobelizados) {
		try {
			System.out.println("Enviando trabajo");
			imap.devolverFrames(framesSobelizados);
			disponible = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized boolean estaDisponible() throws RemoteException {
		return disponible;
	}
	
	public void terminar() {
		try {
			imap.removeWorker(ip, puerto, nombre);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
