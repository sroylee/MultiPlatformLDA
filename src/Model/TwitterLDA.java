package Model;

import hoang.larc.tooler.RankingTool;
import hoang.larc.tooler.SystemTool;
import hoang.larc.tooler.WeightedElement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

public class TwitterLDA {
	//
	public String dataPath;
	public int platform;
	public String outputPath;
	public int nTopics;

	public int burningPeriod;
	public int maxIteration;
	public int samplingGap;
	public int testBatch;

	public Random rand;

	public boolean toOutputLikelihoodPerplexity;// option to output likelihood
	// of train data and perplexity
	// of test data

	public boolean toOutputTopicTopPosts;// option to output top posts of each
	// topic
	public boolean toOutputInferedTopics;// option to output (all) posts'
	// inferred topics
	public boolean toOutputInferedPlatforms;// option to output inferred
	// platform for posts in test batch

	// hyperparameters
	private double alpha;
	private double sum_alpha;
	private double beta;
	private double sum_beta;
	private double[] gamma;
	private double sum_gamma;
	// data
	private User[] users;

	private String[] vocabulary;
	// parameters
	private double[][] postTopics;
	private double[] backgroundTopic;
	private double[] coinBias;

	// Gibbs sampling variables
	// user - topic count
	private int[][] n_zu; // n_zu[k,u]: number of times topic z is observed in
							// posts by user u
	private int[] sum_nzu;// sum_nzu[u]: total number of topics that are
							// observed in posts by user u
	// topic - word count
	private int[][] n_wz;// n_wz[w,z]: number of times word w is generated by a
							// topic z in all posts
	private int[] sum_nwz;// sum_nw[z]: total number of words that are generated
							// by a topic z in posts
	private int[] n_wb;// n_wz[w]: number of times word w is generated by a
						// background topic
	private int sum_nwb;// sum_nw[z]: total number of words that are generated
						// by background topic

	// topic - coin count
	private int[] n_c;// sum_nw[c]: total number of words that are
						// associated with coin c
	private int sum_nc;

	private int[][] final_n_zu;
	private int[] final_sum_nzu;
	private int[][] final_n_wz;
	private int[] final_sum_nwz;
	private int[] final_n_wb;
	private int final_sum_nwb;
	private int[] final_n_c;
	private int final_sum_nc;

	private double postLogLikelidhood;
	private double postLogPerplexity;

