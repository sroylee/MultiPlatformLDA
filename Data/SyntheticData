package Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;

import Tool.StatTool;

public class SyntheticData {

	public int nPlatforms;
	public int nUsers;
	public int nTopics;
	public int nWords;

	public double alpha;
	public double beta;
	public double gamma;
	public double mu;

	double[][] userTopicDistributions;// theta^u
	double[][] topicWordDistributions;// phi^t
	double[] backgroundTopicWordDistribution;// phi^b
	double backgroundTopicBias;// pi
	double[][][] userTopicPlatformDistributions;// omega_u^t
	double[][] globalTopicPlatformDistributions;// omega^t

	public String outputPath;

	private double mass = 0.9;
	private double userSkewness = 0.1;// together with mass, this means, for
										// each user, 90% of her posts are about
										// 10% of topics
	private double topicSkewness = 0.01;// similarly, each topic focuses on 1%
										// of words whose probabilities summing
										// up to 90%

	private int minNPosts = 20;
	private int maxNPosts = 100;

	private int minNWords = 10;
	private int maxNWords = 20;

	private StatTool statTool;

	public SyntheticData(int _nPlatforms, int _nUsers, int _nTopics, int _nWords, double _alpha, double _beta,
			double _gamma, double _mu, String _ouputPath) {
		nPlatforms = _nPlatforms;
		nUsers = _nUsers;
		nTopics = _nTopics;
		nWords = _nWords;

		alpha = _alpha;
		beta = _beta;
		gamma = _gamma;
		mu = _mu;
		outputPath = _ouputPath;
		statTool = new StatTool();
	}

	private void setDefaultHyperparams() {
		alpha = 50.0 / nTopics;
		beta = 1;
		gamma = 2;
		mu = 2;
	}

	public SyntheticData(int _nPlatforms, int _nUsers, int _nTopics, int _nWords, String _ouputPath) {
		nPlatforms = _nPlatforms;
		nUsers = _nUsers;
		nTopics = _nTopics;
		nWords = _nWords;

		setDefaultHyperparams();

		outputPath = _ouputPath;
		statTool = new StatTool();
	}

	private void genBackgroundTopicBias() {
		BetaDistribution betaDistribution = new BetaDistribution(gamma, gamma);
		backgroundTopicBias = betaDistribution.sample();
	}

	private void genTopics() {
		Random rand = new Random(System.currentTimeMillis());
		topicWordDistributions = new double[nTopics][];
		for (int t = 0; t < nTopics; t++) {
			topicWordDistributions[t] = statTool.sampleDirichletSkew(beta, nWords, topicSkewness, mass, rand);
		}
		backgroundTopicWordDistribution = statTool.sampleDirichletSkew(beta, nWords, topicSkewness, mass, rand);
	}

	private void genUserTopicDistribution() {
		Random rand = new Random(System.currentTimeMillis());
		userTopicDistributions = new double[nUsers][];
		for (int u = 0; u < nUsers; u++) {
			userTopicDistributions[u] = statTool.sampleDirichletSkew(alpha, nTopics, userSkewness, mass, rand);
		}
	}

	private void genUserTopicPlatformDistribution() {
		Random rand = new Random(System.currentTimeMillis());
		userTopicPlatformDistributions = new double[nUsers][nTopics][];
		for (int u = 0; u < nUsers; u++) {
			for (int t = 0; t < nTopics; t++) {
				int p = rand.nextInt(nPlatforms);// main platform
				userTopicPlatformDistributions[u][t] = statTool.sampleDirichletSkew(alpha, nPlatforms, p, mass);
			}
		}
	}

	private void genGlobalTopicPlatformDistribution() {
		Random rand = new Random(System.currentTimeMillis());
		globalTopicPlatformDistributions = new double[nTopics][];
		for (int t = 0; t < nTopics; t++) {
			int p = rand.nextInt(nPlatforms);// main platform
			globalTopicPlatformDistributions[t] = statTool.sampleDirichletSkew(alpha, nPlatforms, p, mass);
		}
	}

