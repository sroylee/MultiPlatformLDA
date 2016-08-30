package Tool;

import java.util.Random;
import org.apache.commons.math3.distribution.*;

public class StatTool {
	public int sampleMult(double[] prob, boolean flag, Random rand) {
		double p = 0;
		if (!flag) {// prob is not cumulative
			for (int i = 0; i < prob.length; i++) {
				p += prob[i];
			}
			p = rand.nextDouble() * p;
		} else {
			p = rand.nextDouble() * prob[prob.length - 1];
		}

		if (!flag) {
			double csum = 0;
			for (int i = 0; i < prob.length; i++) {
				csum += prob[i];
				if (csum > p)
					return i;
			}
		} else {
			for (int i = 0; i < prob.length; i++) {
				if (prob[i] > p)
					return i;
			}
		}

		System.out.println("p = " + p);
		for (int i = 0; i < prob.length; i++)
			System.out.println(prob[i] + "\t");

		System.out.println("mult sample failed!");
		System.exit(-1);
		return -1;
	}

	public double[] sampleDirichlet(double[] alpha) {
		double[] instance = new double[alpha.length];
		double sum = 0;
		for (int i = 0; i < instance.length; i++) {
			GammaDistribution gamma = new GammaDistribution(alpha[i], 1);
			instance[i] = gamma.sample();
			if (instance[i] < 0) {
				System.out.println("gamma sample failed!");
				System.exit(-1);
			}
			sum += instance[i];
		}
		for (int i = 0; i < instance.length; i++) {
			instance[i] /= sum;
		}
		return instance;
	}

	public double[] sampleDirichlet(double alpha, int dim) {
		double[] instance = new double[dim];
		double sum = 0;
		for (int i = 0; i < instance.length; i++) {
			GammaDistribution gamma = new GammaDistribution(alpha, 1);
			instance[i] = gamma.sample();
			if (instance[i] < 0) {
				System.out.println("gamma sample failed!");
				System.exit(-1);
			}
			sum += instance[i];
		}
		for (int i = 0; i < instance.length; i++) {
			instance[i] /= sum;
		}
		return instance;
	}

	public double[] sampleDirichletSkew(double[] alpha, int massIndex, double proportion) {
		if (proportion < 0 || proportion > 1)
			return sampleDirichlet(alpha);

		if (massIndex < 0 || massIndex >= alpha.length)
			return sampleDirichlet(alpha);

		double[] alphaPrime = new double[alpha.length - 1];
		int k = 0;
		for (int i = 0; i < alpha.length; i++) {
			if (i != massIndex) {
				alphaPrime[k] = alpha[i];
				k++;
			}
		}
		double[] instancePrime = sampleDirichlet(alphaPrime);
		double[] instance = new double[alpha.length];
		k = 0;
		for (int i = 0; i < alpha.length; i++) {
			if (i != massIndex) {
				instance[i] = instancePrime[k] * (1 - proportion);
				k++;
			} else
				instance[i] = proportion;
		}
		return instance;
	}

	public double[] sampleDirichletSkew(double alpha, int dim, int massIndex, double proportion) {
		if (proportion < 0 || proportion > 1)
			return sampleDirichlet(alpha, dim);

		if (massIndex < 0 || massIndex >= dim)
			return sampleDirichlet(alpha, dim);

		double[] instancePrime = sampleDirichlet(alpha, dim - 1);
		double[] instance = new double[dim];
		int k = 0;
		for (int i = 0; i < dim; i++) {
			if (i != massIndex) {
				instance[i] = instancePrime[k] * (1 - proportion);
				k++;
			} else
				instance[i] = proportion;
		}
		return instance;
	}

