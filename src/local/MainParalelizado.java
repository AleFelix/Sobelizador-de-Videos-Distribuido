package local;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import tools.CodificadorDeVideo;
import tools.ConvertidorDeImagen;
import tools.DecodificadorDeVideo;
import tools.Parallelizer;
import tools.Sobelizador;

public class MainParalelizado {

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
				v.setMensaje("Video decodificado, separando los fotogramas...");
				String directorio = ddv.getDirFrames();
				File[] frames = new File(directorio).listFiles(new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".png");
				    }});
				ConvertidorDeImagen cdi = new ConvertidorDeImagen();
				Sobelizador s = new Sobelizador();
				List<double[][]> listaDePixeles = new ArrayList<double[][]>();
				List<String> listaPathsImagenes = new ArrayList<String>();
				for (int i = 0; i < 3; i++) {
					if (frames[i].isFile()) {
						listaPathsImagenes.add(frames[i].getAbsolutePath());
						double[][] pixeles = cdi.convertirImagenAPixeles(frames[i].getAbsolutePath());
						if (pixeles == null) {
							v.setMensaje("Hubo un error al leer una imagen, continua la separación...");
						} else {
							listaDePixeles.add(pixeles);
						}
					}
				}
				v.setMensaje("Video separado en frames, sobelizando los fotogramas...");
				Parallelizer<Sobelizador> p = new Parallelizer<Sobelizador>();
				Object[][] pixelesASobelizar = new Object[listaDePixeles.size()][1];
				for (int i=0; i<listaDePixeles.size(); i++) {
					pixelesASobelizar[i][0] = listaDePixeles.get(i);
				}
				List<Object> listaDePixelesSobelizados = p.paraTasks(Sobelizador.class, s, "sobelizar", pixelesASobelizar, listaDePixeles.size());
				v.setMensaje("Finalizada la sobelización, comienza el guardado de las imagenes sobelizadas...");
				for (int i=0; i<listaDePixelesSobelizados.size(); i++) {
					double[][] pixelesSobelizados = (double[][]) listaDePixelesSobelizados.get(i);
					if (!cdi.convertirPixelesAImagen(pixelesSobelizados,listaPathsImagenes.get(i))) {
						v.setMensaje("Hubo un error al convertir una imagen, continua el guardado...");
					}
				}
//				ddv.borrarFrames();
//				cdi.borrarDirByN();
				v.setMensaje("Finalizado el guardado, comienza la codificacion del video...");
				CodificadorDeVideo cdv = new CodificadorDeVideo();
				if (!cdv.codificarVideo(cdi.getDirSob(),selectedFile.getParent(),selectedFile.getName(),ddv.getFrameRate())) {
					do {} while(!v.finalizarVentana());
					JOptionPane.showMessageDialog(null, "Hubo un fallo en la codificaci�n del video");
				} else {
//					cdi.borrarFramesSob();
//					cdi.borrarDirSob();
//					ddv.borrarDirFrames();
					do {} while(!v.finalizarVentana());
					JOptionPane.showMessageDialog(null, "Proceso finalizado exitosamente");
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Debe seleccionar el video a sobelizar");
		}
	}

}