	public void readData() {
		Scanner sc = null;
		BufferedReader br = null;
		String line = null;
		HashMap<String, Integer> userId2Index = null;
		HashMap<Integer, String> userIndex2Id = null;

		try {
			String folderName = dataPath + "/users";
			System.out.println("folderName = " + folderName);
			File postFolder = new File(folderName);

			// Read number of users
			int nUser = postFolder.listFiles().length;
			users = new User[nUser];
			userId2Index = new HashMap<String, Integer>(nUser);
			userIndex2Id = new HashMap<Integer, String>(nUser);
			int u = -1;

			// Read the posts from each user file
			for (File postFile : postFolder.listFiles()) {
				u++;

				System.out.printf("reading user: %d/%d\n", u, nUser);

				users[u] = new User();

				// Read index of the user
				String userId = FilenameUtils.removeExtension(postFile.getName());
				userId2Index.put(userId, u);
				userIndex2Id.put(u, userId);
				users[u].userID = userId;

				// Read the number of posts from user
				// Read the number of posts from user
				int nPost = 0;
				br = new BufferedReader(new FileReader(postFile.getAbsolutePath()));
				while (br.readLine() != null) {
					nPost++;
				}
				br.close();

				// Declare the number of posts from user
				users[u].posts = new Post[nPost];

				// Read each of the post
				br = new BufferedReader(new FileReader(postFile.getAbsolutePath()));
				int j = -1;
				while ((line = br.readLine()) != null) {
					
					j++;
					users[u].posts[j] = new Post();

					sc = new Scanner(line.toString());
					sc.useDelimiter(",");
					while (sc.hasNext()) {
						users[u].posts[j].postID = sc.next();
						users[u].posts[j].platform = sc.nextInt();
						users[u].posts[j].batch = sc.nextInt();
						
					
						// Read the words in each post
						String[] tokens = sc.next().toString().split(" ");
						users[u].posts[j].words = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++) {
							users[u].posts[j].words[i] = Integer.parseInt(tokens[i]);
						}
					}
				}
				br.close();
			}

			// Read post vocabulary
			String vocabularyFileName = dataPath + "/vocabulary.csv";

			br = new BufferedReader(new FileReader(vocabularyFileName));
			int nPostWord = 0;
			while (br.readLine() != null) {
				nPostWord++;
			}
			br.close();
			vocabulary = new String[nPostWord];

			br = new BufferedReader(new FileReader(vocabularyFileName));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				int index = Integer.parseInt(tokens[0]);
				vocabulary[index] = tokens[1];
			}
			br.close();

		} catch (Exception e) {
			System.out.println("Error in reading post from file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void declareFinalCounts() {
		final_n_zu = new int[nTopics][users.length];
		final_sum_nzu = new int[users.length];
		for (int u = 0; u < users.length; u++) {
			for (int z = 0; z < nTopics; z++)
				final_n_zu[z][u] = 0;
			final_sum_nzu[u] = 0;
		}
		final_n_wz = new int[vocabulary.length][nTopics];
		final_sum_nwz = new int[nTopics];
		for (int z = 0; z < nTopics; z++) {
			for (int w = 0; w < vocabulary.length; w++)
				final_n_wz[w][z] = 0;
			final_sum_nwz[z] = 0;
		}

		final_n_wb = new int[vocabulary.length];
		for (int w = 0; w < vocabulary.length; w++)
			final_n_wb[w] = 0;
		final_sum_nwb = 0;

		final_n_c = new int[2];
		for (int c = 0; c < 2; c++) {
			final_n_c[c] = 0;
		}
		final_sum_nc = 0;
	}

	private void initilize() {
		// init coin and topic for each post
		for (int u = 0; u < users.length; u++) {
			// post
			for (int j = 0; j < users[u].posts.length; j++) {
				if (users[u].posts[j].batch == testBatch)
					continue;
				users[u].posts[j].topic = rand.nextInt(nTopics);
				int nWords = users[u].posts[j].words.length;
				users[u].posts[j].coins = new int[nWords];
				for (int i = 0; i < users[u].posts[j].coins.length; i++)
					users[u].posts[j].coins[i] = rand.nextInt(2);
			}
		}
		// declare and initiate counting tables
		n_zu = new int[nTopics][users.length];
		sum_nzu = new int[users.length];
		for (int u = 0; u < users.length; u++) {
			for (int z = 0; z < nTopics; z++)
				n_zu[z][u] = 0;
			sum_nzu[u] = 0;
		}
		n_wz = new int[vocabulary.length][nTopics];
		sum_nwz = new int[nTopics];
		for (int z = 0; z < nTopics; z++) {
			for (int w = 0; w < vocabulary.length; w++)
				n_wz[w][z] = 0;
			sum_nwz[z] = 0;
		}
		n_wb = new int[vocabulary.length];
		for (int w = 0; w < vocabulary.length; w++)
			n_wb[w] = 0;
		sum_nwb = 0;

		n_c = new int[2];
		n_c[0] = 0;
		n_c[1] = 0;
		sum_nc = 0;
		// update counting tables
		for (int u = 0; u < users.length; u++) {
			// post
			for (int j = 0; j < users[u].posts.length; j++) {
				if (users[u].posts[j].batch == testBatch)
					continue;
				int z = users[u].posts[j].topic;
				// user-topic and community-topic
				n_zu[z][u]++;
				sum_nzu[u]++;
				for (int i = 0; i < users[u].posts[j].words.length; i++) {
					int w = users[u].posts[j].words[i];
					int c = users[u].posts[j].coins[i];
					// coin count
					n_c[c]++;
					sum_nc++;
					if (c == 0) {
						// word - background topic
						n_wb[w]++;
						sum_nwb++;
					} else {
						// word - topic
						n_wz[w][z]++;
						sum_nwz[z]++;
					}

				}
			}
		}
	}

	// sampling
	private void setPriors() {
		// user topic prior
		alpha = 50.0 / nTopics;
		sum_alpha = 50;

		// topic post word prior
		beta = 0.01;
		sum_beta = 0.01 * vocabulary.length;
		// biased coin prior
		gamma = new double[2];
		gamma[0] = 2;
		gamma[1] = 2;
		sum_gamma = gamma[0] + gamma[1];
	}

	private void samplePostTopic(int u, int j) {
		// sample the topic for post number j of user number u
		// get current topic
		int currz = users[u].posts[j].topic;
		// sampling based on user interest
		n_zu[currz][u]--;
		sum_nzu[u]--;
		for (int i = 0; i < users[u].posts[j].words.length; i++) {
			if (users[u].posts[j].coins[i] == 0)
				continue;// do not consider background words
			int w = users[u].posts[j].words[i];
			n_wz[w][currz]--;
			sum_nwz[currz]--;
		}
		double sump = 0;
		double[] p = new double[nTopics];
		for (int z = 0; z < nTopics; z++) {
			p[z] = (n_zu[z][u] + alpha) / (sum_nzu[u] + sum_alpha);
			for (int i = 0; i < users[u].posts[j].words.length; i++) {
				if (users[u].posts[j].coins[i] == 0)
					continue;// do not consider background words
				int w = users[u].posts[j].words[i];
				p[z] = p[z] * (n_wz[w][z] + beta) / (sum_nwz[z] + sum_beta);
			}
			// cumulative
			p[z] = sump + p[z];
			sump = p[z];
		}
		sump = rand.nextDouble() * sump;
		for (int z = 0; z < nTopics; z++) {
			if (sump > p[z])
				continue;
			// the topic
			users[u].posts[j].topic = z;
			// user - topic
			n_zu[z][u]++;
			sum_nzu[u]++;
			// topic - word
			for (int i = 0; i < users[u].posts[j].words.length; i++) {
				if (users[u].posts[j].coins[i] == 0)
					continue;// do not consider background words
				int w = users[u].posts[j].words[i];
				n_wz[w][z]++;
				sum_nwz[z]++;
			}
			return;
		}
		System.out.println("bug in samplePostTopic");
		for (int z = 0; z < nTopics; z++) {
			System.out.print(p[z] + " ");
		}
		System.exit(-1);
	}

	private void sampleWordCoin(int u, int j, int i) {
		// sample the coin for the word number i of the post number j of user
		// number u
		// get current coin
		int currc = users[u].posts[j].coins[i];
		// get current word
		int w = users[u].posts[j].words[i];
		// get current topic
		int z = users[u].posts[j].topic;
		// coin count
		n_c[currc]--;
		sum_nc--;
		if (currc == 0) {
			// word - background topic
			n_wb[w]--;
			sum_nwb--;
		} else {
			// word - topic
			n_wz[w][z]--;
			sum_nwz[z]--;
		}

		// probability of coin 0 given priors and recent counts
		double p_0 = (n_c[0] + gamma[0]) / (sum_nc + sum_gamma);
		// probability of w given coin 0
		p_0 = p_0 * (n_wb[w] + beta) / (sum_nwb + sum_beta);

		// probability of coin 1 given priors and recent counts
		double p_1 = (n_c[1] + gamma[1]) / (sum_nc + sum_gamma);
		// probability of w given coin 1 and topic z
		p_1 = p_1 * (n_wz[w][z] + beta) / (sum_nwz[z] + sum_beta);

		double sump = p_0 + p_1;
		sump = rand.nextDouble() * sump;
		int c = 0;
		if (sump > p_0)
			c = 1;
		// the coin
		users[u].posts[j].coins[i] = c;
		// coin count
		n_c[c]++;
		sum_nc++;
		if (c == 0) {
			// word-background topic
			n_wb[w]++;
			sum_nwb++;
		} else {
			// word - topic
			n_wz[w][z]++;
			sum_nwz[z]++;
		}
	}

	private void updateFinalCounts() {
		for (int u = 0; u < users.length; u++) {
			for (int z = 0; z < nTopics; z++)
				final_n_zu[z][u] += n_zu[z][u];
			final_sum_nzu[u] += sum_nzu[u];
		}

		for (int z = 0; z < nTopics; z++) {
			for (int w = 0; w < vocabulary.length; w++)
				final_n_wz[w][z] += n_wz[w][z];
			final_sum_nwz[z] += sum_nwz[z];
		}

		for (int w = 0; w < vocabulary.length; w++)
			final_n_wb[w] += n_wb[w];
		final_sum_nwb += sum_nwb;

		for (int c = 0; c < 2; c++) {
			final_n_c[c] += n_c[c];
		}
		final_sum_nc += sum_nc;
	}

	private void gibbsSampling() {
		System.out.println("Runing Gibbs sampling");
		System.out.print("Setting prios ...");
		setPriors();
		System.out.println(" Done!");
		declareFinalCounts();
		System.out.print("Initializing ... ");
		initilize();
		System.out.println("... Done!");
		for (int iter = 0; iter < burningPeriod + maxIteration; iter++) {
			System.out.print("iteration " + iter);
			// topic
			for (int u = 0; u < users.length; u++) {
				for (int j = 0; j < users[u].posts.length; j++) {
					if (users[u].posts[j].batch == testBatch)
						continue;
					samplePostTopic(u, j);
				}
			}
			// coin
			for (int u = 0; u < users.length; u++) {
				for (int j = 0; j < users[u].posts.length; j++) {
					if (users[u].posts[j].batch == testBatch)
						continue;
					for (int i = 0; i < users[u].posts[j].words.length; i++)
						sampleWordCoin(u, j, i);
				}
			}

			System.out.println(" done!");
			if (samplingGap <= 0)
				continue;
			if (iter < burningPeriod)
				continue;
			if ((iter - burningPeriod) % samplingGap == 0) {
				updateFinalCounts();
			}
		}
		if (samplingGap <= 0)
			updateFinalCounts();
	}

	// inference
	private void inferingModelParameters() {
		// user
		for (int u = 0; u < users.length; u++) {
			// topic distribution
			users[u].topicDistribution = new double[nTopics];
			for (int z = 0; z < nTopics; z++) {
				users[u].topicDistribution[z] = (final_n_zu[z][u] + alpha) / (final_sum_nzu[u] + sum_alpha);
			}
		}
		// topics
		postTopics = new double[nTopics][vocabulary.length];
		for (int z = 0; z < nTopics; z++) {
			for (int w = 0; w < vocabulary.length; w++)
				postTopics[z][w] = (final_n_wz[w][z] + beta) / (final_sum_nwz[z] + sum_beta);
		}

		// background topics
		backgroundTopic = new double[vocabulary.length];
		for (int w = 0; w < vocabulary.length; w++)
			backgroundTopic[w] = (final_n_wb[w] + beta) / (final_sum_nwb + sum_beta);
		// coin bias
		coinBias = new double[2];
		coinBias[0] = (final_n_c[0] + gamma[0]) / (final_sum_nc + sum_gamma);
		coinBias[1] = (final_n_c[1] + gamma[1]) / (final_sum_nc + sum_gamma);

	}

	public void learnModel() {
		gibbsSampling();
		inferingModelParameters();
	}

	private double getPostLikelihood(int u, int j) {
		// compute likelihood of post number t of user number u
		double logLikelihood = 0;
		for (int i = 0; i < users[u].posts[j].words.length; i++) {
			int w = users[u].posts[j].words[i];
			// probability that word i is generated by background topic
			double p_0 = backgroundTopic[w] * coinBias[0];
			// probability that word i is generated by other topics
			double p_1 = 0;
			for (int z = 0; z < nTopics; z++) {
				double p_z = postTopics[z][w] * users[u].topicDistribution[z];
				p_1 = p_1 + p_z;
			}
			p_1 = p_1 * coinBias[1];

			logLikelihood = logLikelihood + Math.log10(p_0 + p_1);
			/*
			 * if (Double.isNaN(logLikelihood)) { System.out.println("p_0 = " +
			 * p_0 + "\tp_1 = " + p_1); // System.exit(-1); }
			 */
		}

		return logLikelihood;
	}

	private double getPostLikelihood(int u, int j, int z) {
		// compute likelihood of post number t of user number u given the topic
		// z
		if (z >= 0) {
			double logLikelihood = 0;
			for (int i = 0; i < users[u].posts[j].words.length; i++) {
				int w = users[u].posts[j].words[i];
				// probability that word i is generated by background topic
				double p_0 = backgroundTopic[w] * coinBias[0];
				// probability that word i is generated by topic z
				double p_1 = postTopics[z][w] * coinBias[1];
				logLikelihood = logLikelihood + Math.log10(p_0 + p_1);
			}
			return logLikelihood;
		} else {
			double logLikelihood = 0;
			for (int i = 0; i < users[u].posts[j].words.length; i++) {
				int w = users[u].posts[j].words[i];
				// probability that word i is generated by background topic
				double p_0 = backgroundTopic[w];
				logLikelihood = logLikelihood + Math.log10(p_0);
			}
			return logLikelihood;
		}
	}

	private void getLikelihoodPerplexity() {
		postLogLikelidhood = 0;
		postLogPerplexity = 0;
		int nTestPost = 0;
		for (int u = 0; u < users.length; u++) {
			// post
			for (int t = 0; t < users[u].posts.length; t++) {
				double logLikelihood = getPostLikelihood(u, t);
				if (users[u].posts[t].batch != testBatch)
					postLogLikelidhood += logLikelihood;
				else {
					postLogPerplexity += (-logLikelihood);
					nTestPost++;
				}
			}
		}
		postLogPerplexity /= nTestPost;
	}

	private void inferPostTopic() {
		for (int u = 0; u < users.length; u++) {
			for (int t = 0; t < users[u].posts.length; t++) {
				users[u].posts[t].inferedTopic = -1;// background topic only
				users[u].posts[t].inferedLikelihood = users[u].posts.length * Math.log10(coinBias[0])
						+ getPostLikelihood(u, t, -1);

				for (int z = 0; z < nTopics; z++) {
					double p_z = getPostLikelihood(u, t, z);
					p_z += Math.log10(users[u].topicDistribution[z]);

					if (users[u].posts[t].inferedLikelihood < p_z) {
						users[u].posts[t].inferedLikelihood = p_z;
						users[u].posts[t].inferedTopic = z;
					}
				}
			}
		}
	}

	private void outputPostTopicWordDistributions() {
		try {
			String fileName = outputPath + "/topicWordDistributions.csv";
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			for (int z = 0; z < nTopics; z++) {
				bw.write("" + z);
				for (int w = 0; w < vocabulary.length; w++)
					bw.write("," + postTopics[z][w]);
				bw.write("\n");
			}
			bw.write("background");
			for (int w = 0; w < vocabulary.length; w++)
				bw.write("," + backgroundTopic[w]);
			bw.write("\n");
			bw.close();
		} catch (Exception e) {
			System.out.println("Error in writing out post topics to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void outputCoinBias() {
		try {
			String fileName = outputPath + "/backgroundTopicBias.csv";
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			bw.write(coinBias[0] + "," + coinBias[1]);
			bw.close();
		} catch (Exception e) {
			System.out.println("Error in writing out coin bias to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void outputPostTopicTopWords(int k) {
		try {
			String fileName = outputPath + "/topicTopWords.csv";
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			RankingTool rankTool = new RankingTool();
			WeightedElement[] topWords = null;
			for (int z = 0; z < nTopics; z++) {
				bw.write(z + "\n");
				topWords = rankTool.getTopKbyWeight(vocabulary, postTopics[z], k);
				for (int j = 0; j < k; j++)
					bw.write("," + topWords[j].name + "," + topWords[j].weight + "\n");
			}

			bw.write("background\n");
			topWords = rankTool.getTopKbyWeight(vocabulary, backgroundTopic, 2 * k);
			for (int j = 0; j < 2 * k; j++)
				bw.write("," + topWords[j].name + "," + topWords[j].weight + "\n");

			bw.close();
		} catch (Exception e) {
			System.out.println("Error in writing out post topic top words to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void outputPostTopicTopPosts(int k) {
		int[] nTopicPosts = new int[nTopics];
		int nBackgroundTopicPosts = 0;
		for (int z = 0; z < nTopics; z++)
			nTopicPosts[z] = 0;
		for (int u = 0; u < users.length; u++) {
			for (int t = 0; t < users[u].posts.length; t++) {
				if (users[u].posts[t].batch == testBatch)
					continue;
				if (users[u].posts[t].inferedTopic >= 0)
					nTopicPosts[users[u].posts[t].inferedTopic]++;
				else
					nBackgroundTopicPosts++;
			}
		}

		String[][] postID = new String[nTopics][];
		double[][] postPerplexity = new double[nTopics][];
		for (int z = 0; z < nTopics; z++) {
			postID[z] = new String[nTopicPosts[z]];
			postPerplexity[z] = new double[nTopicPosts[z]];
			nTopicPosts[z] = 0;
		}
		String[] backgroundPostID = new String[nBackgroundTopicPosts];
		double[] backgroundPostPerplexity = new double[nBackgroundTopicPosts];
		nBackgroundTopicPosts = 0;

		for (int u = 0; u < users.length; u++) {
			for (int t = 0; t < users[u].posts.length; t++) {
				if (users[u].posts[t].batch == testBatch)
					continue;
				int z = users[u].posts[t].inferedTopic;
				if (z >= 0) {
					postID[z][nTopicPosts[z]] = users[u].posts[t].postID;
					postPerplexity[z][nTopicPosts[z]] = users[u].posts[t].inferedLikelihood
							/ users[u].posts[t].words.length;
					nTopicPosts[z]++;
				} else {
					backgroundPostID[nBackgroundTopicPosts] = users[u].posts[t].postID;
					backgroundPostPerplexity[nBackgroundTopicPosts] = users[u].posts[t].inferedLikelihood;
					nBackgroundTopicPosts++;
				}

			}
		}

		try {
			String fileName = outputPath + "/topicTopPosts.csv";
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			RankingTool rankTool = new RankingTool();
			WeightedElement[] topPosts = null;
			for (int z = 0; z < nTopics; z++) {
				bw.write(z + "\n");
				topPosts = rankTool.getTopKbyWeight(postID[z], postPerplexity[z], Math.min(k, nTopicPosts[z]));
				for (int j = 0; j < Math.min(k, nTopicPosts[z]); j++)
					bw.write("," + topPosts[j].name + "," + topPosts[j].weight + "\n");
			}
			if (nBackgroundTopicPosts > 0) {
				bw.write("background\n");
				topPosts = rankTool.getTopKbyWeight(backgroundPostID, backgroundPostPerplexity,
						Math.min(k, nBackgroundTopicPosts));
				for (int j = 0; j < Math.min(k, nBackgroundTopicPosts); j++)
					bw.write("," + topPosts[j].name + "," + topPosts[j].weight + "\n");
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("Error in writing out topic top posts to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void outputUserTopicDistribution() {
		try {
			String fileName = outputPath + "/userTopicDistributions.csv";
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			for (int u = 0; u < users.length; u++) {
				bw.write("" + users[u].userID);
				for (int z = 0; z < nTopics; z++)
					bw.write("," + users[u].topicDistribution[z]);
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("Error in writing out user topic distributions to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void outputLikelihoodPerplexity() {
		try {
			String fileName = outputPath + "/likelihood-perplexity.csv";
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			bw.write("postLogLikelihood,postLogPerplexity\n");
			bw.write("" + postLogLikelidhood + "," + postLogPerplexity);
			bw.close();
		} catch (Exception e) {
			System.out.println("Error in writing out likelihood and perplexity to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void outputInferedTopic() {
		try {
			SystemTool.createFolder(outputPath, "inferedTopics");
			for (int u = 0; u < users.length; u++) {
				String filename = outputPath + SystemTool.pathSeparator + "inferedTopics" + SystemTool.pathSeparator
						+ users[u].userID + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
				for (int j = 0; j < users[u].posts.length; j++)
					bw.write(users[u].posts[j].postID + "\t" + users[u].posts[j].inferedTopic + "\n");
				bw.close();
			}
		} catch (Exception e) {
			System.out.println("Error in writing out post topics to file!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void output() {
		outputPostTopicWordDistributions();
		outputPostTopicTopWords(20);
		outputCoinBias();
		outputUserTopicDistribution();

		if (toOutputTopicTopPosts || toOutputInferedTopics) {
			inferPostTopic();
			if (toOutputTopicTopPosts) {
				outputPostTopicTopPosts(100);
			}
			if (toOutputInferedTopics) {
				outputInferedTopic();
			}
		}
		if (toOutputLikelihoodPerplexity) {
			getLikelihoodPerplexity();
			outputLikelihoodPerplexity();
		}
	}
}
