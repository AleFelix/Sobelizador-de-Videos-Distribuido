package tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class Parallelizer<O> {

	/**
	 * Paraleliza un metodo de una clase para que se ejecute tantas veces como nucleos disponibles tenga el sistema
	 * @param classType La clase del objeto del metodo a paralelizar
	 * @param object El objeto que contiene el metodo a paralelizar
	 * @param methodName El nombre del metodo a paralelizar
	 * @param parameters Los parametros para cada ejecucion del metodo
	 * @return La salida correspondiente a cada ejecucion del metodo, con el mismo orden que los parametros de entrada
	 */
	public Object[] paraCores(Class<O> classType, O object, String methodName, Object[][] parameters) {
		ParallelTask<O> parallelTask = new ParallelTask<O>(classType, object, methodName, parameters);
		ForkJoinPool pool = new ForkJoinPool();
		return (Object[]) pool.invoke(parallelTask);
	}

	/**
	 * Paraleliza un metodo de una clase para que se ejecute tantas veces como se desee
	 * @param classType La clase del objeto del metodo a paralelizar
	 * @param object El objeto que contiene el metodo a paralelizar
	 * @param methodName El nombre del metodo a paralelizar
	 * @param parameters Los parametros para cada ejecucion del metodo
	 * @param numTasks Cantidad de ejecuciones deseadas
	 * @return La salida correspondiente a cada ejecucion del metodo, con el mismo orden que los parametros de entrada
	 */
	public Object[] paraTasks(Class<O> classType, O object, String methodName, Object[][] parameters, int numTasks) {
		ParallelTask<O> parallelTask = new ParallelTask<O>(classType, object, methodName, parameters, numTasks);
		ForkJoinPool pool = new ForkJoinPool();
		return (Object[]) pool.invoke(parallelTask);
	}

	private class ParallelTask<E> extends RecursiveTask<Object> {

		private static final long serialVersionUID = 1L;

		private E object;
		private String methodName;
		private Class<E> classType;
		private Object[][] parameters;
		@SuppressWarnings("rawtypes")
		private Class[] parameterClasses;
		private Object[] results;
		private Integer numResults;
		private int numCores;
		private Boolean error;
		private int numTasks;
		private Integer numForks;

		public ParallelTask(Class<E> classType, E object, String methodName, Object[][] parameters) {
			this.object = object;
			this.methodName = methodName;
			this.classType = classType;
			this.parameters = parameters;
			parameterClasses = new Class[parameters[0].length];
			for (int i = 0; i < parameters[0].length; i++) {
				parameterClasses[i] = getClassType(parameters[0][i].getClass());
			}
			numResults = 0;
			numCores = Runtime.getRuntime().availableProcessors();
			error = false;
			numForks = 0;
			numTasks = numCores;
			this.results = new Object[numTasks];
		}

		public ParallelTask(Class<E> classType, E object, String methodName, Object[][] parameters, int numTasks) {
			this(classType, object, methodName, parameters);
			if (numTasks > 0) {
				this.numTasks = numTasks;
				this.results = new Object[numTasks];
			}
		}

		private void execute(int forkNumber) {
			Object result = null;
			if (forkNumber < parameters.length) {
				try {
					Method m = classType.getMethod(methodName, parameterClasses);
					result = m.invoke(object, parameters[forkNumber]);
					synchronized (results) {
						synchronized (numResults) {
							results[forkNumber] = result;
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
		}

		@Override
		public Object[] compute() {
			int forkNumber;
			synchronized (numForks) {
				forkNumber = numForks;
				numForks++;
			}
			System.out.println("Soy el proceso: " + forkNumber);
			if (forkNumber < numTasks - 1) {
				ForkJoinTask<Object> child = this.fork();
				this.execute(forkNumber);
				System.out.println("Fork N." + forkNumber + " finalizing");
				child.join();
			} else if (forkNumber == numTasks - 1) {
				this.execute(forkNumber);
			}
			System.out.println("Termino el proceso: " + forkNumber);
			if (error) {
				return null;
			} else {
				return results;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private Class getClassType(Class classParameter) {
		if (Integer.class.equals(classParameter)) {
			return Integer.TYPE;
		} else if (Double.class.equals(classParameter)) {
			return Double.TYPE;
		} else if (Long.class.equals(classParameter)) {
			return Long.TYPE;
		} else if (Float.class.equals(classParameter)) {
			return Float.TYPE;
		} else if (Short.class.equals(classParameter)) {
			return Short.TYPE;
		} else if (Byte.class.equals(classParameter)) {
			return Byte.TYPE;
		} else if (Boolean.class.equals(classParameter)) {
			return Boolean.TYPE;
		} else if (Character.class.equals(classParameter)) {
			return Character.TYPE;
		} else if (Void.class.equals(classParameter)) {
			return Void.TYPE;
		} else {
			return classParameter;
		}
	}
}
