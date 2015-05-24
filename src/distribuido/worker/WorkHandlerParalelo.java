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
import java.util.List;

import tools.BorradorDeCarpetas;
import tools.ConvertidorDeImagen;
import tools.DecodificadorDeVideo;
import tools.Parallelizer;
import tools.Sobelizador;
import distribuido.archivo.Archivo;
import distribuido.archivo.IArchivo;

public class WorkHandlerParalelo implements Runnable {

	private Worker worker;
	private IArchivo video;

	public WorkHandlerParalelo(Worker w, IArchivo v) {
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
				Sobelizador s = new Sobelizador();
				List<double[][]> listaDePixeles = new ArrayList<double[][]>();
				List<String> listaPathsImagenes = new ArrayList<String>();
				int indiceFrame = 0;
				int subIndice = 0;
				int indiceLista = 0;
				int topSubindice = 10;
				double[][] pixeles, pixelesSobelizados;
				Object[][] pixelesASobelizar;
				Parallelizer<Sobelizador> p;
				Object[] listaDePixelesSobelizados;
				while (indiceFrame < frames.length) {
					while (subIndice < topSubindice && indiceFrame < frames.length) {
						if (frames[indiceFrame].isFile()) {
							listaPathsImagenes.add(frames[indiceFrame].getAbsolutePath());
							System.out.println("INDICEFRAME: " + indiceFrame);
							cdi = new ConvertidorDeImagen();
							pixeles = cdi.convertirImagenAPixeles(frames[indiceFrame].getAbsolutePath());
							listaDePixeles.add(pixeles);
						}
						indiceFrame++;
						subIndice++;
					}
					if (listaDePixeles.size() > 0) {
						p = new Parallelizer<Sobelizador>();
						pixelesASobelizar = new Object[listaDePixeles.size()][1];
						System.out.println("Tamaño de la lista de pixeles: " + listaDePixeles.size());
						for (int i = 0; i < listaDePixeles.size(); i++) {
							pixelesASobelizar[i][0] = listaDePixeles.get(i);
							System.out.println("Añadido uno al paralelizador");
						}
						listaDePixelesSobelizados = p.paraTasks(Sobelizador.class, s, "sobelizar", pixelesASobelizar, listaDePixeles.size());
						System.out.println("Sobelizacion Hecha");
						for (int i = 0; i < listaDePixelesSobelizados.length; i++) {
							pixelesSobelizados = (double[][]) listaDePixelesSobelizados[i];
							cdi.convertirPixelesAImagen(pixelesSobelizados, listaPathsImagenes.get(indiceLista));
							indiceLista++;
						}
						listaDePixeles = new ArrayList<double[][]>();
					}
					subIndice = 0;
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
				BorradorDeCarpetas.borrarContenido(carpetaVideoFile);
				System.out.println("Todo borrado");
				worker.enviarTrabajo(archivosFramesSobelizados);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
