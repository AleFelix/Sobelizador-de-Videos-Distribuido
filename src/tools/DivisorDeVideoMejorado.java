package tools;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;

public class DivisorDeVideoMejorado extends MediaToolAdapter {

	public static final long MICROSEG_EN_UN_MILISEG = 1000;
	public static final long DURACION_MILISEG_PIEZA = 2000;
	public static final String PADDING_CEROS = "%06d";
	public static final String NOMBRE_DIR_PIEZAS = "Piezas";
	public static final String PREFIJO = "PZ";
	private long tiempoPiezaActual = 0;
	private IMediaWriter writer;
	private IMediaReader reader;
	private String dirCompletaArchivo;
	private long numPiezaActual = 1;
	private long cantPiezas = 0;
	private String nombreArchivo;
	private String dirPiezas;
	private long timeUltimoCorte = 0;
	private long timeEvento;
	private boolean frameInicial = true;
	private int frameRate;
	private long nanosegundosPrevios = 0;

	public static void main(String[] args) {
		DivisorDeVideoMejorado coddv = new DivisorDeVideoMejorado();
		coddv.dividirVideo("/home/ale/TestVideos", "Cats.mp4");
	}

	/**
	 * Devuelve el framerate del video
	 * 
	 * @return El framerate del video
	 */
	public int getFramerate() {
		return frameRate;
	}

	/**
	 * Devuelve la cantidad de piezas en las que se dividio el video
	 * 
	 * @return La cantidad de partes del video
	 */
	public long getCantPiezas() {
		return cantPiezas;
	}

	/**
	 * Devuelve el directorio que contiene las piezas del video
	 * 
	 * @return La url de la carpeta con las piezas del video
	 */
	public String getDirPiezas() {
		return dirPiezas;
	}

	/**
	 * Usado internamente para obtener la duracion del video y la cantidad de
	 * piezas
	 */
	private void establecerCantidades() {
		IMediaReader r = ToolFactory.makeReader(dirCompletaArchivo);
		r.readPacket();
		long duracionEnMilisegundos = r.getContainer().getDuration() / MICROSEG_EN_UN_MILISEG;
		Double dfr = r.getContainer().getStream(0).getFrameRate().getDouble();
		frameRate = dfr.intValue();
		cantPiezas = duracionEnMilisegundos / DURACION_MILISEG_PIEZA;
		if (duracionEnMilisegundos % DURACION_MILISEG_PIEZA != 0) {
			cantPiezas += 1;
		}
		r.close();
	}

	/**
	 * Divide al video en distintas piezas mas pequeñas
	 * 
	 * @param dirArchivo
	 *            URL del directorio del video
	 * @param nombreArchivo
	 *            Nombre del video
	 */
	public void dividirVideo(String directorioArchivo, String nombreArchivo) {
		dirCompletaArchivo = directorioArchivo + "/" + nombreArchivo;
		this.nombreArchivo = nombreArchivo;
		dirPiezas = directorioArchivo + "/" + NOMBRE_DIR_PIEZAS;
		new File(dirPiezas).mkdir();
		System.out.println(dirCompletaArchivo);
		System.out.println(directorioArchivo);
		System.out.println(nombreArchivo);
		System.out.println(dirPiezas);
		System.out.println(dirPiezas + "/" + PREFIJO + "-" + String.format(PADDING_CEROS, numPiezaActual) + "-" + nombreArchivo);
		establecerCantidades();
		reader = ToolFactory.makeReader(dirCompletaArchivo);
		writer = ToolFactory.makeWriter(dirPiezas + "/" + PREFIJO + "-" + String.format(PADDING_CEROS, numPiezaActual) + "-" + nombreArchivo);
		reader.addListener(this);
		while (reader.readPacket() == null) {
		}
		writer.close();
	}

	/**
	 * Usado internamente para dividir el video
	 * 
	 * @param event
	 *            Evento recibido
	 */
	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		IVideoPicture imagen = event.getPicture();
		if (imagen != null) {
			if (frameInicial) {
				writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, imagen.getWidth(), imagen.getHeight());
				frameInicial = false;
			}
			timeEvento = event.getTimeStamp(TimeUnit.MILLISECONDS);
			if (event.getTimeStamp() - nanosegundosPrevios < 0) {
				imagen.setTimeStamp(0);
			} else {
				imagen.setTimeStamp(event.getTimeStamp() - nanosegundosPrevios);
			}
			writer.encodeVideo(0, imagen);
			writer.flush();
			tiempoPiezaActual = timeEvento - timeUltimoCorte;
			System.out.println(tiempoPiezaActual);
			if (tiempoPiezaActual >= DURACION_MILISEG_PIEZA) {
				timeUltimoCorte = timeEvento;
				nanosegundosPrevios = event.getTimeStamp();
				writer.close();
				numPiezaActual++;
				writer = ToolFactory.makeWriter(dirPiezas + "/" + PREFIJO + "-" + String.format(PADDING_CEROS, numPiezaActual) + "-" + nombreArchivo);
				frameInicial = true;
			}
		} else {
			System.out.println("NULO");
		}
	}

}