	public double[] sampleDirichletSkew(double[] alpha, double focusPropotion, double massProportion, Random rand) {
		if (massProportion < 0 || massProportion > 1)
			return sampleDirichlet(alpha);
		if (focusPropotion < 0 || focusPropotion > 1)
			return sampleDirichlet(alpha);

		int nFocusElements = (int) Math.round(alpha.length * focusPropotion);

		boolean[] focusMark = new boolean[alpha.length];
		for (int i = 0; i < focusMark.length; i++)
			focusMark[i] = false;

		for (int i = 0; i < nFocusElements; i++) {
			int index = rand.nextInt(alpha.length);
			while (focusMark[index]) {
				index = rand.nextInt(alpha.length);
			}
			focusMark[index] = true;
		}

		double[] alphaPrime = new double[alpha.length - nFocusElements];
		double[] beta = new double[nFocusElements];
		int k = 0;
		int l = 0;
		for (int i = 0; i < alpha.length; i++) {
			if (!focusMark[i]) {
				alphaPrime[k] = alpha[i];
				k++;
			} else {
				beta[l] = alpha[i];
				l++;
			}
		}
		double[] nonFocusInstance = sampleDirichlet(alphaPrime);
		double[] focusInstance = sampleDirichlet(beta);
		double[] instance = new double[alpha.length];
		k = 0;
		l = 0;
		for (int i = 0; i < alpha.length; i++) {
			if (!focusMark[i]) {
				instance[i] = nonFocusInstance[k] * (1 - massProportion);
				k++;
			} else {
				instance[i] = focusInstance[l] * massProportion;
				l++;
			}
		}
		return instance;
	}

	public double[] sampleDirichletSkew(double alpha, int dim, double focusPropotion, double massProportion,
			Random rand) {
		if (massProportion < 0 || massProportion > 1)
			return sampleDirichlet(alpha, dim);
		if (focusPropotion < 0 || focusPropotion > 1)
			return sampleDirichlet(alpha, dim);

		int nFocusElements = (int) Math.round(dim * focusPropotion);

		boolean[] focusMark = new boolean[dim];
		for (int i = 0; i < focusMark.length; i++)
			focusMark[i] = false;

		for (int i = 0; i < nFocusElements; i++) {
			int index = rand.nextInt(dim);
			while (focusMark[index]) {
				index = rand.nextInt(dim);
			}
			focusMark[index] = true;
		}

		double[] nonFocusInstance = sampleDirichlet(alpha, dim - nFocusElements);
		double[] focusInstance = sampleDirichlet(alpha, nFocusElements);
		double[] instance = new double[dim];
		int k = 0;
		int l = 0;
		for (int i = 0; i < dim; i++) {
			if (!focusMark[i]) {
				instance[i] = nonFocusInstance[k] * (1 - massProportion);
				k++;
			} else {
				instance[i] = focusInstance[l] * massProportion;
				l++;
			}
		}
		return instance;
	}

	public double getKLDistance(double[] p, double[] q) {
		int d = 0;
		for (int i = 0; i < p.length; i++) {
			if (q[i] < 0 || p[i] < 0)
				return -1;
			if (q[i] <= Double.MIN_NORMAL && p[i] > Double.MIN_NORMAL)
				return -1;
			if (q[i] <= Double.MIN_NORMAL && p[i] == Double.MIN_NORMAL)
				continue;
			d += Math.log(p[i] / q[i]) * p[i];
		}
		return d;
	}

	public double getKLDistance(double[] p, double[] q, int[] indexMatching) {
		int d = 0;
		for (int i = 0; i < p.length; i++) {
			int j = indexMatching[i];
			if (q[j] < 0 || p[i] < 0)
				return -1;
			if (q[j] <= Double.MIN_NORMAL && p[i] > Double.MIN_NORMAL)
				return -1;
			if (q[j] <= Double.MIN_NORMAL && p[i] == Double.MIN_NORMAL)
				continue;
			d += Math.log(p[i] / q[j]) * p[i];
		}
		return d;
	}

	public double getEuclideanDistance(double[] p, double[] q) {
		double d = 0;
		for (int i = 0; i < p.length; i++) {
			d += Math.pow(p[i] - q[i], 2);
		}
		d = Math.sqrt(d);
		return d;
	}

	public double getEuclideanDistance(double[] p, double[] q, int[] indexMatching) {
		double d = 0;
		for (int i = 0; i < p.length; i++) {
			int j = indexMatching[i];
			d += Math.pow(p[i] - q[j], 2);
		}
		d = Math.sqrt(d);
		return d;
	}
}
