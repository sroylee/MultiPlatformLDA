package Tool;

public class Vector {
	public double euclideanDistance(double[] x, double[] y) {
		if (x.length != y.length) {
			System.out.println("ERROR: vectors of different dimensions");
			System.exit(-1);
		}
		double d = 0;
		for (int i = 0; i < x.length; i++)
			d += Math.pow(x[i] - y[i], 2);
		return Math.sqrt(d);
	}

	public double euclideanDistance(double[] x, double[] y, int[] match) {
		if (x.length != y.length || x.length != match.length) {
			System.out.println("ERROR: vectors of different dimensions");
			System.exit(-1);
		}
		double d = 0;
		for (int i = 0; i < x.length; i++) {
			int j = match[i];
			d += Math.pow(x[i] - y[j], 2);
		}
		return Math.sqrt(d);
	}

	public double weightedEuclideanDistance(double[] x, double[] y, int[] match, double[] weight) {
		if (x.length != y.length || x.length != match.length || x.length != weight.length) {
			System.out.println("ERROR: vectors of different dimensions");
			System.exit(-1);
		}
		double d = 0;
		for (int i = 0; i < x.length; i++) {
			int j = match[i];
			d += Math.pow(x[i] - y[j], 2) * weight[i];
		}
		return Math.sqrt(d);
	}

	public double klDistance(double[] x, double[] y) {
		if (x.length != y.length) {
			System.out.println("ERROR: vectors of different dimensions");
			System.exit(-1);
		}
		double d = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i] < 0 || y[i] < 0) {
				System.out.println("ERROR: not defined");
				System.exit(-1);
			}
			if (x[i] == 0) {
				if (y[i] == 0) {
					continue;
				} else {
					System.out.println("ERROR: not defined");
					System.exit(-1);
				}
			} else {
				d += x[i] * Math.log(x[i] / y[i]);
			}
		}
		return d;
	}

	public double klDistance(double[] x, double[] y, int[] match) {
		if (x.length != y.length || x.length != match.length) {
			System.out.println("ERROR: vectors of different dimensions");
			System.exit(-1);
		}
		double d = 0;
		for (int i = 0; i < x.length; i++) {
			int j = match[i];
			if (x[i] < 0 || y[j] < 0) {
				System.out.println("ERROR: not defined");
				System.exit(-1);
			}
			if (x[i] == 0) {
				if (y[j] == 0) {
					continue;
				} else {
					System.out.println("ERROR: not defined");
					System.exit(-1);
				}
			} else {
				d += x[i] * Math.log(x[i] / y[j]);
			}
		}
		return d;
	}

	public double jensenShallonDistance(double[] x, double[] y) {
		return (klDistance(x, y) + klDistance(y, x)) / 2;
	}

	public double jensenShallonDistance(double[] x, double[] y, int[] matchXY, int[] matchYX) {
		return (klDistance(x, y, matchXY) + klDistance(y, x, matchYX)) / 2;
	}

}
