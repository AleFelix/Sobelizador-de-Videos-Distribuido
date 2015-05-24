package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IContainer;

public class DecodificadorDeVideo extends MediaListenerAdapter {

	public static final String NOMBRE_CARPETA = "Frames";
	public static final String PREFIJO = "Frame";
	private long mLastPtsWrite = Global.NO_PTS;
	private int mVideoStreamIndex = -1;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss.SSS");
	private String directorio;
	private File framesDir;
	private IContainer contenedor;
	private boolean cambiarFrameRate = false;
	private double frameRate = 30.0;
	public double segundosEntreFrames = 1 / frameRate;
	public long microSegundosEntreFrames = (long) (Global.DEFAULT_PTS_PER_SECOND * segundosEntreFrames);
	
	public static void main(String[] args) {
		DecodificadorDeVideo ddv = new DecodificadorDeVideo(false);
		ddv.setFramerate(24);
		boolean r = ddv.decodificarVideo("/home/ale/TestVideos/Worker1/Video/VID11");
		System.out.println(r);
	}
	
	/**
	 * Crea un nuevo decodificador de video
	 * @param cambiarFramerate false si el framerate sera seteado manualmente o true si se detecta automaticamente
	 */
	public DecodificadorDeVideo(boolean cambiarFramerate) {
		this.cambiarFrameRate = cambiarFramerate;
	}
	
	/**
	 * Establece el framerate
	 * @param frameRate El framerate de decodificacion del video
	 */
	public void setFramerate(double frameRate) {
		this.frameRate = frameRate;
	}

	/**
	 * Transforma un video en un conjunto de imagenes que representan los fotogramas, la cantidad depende de la
	 * duracion del video y del framerate establecido
	 * @param filename URL del video a decodificar
	 * @return true si pudo decodificar el video, sino false
	 */
	public boolean decodificarVideo(String filename) {
		IMediaReader reader = ToolFactory.makeReader(filename);
		System.out.println(filename);
		contenedor = reader.getContainer();
		String nombreArchivo = new File(filename).getName();
		File dirFile = new File(filename).getParentFile();
		if (!dirFile.isDirectory()) {
			return false;
		}
		framesDir = new File(dirFile.getAbsolutePath() + "/" + nombreArchivo + "-" + NOMBRE_CARPETA);
		if (!framesDir.exists()) {
			if (!framesDir.mkdir()) {
				return false;
			}
		}
		directorio = dirFile.getAbsolutePath() + "/" + nombreArchivo + "-" + NOMBRE_CARPETA;
		reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		reader.addListener(this);
		System.out.println("DEBUG: A LEER");
		while (reader.readPacket() == null) {
		}
		System.out.println("DEBUG: FIN DE LECTURA");
		return true;
	}

	/**
	 * Borra las imagenes almacenadas de los fotogramas
	 */
	public void borrarFrames() {
		File[] frames = framesDir.listFiles();
		for (File frame : frames) {
			frame.delete();
		}
	}

	/**
	 * Borra el directorio donde se almacenan las imagenes de los fotogramas
	 */
	public void borrarDirFrames() {
		framesDir.delete();
	}

	/**
	 * Devuelve la URL del directorio donde se almacenan las imagenes de los fotogramas
	 * @return Carpeta de los fotogramas
	 */
	public String getDirFrames() {
		return directorio;
	}

	/**
	 * Devuelve el framerate establecido
	 * @return El framerate
	 */
	public double getFrameRate() {
		return frameRate;
	}

	/**
	 * Implementado internamente para decodificar el video
	 * @param event El evento recibido
	 */
	public void onVideoPicture(IVideoPictureEvent event) {
		try {
			if (event.getStreamIndex() != mVideoStreamIndex) {
				if (-1 == mVideoStreamIndex) {
					mVideoStreamIndex = event.getStreamIndex();
					//System.out.println("DEBUG: PRIMER FRAME");
					if (cambiarFrameRate) {
						frameRate = contenedor.getStream(mVideoStreamIndex).getFrameRate().getDouble();
						segundosEntreFrames = 1 / frameRate;
						microSegundosEntreFrames = (long) (Global.DEFAULT_PTS_PER_SECOND * segundosEntreFrames);
					}
				} else {
					//System.out.println("DEBUG: OTRO STREAM");
					return;
				}
			}
			if (mLastPtsWrite == Global.NO_PTS)
				//System.out.println("DEBUG: Seteo last write en: " + event.getTimeStamp() + " - " + microSegundosEntreFrames);
				mLastPtsWrite = event.getTimeStamp() - microSegundosEntreFrames;
				//System.out.println("DEBUG: last write = " + mLastPtsWrite);
			if (event.getTimeStamp() - mLastPtsWrite >= microSegundosEntreFrames) {
				File file = new File(directorio + "/" + PREFIJO + sdf.format(new Date()) + ".png");
				// System.out.println(directorio + "\\" + PREFIJO +
				// sdf.format(new Date()) + ".png");
				ImageIO.write(event.getImage(), "png", file);
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
				System.out.printf("A los %6.3f segundos, escribi: %s\n", seconds, file);
				mLastPtsWrite += microSegundosEntreFrames;
			} else {
				//System.out.println("DEBUG: NO GUARDO ESTE FRAME");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
