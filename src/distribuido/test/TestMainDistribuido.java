package distribuido.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import tools.ConvertidorDeImagen;
import tools.DecodificadorDeVideo;
import tools.Sobelizador;

public class TestMainDistribuido {

	public static void main(String[] args) {
		File[] archivos = new File("/home/ale/Videos-SOB/Videos-02/Piezas").listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".mp4");
			}
		});
		Arrays.sort(archivos);
		for (int i=0; i<archivos.length; i++) {
			DecodificadorDeVideo ddv = new DecodificadorDeVideo(false);
			ConvertidorDeImagen cdi = null;
			ddv.decodificarVideo(archivos[i].getAbsolutePath());
			System.out.println("Video N°" + i + " decodificado.");
			File[] imagenes = new File(ddv.getDirFrames()).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".png");
				}
			});
			Arrays.sort(imagenes);
			for (int j=0; j<imagenes.length; j++) {
				cdi = new ConvertidorDeImagen();
				Sobelizador s = new Sobelizador();
				double[][] pixeles = cdi.convertirImagenAPixeles(imagenes[j].getAbsolutePath());
				double[][] pixelesSobelizados = s.sobelizar(pixeles);
				cdi.convertirPixelesAImagen(pixelesSobelizados);
			}
		}
	}

}
