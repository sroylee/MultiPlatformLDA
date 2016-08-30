package Evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Tool.HungaryMethod;
import Tool.Vector;

public class CompareWithGroundTruth {
	private String groundtruthPath;
	private String learntPath;
	private int model;
	private String distance;
	private String outputPath;

	private int nTopics;
	private int nUsers;
	private int nPlatforms;
	private int nWords;

	private HashMap<String, Integer> userId2Index;

	// groundtruth params are prefixed by "g"
	private double[][] g_topics;
	private double[][] g_userTopicDistributions;
	private double[][][] g_userTopicPlatformDistributions;
	private double[][] g_globalTopicPlatformDistributions;

	// learnt params are prefixed by "l"
	private double[][] l_topics;
	private double[][] l_userTopicDistributions;
	private double[][][] l_userTopicPlatformDistributions;
	private double[][] l_globalTopicPlatformDistributions;

	private int[] glMatch;
	private int[] lgMatch;
	private double[][] topicDistance;

	public CompareWithGroundTruth(String _groundtruthPath, String _learntPath, int _model, String _distance,
			String _outputPath) {
		groundtruthPath = _groundtruthPath;
		learntPath = _learntPath;
		model = _model;
		distance = _distance;
		outputPath = _outputPath;
	}

	private void getGroundTruth() {
		try {
			// topics
			String filename = String.format("%s/topicWordDistributions.csv", groundtruthPath);
			BufferedReader br = new BufferedReader(new FileReader(filename));
			nTopics = 1;
			String line = br.readLine();
			nWords = line.split(",").length - 1;
			while ((line = br.readLine()) != null) {
				nTopics++;
			}
			br.close();

			g_topics = new double[nTopics][nWords];
			br = new BufferedReader(new FileReader(filename));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				int t = Integer.parseInt(tokens[0]);
				for (int i = 1; i < tokens.length; i++) {
					g_topics[t][i - 1] = Double.parseDouble(tokens[i]);
				}
			}
			br.close();

			// users' topic distributions;
			filename = String.format("%s/userTopicDistributions.csv", groundtruthPath);
			br = new BufferedReader(new FileReader(filename));
			nUsers = 0;
			line = null;
			while ((line = br.readLine()) != null) {
				nUsers++;
			}
			br.close();
			userId2Index = new HashMap<String, Integer>();
			g_userTopicDistributions = new double[nUsers][nTopics];
			br = new BufferedReader(new FileReader(filename));
			int u = 0;
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				userId2Index.put(tokens[0], u);
				for (int t = 1; t < tokens.length; t++) {
					g_userTopicDistributions[u][t - 1] = Double.parseDouble(tokens[t]);
				}
				u++;
			}
			br.close();

