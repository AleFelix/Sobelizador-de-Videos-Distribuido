package local;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Ventana implements Runnable {
	
	private JOptionPane panel;
	private JDialog dialogo;
	private String titulo;

	public Ventana(String titulo, String mensaje) {
		this.titulo = titulo;
		panel = new JOptionPane("<html><body><p style='width: 200px;'>" + mensaje + "</p></body></html>");
		panel.setOptions(new Object[]{});
	}

	@Override
	public void run() {
		dialogo =  panel.createDialog(null,titulo);
		dialogo.setVisible(true);
	}
	
	public void setMensaje(String mensaje) {
		panel.setMessage("<html><body><p style='width: 200px;'>" + mensaje + "</p></body></html>");
		panel.setOptions(new Object[]{});
	}
	
	public void setMensajeDeCierre(String mensaje) {
		panel.setMessage("<html><body><p style='width: 200px;'>" + mensaje + "</p></body></html>");
		panel.setOptions(new Object[]{"OK"});
	}
	
	public boolean finalizarVentana() {
		if (dialogo != null) {
			dialogo.dispose();
			return true;
		} else {
			return false;
		}
	}

}
