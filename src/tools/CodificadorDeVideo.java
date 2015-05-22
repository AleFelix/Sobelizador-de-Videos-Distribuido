package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class CodificadorDeVideo {

	public static final String SUFIJO = ".codificado.mp4";
	private double frameRate = 30;
	private double milisegundosEntreFrames = Math.ceil((1/frameRate)*1000);

	public static void main(String args[]) {
		CodificadorDeVideo cdv = new CodificadorDeVideo();
		String directorioFrames = "C:/Users/Ale/Desktop/out/Frames/SoB";
		String directorioSalida = "C:/Users/Ale/Desktop/out";
		String nombreVideo = "SampleVideo_1080x720_1mb.mp4";
		cdv.codificarVideo(directorioFrames, directorioSalida, nombreVideo, 30);
	}

	public boolean codificarVideo(String directorioFrames, String directorioSalida, String nombreVideo, double frameRate) {
		this.frameRate = frameRate;
		milisegundosEntreFrames = Math.ceil((1/frameRate)*1000);
		String dirArchivo = directorioSalida + "/" + nombreVideo + SUFIJO;
		File[] imagenes = new File(directorioFrames).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".png");
			}
		});
		Arrays.sort(imagenes);
		int ancho ,alto;
		try {
			ancho = ImageIO.read(imagenes[0]).getWidth();
			alto = ImageIO.read(imagenes[0]).getHeight();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		final IMediaWriter writer = ToolFactory.makeWriter(dirArchivo);
		writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, ancho, alto);
		BufferedImage imagen, imagenConvertida;
		long tiempo = 0;
		for (int index = 0; index < imagenes.length; index++) {
			try {
				imagen = ImageIO.read(imagenes[index]);
				System.out.println(imagenes[index].getAbsolutePath());
				imagenConvertida = convertirImagen(imagen, BufferedImage.TYPE_3BYTE_BGR);
				writer.encodeVideo(0, imagenConvertida, tiempo, TimeUnit.MILLISECONDS);
				tiempo += milisegundosEntreFrames;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		writer.close();
		return true;
	}

	private BufferedImage convertirImagen(BufferedImage imagenOriginal, int tipo) {
		BufferedImage imagenConvertida;
		if (imagenOriginal.getType() == tipo) {
			imagenConvertida = imagenOriginal;
		} else {
			imagenConvertida = new BufferedImage(imagenOriginal.getWidth(), imagenOriginal.getHeight(), tipo);
			imagenConvertida.getGraphics().drawImage(imagenOriginal, 0, 0, null);
		}
		return imagenConvertida;
	}

}
