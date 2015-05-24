package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BorradorDeCarpetas {

	/**
	 * Metodo utilizado para borrar un archivo o carpeta con todos sus hijos
	 * @param file Archivo o carpeta que se desea borrar
	 * @throws IOException Si no se pudo borrar algun archivo
	 */
	public static void borrar(File file) throws IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				borrar(child);
		}
		if (!file.delete())
			throw new FileNotFoundException("Fallo al borrar: " + file);
	}

	/**
	 * Metodo utilizado para borrar el contenido de una carpeta
	 * @param file Carpeta de la cual se desea borrar el contenido
	 * @throws IOException Si el parametro file no es una carpeta
	 */
	public static void borrarContenido(File file) throws IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				borrar(child);
		} else {
			throw new FileNotFoundException("No es una carpeta");
		}
	}

}
