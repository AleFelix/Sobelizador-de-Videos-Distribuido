package tools;

public class Sobelizador {
	
	private static final double[][] MATRIZ_X =  {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
	private static final double[][] MATRIZ_Y =  {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
	
	public double[][] sobelizar(double[][] pixeles) {
		double pixelX, pixelY, valorFinal;
		double[][] pixelesSobelizados = new double[pixeles.length-2][pixeles[0].length-2];
		for (int x=1; x<pixeles.length-1; x++) {
			for (int y=1; y<pixeles[0].length-1; y++) {
				pixelX = (MATRIZ_X[0][0] * pixeles[x-1][y-1]) + (MATRIZ_X[0][1] * pixeles[x][y-1])
						+ (MATRIZ_X[0][2] * pixeles[x+1][y-1]) + (MATRIZ_X[1][0] * pixeles[x-1][y])
						+ (MATRIZ_X[1][1] * pixeles[x][y]) + (MATRIZ_X[1][2] * pixeles[x+1][y])
						+ (MATRIZ_X[2][0] * pixeles[x-1][y+1]) + (MATRIZ_X[2][1] * pixeles[x][y+1])
						+ (MATRIZ_X[2][2] * pixeles[x+1][y+1]);
				pixelY = (MATRIZ_Y[0][0] * pixeles[x-1][y-1]) + (MATRIZ_Y[0][1] * pixeles[x][y-1])
						+ (MATRIZ_Y[0][2] * pixeles[x+1][y-1]) + (MATRIZ_Y[1][0] * pixeles[x-1][y])
						+ (MATRIZ_Y[1][1] * pixeles[x][y]) + (MATRIZ_Y[1][2] * pixeles[x+1][y])
						+ (MATRIZ_Y[2][0] * pixeles[x-1][y+1]) + (MATRIZ_Y[2][1] * pixeles[x][y+1])
						+ (MATRIZ_Y[2][2] * pixeles[x+1][y+1]);
				valorFinal = Math.ceil(Math.sqrt((pixelX * pixelX) + (pixelY * pixelY)));
				if (valorFinal < 0) {
					valorFinal = 0;
				} else if (valorFinal > 255) {
					valorFinal = 255;
				}
				pixelesSobelizados[x-1][y-1] = valorFinal;
			}
		}
		return pixelesSobelizados;
	}
	
}
