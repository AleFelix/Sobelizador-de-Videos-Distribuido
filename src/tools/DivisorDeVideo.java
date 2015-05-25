package tools;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class DivisorDeVideo extends MediaToolAdapter {

	public static final long MICROSEG_EN_UN_MILISEG = 1000;
	public static final long DURACION_MILISEG_PIEZA = 2000;
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
	private Long timeEvento;
	
	public static void main(String[] args) {
		DivisorDeVideo ddv = new DivisorDeVideo();
		ddv.dividirVideo("/home/ale/Videos-SOB/Videos-02", "Sample.mp4");
	}
	
	/**
	 * Devuelve la cantidad de piezas en las que se dividio el video
	 * @return La cantidad de partes del video
	 */
	public long getCantPiezas() {
		return cantPiezas;
	}
	
	/**
	 * Usado internamente para obtener la duracion del video y la cantidad de piezas
	 */
	private void establecerCantidades() {
		IMediaReader r = ToolFactory.makeReader(dirCompletaArchivo);
		r.readPacket();
		long duracionEnMilisegundos = r.getContainer().getDuration()/MICROSEG_EN_UN_MILISEG;
		cantPiezas = duracionEnMilisegundos / DURACION_MILISEG_PIEZA;
		if (duracionEnMilisegundos % DURACION_MILISEG_PIEZA != 0) {
			cantPiezas += 1;
		}
		r.close();
	}

	/**
	 * Divide al video en distintas piezas mas pequeñas
	 * @param dirArchivo URL del directorio del video
	 * @param nombreArchivo Nombre del video
	 */
	public void dividirVideo(String dirArchivo, String nombreArchivo) {
		dirCompletaArchivo = dirArchivo + "/" + nombreArchivo;
		this.nombreArchivo = nombreArchivo;
		dirPiezas = dirArchivo + "/" + NOMBRE_DIR_PIEZAS;
		new File(dirPiezas).mkdir();
		System.out.println(dirCompletaArchivo);
		System.out.println(dirArchivo);
		System.out.println(nombreArchivo);
		System.out.println(dirPiezas);
		System.out.println(dirPiezas + "/" + PREFIJO + "-" + numPiezaActual + "-" + nombreArchivo);
		establecerCantidades();
		reader = ToolFactory.makeReader(dirCompletaArchivo);
		writer = ToolFactory.makeWriter(dirPiezas + "/" + PREFIJO + "-" + numPiezaActual + "-" + nombreArchivo, reader);
//		writer.setForceInterleave(false);
		reader.addListener(this);
		this.addListener(writer);
		while (reader.readPacket() == null) {
		}
	}
	
	/**
	 * Usado internamente para dividir el video
	 * @param event Evento recibido
	 */
	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		super.onVideoPicture(event);
		timeEvento = event.getTimeStamp(TimeUnit.MILLISECONDS);
		tiempoPiezaActual = timeEvento - timeUltimoCorte;
		System.out.println(tiempoPiezaActual);
		if (tiempoPiezaActual >= DURACION_MILISEG_PIEZA) {
			timeUltimoCorte = timeEvento;
			this.removeListener(writer);
			writer.close();
			numPiezaActual++;
			writer = ToolFactory.makeWriter(dirPiezas + "/" + PREFIJO + "-" + numPiezaActual + "-" + nombreArchivo, reader);
//			writer.setForceInterleave(false);
			this.addListener(writer);
		}
	}
	
}