			// topics' platform distribution
			if (model == 1) {
				filename = String.format("%s/userTopicPlatformDistributions.csv", groundtruthPath);
				br = new BufferedReader(new FileReader(filename));
				line = br.readLine();
				nPlatforms = (line.split(",").length - 1) / nTopics;
				br.close();

				g_userTopicPlatformDistributions = new double[nUsers][nTopics][nPlatforms];
				br = new BufferedReader(new FileReader(filename));
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(",");
					u = userId2Index.get(tokens[0]);
					for (int t = 0; t < nTopics; t++) {
						for (int p = 0; p < nPlatforms; p++) {
							g_userTopicPlatformDistributions[u][t][p] = Double
									.parseDouble(tokens[t * nPlatforms + p + 1]);
						}
					}
				}
				br.close();
			} else {
				filename = String.format("%s/topicPlatformDistributions.csv", groundtruthPath);
				br = new BufferedReader(new FileReader(filename));
				line = br.readLine();
				nPlatforms = line.split(",").length - 1;
				br.close();

				g_globalTopicPlatformDistributions = new double[nTopics][nPlatforms];
				br = new BufferedReader(new FileReader(filename));
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(",");
					int t = Integer.parseInt(tokens[0]);
					for (int p = 1; p < tokens.length - 1; p++) {
						g_globalTopicPlatformDistributions[t][p - 1] = Double.parseDouble(tokens[p]);
					}
				}
				br.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void getLearntParams() {
		try {
			// topics
			String filename = String.format("%s/topicWordDistributions.csv", learntPath);
			l_topics = new double[nTopics][nWords];
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				int t = Integer.parseInt(tokens[0]);
				for (int i = 1; i < tokens.length; i++) {
					l_topics[t][i - 1] = Double.parseDouble(tokens[i]);
				}
			}
			br.close();

			// users' topic distributions;
			filename = String.format("%s/userTopicDistributions.csv", learntPath);
			l_userTopicDistributions = new double[nUsers][nTopics];
			br = new BufferedReader(new FileReader(filename));
			line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				int u = userId2Index.get(tokens[0]);
				for (int t = 1; t < tokens.length; t++) {
					l_userTopicDistributions[u][t - 1] = Double.parseDouble(tokens[t]);
				}
			}
			br.close();

			// topics' platform distribution
			if (model == 1) {
				filename = String.format("%s/userTopicPlatformDistributions.csv", learntPath);
				l_userTopicPlatformDistributions = new double[nUsers][nTopics][nPlatforms];
				br = new BufferedReader(new FileReader(filename));
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(",");
					int u = userId2Index.get(tokens[0]);
					for (int t = 0; t < nTopics; t++) {
						for (int p = 0; p < nPlatforms; p++) {
							l_userTopicPlatformDistributions[u][t][p] = Double
									.parseDouble(tokens[t * nPlatforms + p + 1]);
						}
					}
				}
				br.close();
			} else {
				filename = String.format("%s/topicPlatformDistributions.csv", learntPath);
				l_globalTopicPlatformDistributions = new double[nTopics][nPlatforms];
				br = new BufferedReader(new FileReader(filename));
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(",");
					int t = Integer.parseInt(tokens[0]);
					for (int p = 1; p < tokens.length - 1; p++) {
						l_globalTopicPlatformDistributions[t][p - 1] = Double.parseDouble(tokens[p]);
					}
				}
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void topicMatching() {

		Vector vector = new Vector();

		topicDistance = new double[nTopics][nTopics];
		for (int t = 0; t < nTopics; t++) {
			for (int k = 0; k < nTopics; k++) {
				if (distance.equals("euclidean")) {
					topicDistance[t][k] = vector.euclideanDistance(g_topics[t], l_topics[k]);
				} else {
					topicDistance[t][k] = vector.jensenShallonDistance(g_topics[t], l_topics[k]);
				}
				if (topicDistance[t][k] < 0) {
					System.out.println("something wrong!!!!");
					System.exit(-1);
				}
			}
		}
		System.out.println("Cost:");
		for (int t = 0; t < nTopics; t++) {
			System.out.printf("%f", topicDistance[t][0]);
			for (int k = 1; k < nTopics; k++) {
				System.out.printf(" %f", topicDistance[t][k]);
			}
			System.out.println("");
		}

		HungaryMethod matcher = new HungaryMethod(topicDistance);
		glMatch = matcher.execute();
		lgMatch = new int[nTopics];
		for (int i = 0; i < nTopics; i++) {
			int j = glMatch[i];
			lgMatch[j] = i;
		}
	}

	public void measureGoodness() {
		try {
			System.out.println("getting groundtruth");
			getGroundTruth();
			System.out.println("getting learnt parameters");
			getLearntParams();

			System.out.printf("#words = %d #users = %d #topics = %d #platform = %d\n", nWords, nUsers, nTopics,
					nPlatforms);

			System.out.println("matching topics");
			topicMatching();
			String filename = String.format("%s/topicDistance.csv", outputPath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int t = 0; t < nTopics; t++) {
				bw.write(String.format("%d,%f\n", t, topicDistance[t][glMatch[t]]));
			}
			bw.close();

			System.out.println("measuring users' topic distribution distance");
			Vector vector = new Vector();
			filename = String.format("%s/userTopicDistance.csv", outputPath);
			bw = new BufferedWriter(new FileWriter(filename));
			Iterator<Map.Entry<String, Integer>> iter = userId2Index.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Integer> pair = iter.next();
				int u = pair.getValue();
				if (distance.equals("euclidean")) {
					bw.write(String.format("%s,%f\n", pair.getKey(),
							vector.weightedEuclideanDistance(g_userTopicDistributions[u], l_userTopicDistributions[u],
									glMatch, g_userTopicDistributions[u])));
				} else {
					bw.write(String.format("%s,%f\n", pair.getKey(), vector.jensenShallonDistance(
							g_userTopicDistributions[u], l_userTopicDistributions[u], glMatch, lgMatch)));
				}
			}
			bw.close();

			if (model == 1) {
				System.out.println("measuring user-specific topics' platform distribution distance");
				filename = String.format("%s/userTopicPlatformAvgDistance.csv", outputPath);
				bw = new BufferedWriter(new FileWriter(filename));
				iter = userId2Index.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Integer> pair = iter.next();
					int u = pair.getValue();
					double avgDistance = 0;
					for (int t = 0; t < nTopics; t++) {
						int k = glMatch[t];
						if (distance.equals("euclidean")) {
							avgDistance += vector.euclideanDistance(g_userTopicPlatformDistributions[u][t],
									l_userTopicPlatformDistributions[u][k]) * g_userTopicDistributions[u][t];
						} else {
							avgDistance += vector.jensenShallonDistance(g_userTopicPlatformDistributions[u][t],
									l_userTopicPlatformDistributions[u][k]) * g_userTopicDistributions[u][t];
						}
					}
					bw.write(String.format("%s,%f\n", pair.getKey(), avgDistance));
				}
				bw.close();
			} else {
				filename = String.format("%s/globalTopicPlatformDistance.csv", outputPath);
				bw = new BufferedWriter(new FileWriter(filename));
				for (int t = 0; t < nTopics; t++) {
					int k = glMatch[t];
					if (distance.equals("euclidean")) {
						bw.write(String.format("%d,%f\n", t, vector.euclideanDistance(
								g_globalTopicPlatformDistributions[t], l_globalTopicPlatformDistributions[k])));
					} else {
						bw.write(String.format("%d,%f\n", t, vector.jensenShallonDistance(
								g_globalTopicPlatformDistributions[t], l_globalTopicPlatformDistributions[k])));
					}
				}
				bw.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public static void main(String[] args) {
		CompareWithGroundTruth comparator = new CompareWithGroundTruth("F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/synthetic",
				"F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/synthetic/output/10", 1, "euclidean", "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/synthetic/evaluation");
		comparator.measureGoodness();
	}
}