	private void outputParams() {
		try {
			// hyper-params
			BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/hyper-params.csv", outputPath)));
			bw.write(String.format("alpha = %f\n", alpha));
			bw.write(String.format("beta = %f\n", beta));
			bw.write(String.format("gamma = %f\n", gamma));
			bw.write(String.format("mu = %f\n", mu));
			bw.close();
			// topics
			bw = new BufferedWriter(new FileWriter(String.format("%s/topicWordDistributions.csv", outputPath)));
			for (int t = 0; t < nTopics; t++) {
				bw.write(String.format("%d", t));
				for (int w = 0; w < nWords; w++) {
					bw.write(String.format(",%f", topicWordDistributions[t][w]));
				}
				bw.write("\n");
			}
			bw.close();

			// background topics
			bw = new BufferedWriter(
					new FileWriter(String.format("%s/backgroundTopicWordDistribution.csv", outputPath)));
			bw.write(String.format("%f", backgroundTopicWordDistribution[0]));
			for (int w = 1; w < nWords; w++) {
				bw.write(String.format(",%f", backgroundTopicWordDistribution[w]));
			}
			bw.write("\n");
			bw.close();

			// background topic bias
			bw = new BufferedWriter(new FileWriter(String.format("%s/backgroundTopicBias.csv", outputPath)));
			bw.write(String.format("pi = %f\n", backgroundTopicBias));
			bw.close();

			// user topic distribution
			bw = new BufferedWriter(new FileWriter(String.format("%s/userTopicDistributions.csv", outputPath)));
			for (int u = 0; u < nUsers; u++) {
				bw.write(String.format("%d", u));
				for (int t = 0; t < nTopics; t++) {
					bw.write(String.format(",%f", userTopicDistributions[u][t]));
				}
				bw.write("\n");
			}
			bw.close();

			// user topic platform distribution
			if (userTopicPlatformDistributions != null) {
				bw = new BufferedWriter(
						new FileWriter(String.format("%s/userTopicPlatformDistributions.csv", outputPath)));
				for (int u = 0; u < nUsers; u++) {
					bw.write(String.format("%d", u));
					for (int t = 0; t < nTopics; t++) {
						for (int p = 0; p < nPlatforms; p++) {
							bw.write(String.format(",%f", userTopicPlatformDistributions[u][t][p]));
						}
					}
					bw.write("\n");
				}
				bw.close();
			}

			// global topic platform distribution
			if (globalTopicPlatformDistributions != null) {
				bw = new BufferedWriter(new FileWriter(String.format("%s/topicPlatformDistributions.csv", outputPath)));
				for (int t = 0; t < nTopics; t++) {
					bw.write(String.format("%d", t));
					for (int p = 0; p < nPlatforms; p++) {
						bw.write(String.format(",%f", globalTopicPlatformDistributions[t][p]));
					}
					bw.write("\n");
				}
				bw.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void genSyntheticDataByModel1() {
		try {
			System.out.println("generating background topic's bias");
			genBackgroundTopicBias();
			System.out.println("generating topics' word distribution");
			genTopics();
			System.out.println("generating users' topic distribution");
			genUserTopicDistribution();
			System.out.println("generating user-specific topics' platform distribution");
			genUserTopicPlatformDistribution();

			System.out.println("generating posts");
			File userDir = new File(String.format("%s/users", outputPath));
			if (!userDir.exists()) {
				userDir.mkdir();
			}
			Random rand = new Random(System.currentTimeMillis());
			for (int u = 0; u < nUsers; u++) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/users/%d.csv", outputPath, u)));
				// #posts
				int nPosts = rand.nextInt(maxNPosts - minNPosts) + minNPosts;
				for (int i = 0; i < nPosts; i++) {
					// postId
					bw.write(String.format("%d.%d", u, i));
					// topic
					int z = statTool.sampleMult(userTopicDistributions[u], false, rand);
					// platform
					int p = statTool.sampleMult(userTopicPlatformDistributions[u][z], false, rand);
					bw.write(String.format(",%d,1", p));// batch is always set
														// to 1
					// #words in the post
					int nPWords = rand.nextInt(maxNWords - minNWords) + minNWords;
					// words
					for (int j = 0; j < nPWords; j++) {
						// coin
						int w = statTool.sampleMult(backgroundTopicWordDistribution, false, rand);
						if (rand.nextDouble() >= backgroundTopicBias)
							w = statTool.sampleMult(topicWordDistributions[z], false, rand);
						if (j == 0) {// first word
							bw.write(String.format(",%d", w));
						} else {
							bw.write(String.format(" %d", w));
						}
					}
					bw.write("\n");
				}
				bw.close();
			}
			System.out.println("outputing vocabulary");
			BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/vocabulary.csv", outputPath)));
			for (int w = 0; w < nWords; w++) {
				bw.write(String.format("%d,word_%d\n", w, w));
			}
			bw.close();

			outputParams();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void genSyntheticDataByModel2() {

		try {
			System.out.println("generating background topic's bias");
			genBackgroundTopicBias();
			System.out.println("generating topics' word distribution");
			genTopics();
			System.out.println("generating users' topic distribution");
			genUserTopicDistribution();
			System.out.println("generating topics' platform distribution");
			genGlobalTopicPlatformDistribution();

			System.out.println("generating posts");
			File userDir = new File(String.format("%s/users", outputPath));
			if (!userDir.exists()) {
				userDir.mkdir();
			}
			Random rand = new Random(System.currentTimeMillis());
			for (int u = 0; u < nUsers; u++) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/users/%d.csv", outputPath, u)));
				// #posts
				int nPosts = rand.nextInt(maxNPosts - minNPosts) + minNPosts;
				for (int i = 0; i < nPosts; i++) {
					// postId
					bw.write(String.format("%d.%d", u, i));
					// topic
					int z = statTool.sampleMult(userTopicDistributions[u], false, rand);
					// platform
					int p = statTool.sampleMult(globalTopicPlatformDistributions[z], false, rand);
					bw.write(String.format(",%d,1", p));// batch is always set
														// to 1
					// #words in the post
					int nPWords = rand.nextInt(maxNWords - minNWords) + minNWords;
					// words
					for (int j = 0; j < nPWords; j++) {
						// coin
						int w = statTool.sampleMult(backgroundTopicWordDistribution, false, rand);
						if (rand.nextDouble() >= backgroundTopicBias)
							w = statTool.sampleMult(topicWordDistributions[z], false, rand);
						if (j == 0) {// first word
							bw.write(String.format(",%d", w));
						} else {
							bw.write(String.format(" %d", w));
						}
					}
					bw.write("\n");
				}
				bw.close();
			}
			System.out.println("outputing vocabulary");
			BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s/vocabulary.csv", outputPath)));
			for (int w = 0; w < nWords; w++) {
				bw.write(String.format("%d,word_%d\n", w, w));
			}
			bw.close();
			outputParams();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		SyntheticData synGenerator = new SyntheticData(2, 100, 10, 1000, "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/synthetic");
		synGenerator.genSyntheticDataByModel1();
	}
}
