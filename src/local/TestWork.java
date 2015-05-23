package local;

import java.util.Arrays;

import tools.Parallelizer;

public class TestWork {

	public static void main(String[] args) {
		TestWork t;
		Parallelizer<TestWork> p;
		long inicio, fin, duracionS, duracionP;
		for (int i = 0; i < 20; i++) {
			t = new TestWork();
			p = new Parallelizer<TestWork>();
			inicio = System.currentTimeMillis();
			System.out.println(t.fractal(0, 0, 25.0, 23));
			System.out.println(t.fractal(0, 0, 25.0, 24));
			System.out.println(t.fractal(0, 0, 25.0, 23));
			System.out.println(t.fractal(0, 0, 25.0, 24));
			System.out.println(t.fractal(0, 0, 25.0, 24));
			fin = System.currentTimeMillis();
			duracionS = (fin - inicio) / 1000;
			System.out.println("Secuencial tardo " + duracionS + " segundos");
			inicio = System.currentTimeMillis();
			System.out.println(Arrays.toString(p.paraTasks(TestWork.class, t, "fractal", new Object[][] {{ 0, 0, 25.0, 23 },{ 0, 0, 25.0, 24 },{ 0, 0, 25.0, 23 },{ 0, 0, 25.0, 24 },{ 0, 0, 25.0, 24 }},5)));
			fin = System.currentTimeMillis();
			duracionP = (fin - inicio) / 1000;
			System.out.println("Paralelo tardo " + duracionP + " segundos");
		}
	}

	public int fractal(int x1, int y1, double angle, int depth) {
		return drawTree(x1, y1, angle, depth);
	}

	private int drawTree(int x1, int y1, double angle, int depth) {
		if (depth == 0)
			return 1;
		int x2 = x1 + (int) (Math.cos(Math.toRadians(angle)) * depth * 10.0);
		int y2 = y1 + (int) (Math.sin(Math.toRadians(angle)) * depth * 10.0);
		int uno = drawTree(x2, y2, angle - 20, depth - 1);
		int dos = drawTree(x2, y2, angle + 20, depth - 1);
		return uno + dos;
	}

}
