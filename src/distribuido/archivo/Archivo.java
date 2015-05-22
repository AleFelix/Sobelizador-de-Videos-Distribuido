package distribuido.archivo;


public class Archivo implements IArchivo {
	
	private static final long serialVersionUID = 1L;
	
	private int indice;
	private byte[] bytesArchivo;
	private int framerate;
	

	public Archivo(int i, byte[] b, int f) {
		indice = i;
		bytesArchivo = b;
		framerate = f;
	}

	@Override
	public int getIndice() {
		return indice;
	}

	@Override
	public byte[] getBytes() {
		return bytesArchivo;
	}

	@Override
	public int compareTo(IArchivo ia) {
		return this.indice - ia.getIndice();
	}

	@Override
	public int getFrameRate() {
		return framerate;
	}



}
