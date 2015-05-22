package distribuido.mapper;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class MapperServer {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Debe ingresar 2 parametros: Carpeta de trabajo y Video a convertir");
		} else {
			File carpeta = new File(args[0]);
			carpeta.mkdirs();
			if (!carpeta.isDirectory()) {
				System.out.println("Directorio invalido");
			} else {
				File video = new File(args[1]);
				if (!video.isFile()) {
					System.out.println("Direccion de video invalida");
				} else {
					System.setProperty("Java.rmi.server.hostname", "localhost");
					Registry registro;
					try {
						registro = LocateRegistry.createRegistry(9000);
					} catch (RemoteException e) {
						registro = LocateRegistry.getRegistry(9000);
					}
					Mapper map = new Mapper(args[0]);
					map.setVideoLocal(video);
					IMapper imap = (IMapper) UnicastRemoteObject.exportObject(map, 9000);
					registro.rebind("Mp01", imap);
					System.out.println("Servidor iniciado");
					mostrarMenu(imap);
				}
			}
		}
	}

	private static void mostrarMenu(IMapper imap) throws RemoteException {
		Scanner leer = new Scanner(System.in);
		int opcion;
		do {
			System.out.println("-----------------------------------------");
			System.out.println("Ingrese 1 para iniciar el trabajo");
			System.out.println("Ingrese 0 para salir");
			System.out.print("Opcion: ");
			opcion = leer.nextInt();
			leer.nextLine();
			switch (opcion) {
			case 1:
				imap.iniciarTrabajo();
				break;
			}
		} while (opcion != 0);
		leer.close();
	}
}
