package tools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ConvertidorDeImagen {

	public static final String NOMBRE_CARPETA_BYN = "ByN";
	public static final String NOMBRE_CARPETA_SOB = "SoB";
	private File archivoImagen;

	/**
	 * Convierte una imagen almacenada en el disco en una matriz de pixeles
	 * @param dirImagen URL de la imagen a convertir
	 * @return La matriz de pixeles
	 */
	public double[][] convertirImagenAPixeles(String dirImagen) {
		try {
			archivoImagen = new File(dirImagen);
			new File(archivoImagen.getParent() + "/" + NOMBRE_CARPETA_BYN).mkdir();
			new File(archivoImagen.getParent() + "/" + NOMBRE_CARPETA_SOB).mkdir();
			BufferedImage imagen = ImageIO.read(archivoImagen);
			//System.out.println("PATH: " + archivoImagen.getAbsolutePath());
			BufferedImage imagenByN = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  
			Graphics g = imagenByN.getGraphics();  
			g.drawImage(imagen, 0, 0, null);
			File fileImagenByN = new File(archivoImagen.getParent() + "/" + NOMBRE_CARPETA_BYN + "/" + archivoImagen.getName() + ".ByN." + obtenerExtension(archivoImagen.getName()));
			ImageIO.write(imagenByN, obtenerExtension(archivoImagen.getName()), fileImagenByN);
			Raster raster = imagenByN.getData();
			double[][] pixeles = new double[imagenByN.getWidth()][imagenByN.getHeight()];
			for (int j = 0; j < imagenByN.getWidth(); j++) {
			    for (int k = 0; k < imagenByN.getHeight(); k++) {
			        pixeles[j][k] = raster.getSample(j, k, 0);
			    }
			}
			fileImagenByN.delete();
			return pixeles;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Devuelve la URL del directorio de imagenes sobelizadas a partir de la URL pasada como parametro
	 * @param dirPadre La URL del directorio padre
	 * @return La URL del directorio de imagenes sobelizadas
	 */
	public static String getDirSob(String dirPadre) {
		File carpetaPadre = new File(dirPadre);
		if (!carpetaPadre.isDirectory()) {
			return null;
		} else {
			return carpetaPadre.getAbsolutePath() + "/" + NOMBRE_CARPETA_SOB;
		}
	}
	
	/**
	 * Devuelve el directorio donde se almacenan las imagenes sobelizadas
	 * @return URL del directorio de imagenes sobelizadas
	 */
	public String getDirSob() {
		return archivoImagen.getParent() + "/" + NOMBRE_CARPETA_SOB;
	}
	
	/**
	 * Borra las imagenes en el directorio de imagenes sobelizadas
	 */
	public void borrarFramesSob() {
		File[] imagenes = new File(archivoImagen.getParent() + "/" + NOMBRE_CARPETA_SOB).listFiles();
		for (File imagen : imagenes) {
			imagen.delete();
		}
	}
	
	/**
	 * Borra el directorio donde se almacenan las imagenes sobelizadas
	 * @return true si pudo borrarlo, sino false
	 */
	public boolean borrarDirSob() {
		return new File(archivoImagen.getParent() + "/" + NOMBRE_CARPETA_SOB).delete();
	}
	
	/**
	 * Borra el directorio donde se almacenan las imagenes en blanco y negro
	 * @return true si pudo borrarlo, sino false
	 */
	public boolean borrarDirByN() {
		return new File(archivoImagen.getParent() + "/" + NOMBRE_CARPETA_BYN).delete();
	}
	
	private String obtenerExtension(String nombreImagen) {
		String extension = "";
		int i = nombreImagen.lastIndexOf('.');
		if (i > 0) {
		    extension = nombreImagen.substring(i+1);
		}
		return extension;
	}
	
	/**
	 * Convierte una matriz de pixeles sobelizados en una imagen almacenada en el directorio de sobelizacion,
	 * genera el nombre a partir de una imagen cargada previamente
	 * @param pixelesSobelizados Matriz de pixeles que fueron previamente sobelizados
	 * @return true si pudo crear la imagen, sino false
	 */
	public boolean convertirPixelesAImagen(double[][] pixelesSobelizados) {
		return this.convertirPixelesAImagen(pixelesSobelizados, archivoImagen.getAbsolutePath());
	}

	/**
	 * Convierte una matriz de pixeles sobelizados en una imagen almacenada en el directorio de sobelizacion
	 * @param pixelesSobelizados Matriz de pixeles que fueron previamente sobelizados
	 * @param dirImagen URL de la imagen original sin sobelizar
	 * @return true si pudo crear la imagen, sino false
	 */
	public boolean convertirPixelesAImagen(double[][] pixelesSobelizados, String dirImagen) {
		File nuevoArchivoImagen = new File(dirImagen);
		int ancho = pixelesSobelizados.length;
		int alto = pixelesSobelizados[0].length;
		int magnitudEnRGB;
		BufferedImage imagenSobelizada = new BufferedImage(ancho, alto, BufferedImage.TYPE_BYTE_GRAY);
		for(int x = 0; x < ancho; x++) {
		    for(int y = 0; y < alto; y++) {
		    	magnitudEnRGB = (int) pixelesSobelizados[x][y];
		    	magnitudEnRGB = new Color(magnitudEnRGB,magnitudEnRGB,magnitudEnRGB).getRGB();
		        imagenSobelizada.setRGB(x, y, magnitudEnRGB);
		    }
		}
		try {
			System.out.println("Voy a escribir: " + nuevoArchivoImagen.getParent() + "/" + NOMBRE_CARPETA_SOB + "/" + nuevoArchivoImagen.getName() + ".SoB.png");
			boolean trabajo = ImageIO.write(imagenSobelizada, "PNG", new File(nuevoArchivoImagen.getParent() + "/" + NOMBRE_CARPETA_SOB + "/" + nuevoArchivoImagen.getName() + ".SoB.png"));
			if (!trabajo) {
				System.out.println("No hizo nada");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		//System.out.println("Terminado");
		return true;
	}
	
}
