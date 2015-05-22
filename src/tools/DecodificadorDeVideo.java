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
	private static long mLastPtsWrite = Global.NO_PTS;
	private int mVideoStreamIndex = -1;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss.SSS");
	private String directorio;
	private File framesDir;
	private IContainer contenedor;
	private boolean cambiarFrameRate = false;
	private double frameRate = 30.0;
	public double segundosEntreFrames = 1 / frameRate;
	public long microSegundosEntreFrames = (long) (Global.DEFAULT_PTS_PER_SECOND * segundosEntreFrames);
	
	public DecodificadorDeVideo(boolean cambiarFramerate) {
		this.cambiarFrameRate = cambiarFramerate;
	}
	
	public void setFramerate(double frameRate) {
		this.frameRate = frameRate;
	}

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
		return true;
	}

	public void borrarFrames() {
		File[] frames = framesDir.listFiles();
		for (File frame : frames) {
			frame.delete();
		}
	}

	public void borrarDirFrames() {
		framesDir.delete();
	}

	public String getDirFrames() {
		return directorio;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public void onVideoPicture(IVideoPictureEvent event) {
		try {
			if (event.getStreamIndex() != mVideoStreamIndex) {
				if (-1 == mVideoStreamIndex) {
					mVideoStreamIndex = event.getStreamIndex();
					if (cambiarFrameRate) {
						frameRate = contenedor.getStream(mVideoStreamIndex).getFrameRate().getDouble();
						segundosEntreFrames = 1 / frameRate;
						microSegundosEntreFrames = (long) (Global.DEFAULT_PTS_PER_SECOND * segundosEntreFrames);
					}
				} else
					return;
			}
			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = event.getTimeStamp() - microSegundosEntreFrames;
			if (event.getTimeStamp() - mLastPtsWrite >= microSegundosEntreFrames) {
				File file = new File(directorio + "/" + PREFIJO + sdf.format(new Date()) + ".png");
				// System.out.println(directorio + "\\" + PREFIJO +
				// sdf.format(new Date()) + ".png");
				ImageIO.write(event.getImage(), "png", file);
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
				System.out.printf("A los %6.3f segundos, escribi: %s\n", seconds, file);
				mLastPtsWrite += microSegundosEntreFrames;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
