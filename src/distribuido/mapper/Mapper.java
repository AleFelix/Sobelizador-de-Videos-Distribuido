package distribuido.mapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;

import tools.CodificadorDeVideo;
import tools.DivisorDeVideoMejorado;
import distribuido.archivo.Archivo;
import distribuido.archivo.IArchivo;
import distribuido.worker.IWorker;

public class Mapper implements IMapper {

	public static final String NOMBRE_CARPETA_VIDEO = "Video";
	public static final String NOMBRE_CARPETA_FRAMES = "Frames";
	public static final String EXTENSION_FRAMES = ".png";
	public static final String NOMBRE_FRAME = "Frame";
	public static final String NOMBRE_CARPETA_FINAL = "VideoFinal";
	public static final String PREFIJO_VIDEO_FINAL = "SOB_";
	public static final String PADDING_CEROS = "%06d";

	private File videoFile;
	private File carpetaVideoFile;
	private File carpetaFrames;
	private File carpetaVideoFinal;
	private ArrayList<IArchivo> listaPedazosVideo = new ArrayList<IArchivo>();
	private ArrayList<IWorker> workers = new ArrayList<IWorker>();
	private int cantidadDePedazos;
	private int cantidadDePedazosProcesados;
	private String nombreVideo;
	private int frameRate;

	public Mapper(String directorio) {
		cantidadDePedazosProcesados = 0;
		carpetaVideoFile = new File(directorio + "/" + NOMBRE_CARPETA_VIDEO);
		carpetaVideoFile.mkdirs();
		carpetaFrames = new File(directorio + "/" + NOMBRE_CARPETA_FRAMES);
		carpetaFrames.mkdirs();
		carpetaVideoFinal = new File(directorio + "/" + NOMBRE_CARPETA_FINAL);
		carpetaVideoFinal.mkdirs();
	}

	@Override
	public void devolverFrames(ArrayList<IArchivo> framesSobelizados) throws RemoteException {
		for (int i = 0; i < framesSobelizados.size(); i++) {
			try {
				FileOutputStream fileOuputStream = new FileOutputStream(carpetaFrames.getAbsolutePath() + "/" + NOMBRE_FRAME + String.format(PADDING_CEROS, framesSobelizados.get(i).getIndice()) + EXTENSION_FRAMES);
				fileOuputStream.write(framesSobelizados.get(i).getBytes());
				fileOuputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cantidadDePedazosProcesados++;
		if (cantidadDePedazosProcesados == cantidadDePedazos) {
			CodificadorDeVideo cdv = new CodificadorDeVideo();
			cdv.codificarVideo(carpetaFrames.getAbsolutePath(), carpetaVideoFinal.getAbsolutePath(), PREFIJO_VIDEO_FINAL + nombreVideo, frameRate);
		}
	}
	
	public boolean setVideoLocal(File videoFile) {
		Path pathVideo = Paths.get(videoFile.getAbsolutePath());
		byte[] bytesVideo;
		try {
			bytesVideo = Files.readAllBytes(pathVideo);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		IArchivo videoArchivo = new Archivo(0, bytesVideo, 0);
		try {
			setVideo(videoFile.getName(),videoArchivo);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean setVideo(String nombreVideo, IArchivo video) throws RemoteException {
		this.nombreVideo = nombreVideo;
		String pathVideoFile = carpetaVideoFile.getAbsolutePath() + "/" + nombreVideo;
		videoFile = new File(pathVideoFile);
		try {
			FileOutputStream fileOuputStream = new FileOutputStream(pathVideoFile);
			fileOuputStream.write(video.getBytes());
			fileOuputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean dividirVideo() {
		DivisorDeVideoMejorado ddv = new DivisorDeVideoMejorado();
		ddv.dividirVideo(carpetaVideoFile.getAbsolutePath(), videoFile.getName());
		frameRate = ddv.getFramerate();
		File[] archivosPedazosVideo = new File(ddv.getDirPiezas()).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(obtenerExtension(videoFile.getName()));
			}
		});
		cantidadDePedazos = archivosPedazosVideo.length;
		Arrays.sort(archivosPedazosVideo);
		Path pathPedazoVideo;
		byte[] bytesPedazoVideo;
		IArchivo archivoPedazoVideo;
		for (int i = 0; i < cantidadDePedazos; i++) {
			pathPedazoVideo = Paths.get(archivosPedazosVideo[i].getAbsolutePath());
			try {
				bytesPedazoVideo = Files.readAllBytes(pathPedazoVideo);
				archivoPedazoVideo = new Archivo(i, bytesPedazoVideo, frameRate);
				listaPedazosVideo.add(archivoPedazoVideo);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private String obtenerExtension(String nombreArchivo) {
		String extension = "";
		int i = nombreArchivo.lastIndexOf('.');
		if (i > 0) {
			extension = nombreArchivo.substring(i + 1);
		}
		return extension;
	}

	@Override
	public boolean iniciarTrabajo() throws RemoteException {
		if (workers.size() < 1) {
			return false;
		}
		if (!dividirVideo()) {
			return false;
		}
		WorkManager wm = new WorkManager(this);
		new Thread(wm).start();
		return true;
	}

	@Override
	public boolean addWoker(String ip, int puerto, String nombre) throws RemoteException {
		Registry registro = LocateRegistry.getRegistry(ip, puerto);
		try {
			IWorker iw = (IWorker) registro.lookup(nombre);
			workers.add(iw);
		} catch (NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("Añadido el Worker [IP: " + ip + " PUERTO: " + puerto + " NOMBRE: " + nombre + "]");
		return true;
	}

	@Override
	public boolean removeWorker(String ip, int puerto, String nombre) throws RemoteException {
		Registry registro = LocateRegistry.getRegistry(ip, puerto);
		try {
			IWorker iw = (IWorker) registro.lookup(nombre);
			workers.remove(iw);
		} catch (NotBoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<IWorker> getWorkers() {
		return workers;
	}

	public ArrayList<IArchivo> getListaPedazosVideo() {
		return listaPedazosVideo;
	}

}
