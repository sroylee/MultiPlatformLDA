package Model;

import java.util.Random;

import hoang.larc.tooler.SystemTool;

/* Copyright (c) 2016 Roy Ka-Wei LEE and Tuan-Anh HOANG
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 */

public class ModelRunner {
	
	public static void main(String args[]) {
		 //runTwitterLDA();
		runMultiPlatformLDA();
	}
	
	public static void runMultiPlatformLDA() {

		String dataPath = "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data";
		//String dataPath = "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/synthetic";
		int nTopics = 5;
		int nPlatforms = 3;
		int modelType = ModelType.USER_SPECIFIC;
		//int modelType = ModelType.GLOBAL;
		String outputPath = "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/output";
		//String outputPath = "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/synthetic/output";
		
		int burningPeriod = 10;
		int maxIteration = 50;
		int samplingGap = 20;
		int testBatch = 0;

		MultiPlatformLDA model = new MultiPlatformLDA();
		model.dataPath = dataPath;
		System.out.print("Reading data ... ");
		model.readData();
		System.out.println("DONE!");

		SystemTool.createFolder(outputPath, "" + nTopics);
		model.outputPath = outputPath + SystemTool.pathSeparator + nTopics;

		model.nTopics = nTopics;
		model.nPlatforms = nPlatforms;
		model.modelType = modelType;
		model.burningPeriod = burningPeriod;
		model.maxIteration = maxIteration;
		model.samplingGap = samplingGap;
		model.testBatch = testBatch;
		model.toOutputLikelihoodPerplexity = true;
		model.toOutputInferedTopics = true;
		model.toOutputTopicTopPosts = true;
		model.toOutputInferedPlatforms = true;

		model.rand = new Random();
		model.learnModel();
		model.outputAll();

	}

	public static void runTwitterLDA() {

		String dataPath = "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data";
		int platform = 1;
		int nTopics = 10;
		String outputPath = "F:/Users/roylee/MultiPlatformLDAv1/MultiPlatformLDA/data/output/TwitterLDA";

		int burningPeriod = 2;
		int maxIteration = 5;
		int samplingGap = 1;
		int testBatch = 0;

		TwitterLDA model = new TwitterLDA();
		model.dataPath = dataPath;
		model.platform = platform;
		System.out.print("Reading data ... ");
		model.readData();
		System.out.println("DONE!");

		SystemTool.createFolder(outputPath, "" + nTopics);
		model.outputPath = outputPath + SystemTool.pathSeparator + nTopics;

		model.nTopics = nTopics;
		model.burningPeriod = burningPeriod;
		model.maxIteration = maxIteration;
		model.samplingGap = samplingGap;
		model.testBatch = testBatch;
		model.toOutputLikelihoodPerplexity = true;
		model.toOutputInferedTopics = true;
		model.toOutputTopicTopPosts = true;
		model.toOutputInferedPlatforms = true;

		model.rand = new Random();
		model.learnModel();
		model.output();

	}
	
	
}
