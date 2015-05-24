package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BorradorDeCarpetas {

	public static void borrar(File file) throws IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				borrar(child);
		}
		if (!file.delete())
			throw new FileNotFoundException("Fallo al borrar: " + file);
	}

	public static void borrarContenido(File file) throws IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				borrar(child);
		} else {
			throw new FileNotFoundException("No es una carpeta");
		}
	}

}
