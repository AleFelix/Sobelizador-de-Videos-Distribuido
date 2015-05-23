package tools;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Parallelizer<O> {

	@SuppressWarnings("unchecked")
	public List<Object> paraCores(Class<O> classType, O object, String methodName, Object[] parameters) {
		ParallelTask<O> parallelTask = new ParallelTask<O>(classType, object, methodName, parameters);
		ForkJoinPool pool = new ForkJoinPool();
		return (List<Object>) pool.invoke(parallelTask);
	}

	@SuppressWarnings("unchecked")
	public List<Object> paraTasks(Class<O> classType, O object, String methodName, Object[] parameters, int numTasks) {
		ParallelTask<O> parallelTask = new ParallelTask<O>(classType, object, methodName, parameters, numTasks);
		ForkJoinPool pool = new ForkJoinPool();
		return (List<Object>) pool.invoke(parallelTask);
	}

	private class ParallelTask<E> extends RecursiveTask<Object> {

		private static final long serialVersionUID = 1L;

		private E object;
		private String methodName;
		private Class<E> classType;
		private Object[] parameters;
		@SuppressWarnings("rawtypes")
		private Class[] parameterClasses;
		private List<Object> results;
		private Integer numResults;
		private int numCores;
		private Boolean error;
		private int numTasks;
		private Integer numForks;

		public ParallelTask(Class<E> classType, E object, String methodName, Object[] parameters) {
			this.object = object;
			this.methodName = methodName;
			this.classType = classType;
			this.results = new ArrayList<Object>();
			this.parameters = parameters;
			parameterClasses = new Class[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				parameterClasses[i] = parameters[i].getClass();
			}
			numResults = 0;
			numCores = Runtime.getRuntime().availableProcessors();
			error = false;
			numForks = 0;
			numTasks = numCores;
		}

		public ParallelTask(Class<E> classType, E object, String methodName, Object[] parameters, int numTasks) {
			this(classType, object, methodName, parameters);
			if (numTasks > 0) {
				this.numTasks = numTasks;
			}
		}

		private void execute() {
			Object result = null;
			try {
				Method m = classType.getMethod(methodName, parameterClasses);
				result = m.invoke(object, parameters);
				synchronized (results) {
					synchronized (numResults) {
						results.add(result);
						numResults++;
					}
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				synchronized (error) {
					error = true;
				}
			}
		}

		@Override
		public List<Object> compute() {
			int forkNumber;
			synchronized (numForks) {
				forkNumber = numForks;
				numForks++;
			}
			this.fork();
			if (forkNumber < numTasks) {
				this.execute();
				System.out.println("Fork N." + forkNumber + " finalizing");
			}
			while (numResults < numTasks && !error);
			if (error) {
				return null;
			} else {
				return results;
			}
		}
	}

}
