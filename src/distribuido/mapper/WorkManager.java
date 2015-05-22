package distribuido.mapper;

import java.rmi.RemoteException;

public class WorkManager implements Runnable {
	
	private Mapper mapper;
	private int indice;

	public WorkManager(Mapper map) {
		this.mapper = map;
		indice = 0;
	}

	@Override
	public void run() {
		while (mapper.getListaPedazosVideo().size() > 0) {
			if (indice > mapper.getWorkers().size()) {
				indice = 0;
			}
			try {
				if (mapper.getWorkers().get(indice).estaDisponible()) {
					mapper.getWorkers().get(indice).cargarVideo(mapper.getListaPedazosVideo().get(0));
					mapper.getListaPedazosVideo().remove(0);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			indice++;
		}
	}

}
