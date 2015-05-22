package distribuido.worker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import tools.ConvertidorDeImagen;
import tools.DecodificadorDeVideo;
import tools.Sobelizador;
import distribuido.archivo.Archivo;
import distribuido.archivo.IArchivo;

public class WorkHandler implements Runnable {

	private Worker worker;
	private IArchivo video;

	public WorkHandler(Worker w, IArchivo v) {
		worker = w;
		video = v;
	}

	@Override
	public void run() {
		File carpetaVideoFile = worker.getCarpetaVideo();
		File videoFile = new File(carpetaVideoFile.getAbsolutePath() + "/" + Worker.NOMBRE_VIDEO + video.getIndice());
		try {
			FileOutputStream fileOuputStream = new FileOutputStream(videoFile.getAbsolutePath());
			fileOuputStream.write(video.getBytes());
			fileOuputStream.close();
			DecodificadorDeVideo ddv = new DecodificadorDeVideo(false);
			ddv.setFramerate(video.getFrameRate());
			boolean decodifico = ddv.decodificarVideo(videoFile.getAbsolutePath());
			if (!decodifico) {
				System.out.println("No se pudo decodificar el video");
			} else {
				File directorioFrames = new File(ddv.getDirFrames());
				File[] frames = directorioFrames.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".png");
					}
				});
				Arrays.sort(frames);
				ConvertidorDeImagen cdi = null;
				Sobelizador s;
				double[][] pixeles;
				for (int i = 0; i < frames.length; i++) {
					cdi = new ConvertidorDeImagen();
					s = new Sobelizador();
					pixeles = cdi.convertirImagenAPixeles(frames[i].getAbsolutePath());
					pixeles = s.sobelizar(pixeles);
					cdi.convertirPixelesAImagen(pixeles);
				}
				File directorioFramesSobelizados = new File(cdi.getDirSob());
				File[] framesSobelizados = directorioFramesSobelizados.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".png");
					}
				});
				Arrays.sort(framesSobelizados);
				ArrayList<IArchivo> archivosFramesSobelizados = new ArrayList<IArchivo>();
				IArchivo archivoFrameSobelizado;
				Path pathImagen;
				byte[] bytesImagen;
				for (int i = 0; i < framesSobelizados.length; i++) {
					pathImagen = Paths.get(framesSobelizados[i].getAbsolutePath());
					bytesImagen = Files.readAllBytes(pathImagen);
					archivoFrameSobelizado = new Archivo(i + 100 * video.getIndice(), bytesImagen, video.getFrameRate());
					archivosFramesSobelizados.add(archivoFrameSobelizado);
				}
//				ddv.borrarFrames();
//				cdi.borrarDirByN();
//				cdi.borrarFramesSob();
//				cdi.borrarDirSob();
//				ddv.borrarDirFrames();
				worker.enviarTrabajo(archivosFramesSobelizados);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
