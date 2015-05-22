package distribuido.archivo;

import java.io.Serializable;

public interface IArchivo extends Serializable, Comparable<IArchivo> {

	public int getIndice();
	
	public byte[] getBytes();
	
	public int compareTo(IArchivo ia);
	
	public int getFrameRate();
	
}
