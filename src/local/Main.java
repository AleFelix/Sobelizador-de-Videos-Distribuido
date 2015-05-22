package local;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import tools.CodificadorDeVideo;
import tools.ConvertidorDeImagen;
import tools.DecodificadorDeVideo;
import tools.Sobelizador;

public class Main {

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error del sistema");
		}
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int resultado = fileChooser.showOpenDialog(null);
		if (resultado == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			Ventana v = new Ventana("Sobelizador","Iniciando la decodificación del video...");
			new Thread(v).start();
			DecodificadorDeVideo ddv = new DecodificadorDeVideo(true);
			if (!ddv.decodificarVideo(selectedFile.getAbsolutePath())) {
				do {} while(!v.finalizarVentana());
				JOptionPane.showMessageDialog(null, "No se ha podido decodificar el video");
			} else {
				v.setMensaje("Video decodificado, sobelizando los fotogramas...");
				String directorio = ddv.getDirFrames();
				File[] frames = new File(directorio).listFiles(new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".png");
				    }});
				ConvertidorDeImagen cdi = new ConvertidorDeImagen();
				Sobelizador s = new Sobelizador();
				for (int i = 0; i < frames.length; i++) {
					if (frames[i].isFile()) {
						double[][] pixeles = cdi.convertirImagenAPixeles(frames[i].getAbsolutePath());
						if (pixeles == null) {
							v.setMensaje("Hubo un error al leer una imagen, continua la sobelizacion...");
						} else {
							double[][] pixelesSobelizados = s.sobelizar(pixeles);
							if (!cdi.convertirPixelesAImagen(pixelesSobelizados)) {
								v.setMensaje("Hubo un error al convertir una imagen, continua la sobelizacion...");
							}
						}
					}
				}
				ddv.borrarFrames();
				cdi.borrarDirByN();
				v.setMensaje("Finalizada la sobelizacion, comienza la codificacion del video...");
				CodificadorDeVideo cdv = new CodificadorDeVideo();
				if (!cdv.codificarVideo(cdi.getDirSob(),selectedFile.getParent(),selectedFile.getName(),ddv.getFrameRate())) {
					do {} while(!v.finalizarVentana());
					JOptionPane.showMessageDialog(null, "Hubo un fallo en la codificaci�n del video");
				} else {
					cdi.borrarFramesSob();
					cdi.borrarDirSob();
					ddv.borrarDirFrames();
					do {} while(!v.finalizarVentana());
					JOptionPane.showMessageDialog(null, "Proceso finalizado exitosamente");
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Debe seleccionar el video a sobelizar");
		}
	}

}
