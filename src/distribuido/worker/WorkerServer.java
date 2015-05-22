package distribuido.worker;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class WorkerServer {

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Debe ingresar 4 parametros: ip, puerto, nombreWorker y Directorio de trabajo");
		} else {
			File directorio = new File(args[3]);
			directorio.mkdirs();
			if (!directorio.isDirectory()) {
				System.out.println("No ha proporcionado un directorio valido");
			} else {
				String ip = args[0];
				int puerto = Integer.valueOf(args[1]);
				String nombreWorker = args[2];
				System.setProperty("Java.rmi.server.hostname", ip);
				Registry registro;
				try {
					registro = LocateRegistry.createRegistry(puerto);
				} catch (RemoteException e) {
					registro = LocateRegistry.getRegistry(puerto);
				}
				Worker work = new Worker(ip, puerto, nombreWorker, directorio.getAbsolutePath());
				IWorker iwork = (IWorker) UnicastRemoteObject.exportObject(work, puerto);
				registro.rebind(nombreWorker, iwork);
				iwork.conectarConMapper();
			}
		}
	}

}
