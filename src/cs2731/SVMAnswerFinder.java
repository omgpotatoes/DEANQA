package cs2731;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;

/**
 * Implements a Support Vector Machine model with various features from literature to predict
 * answers
 * @author Eric Heim
 */
public class SVMAnswerFinder implements AnswerFinder {

	private StanfordCoreNLP pipeline;
	private Instances data;
	private J48 model = new J48();
	private static boolean flag = true;
	private final int classFeature = 8;
	
    /**
	 * Constructor that takes the training data folder path and the answer file path as 
	 * arguments
     * @throws FileNotFoundException 
	 */
	public SVMAnswerFinder(String trainingQuestionsFolder, String trainingAnswersFilePath) throws FileNotFoundException{
		/*
		 * Open the training document folder, get all the file names in there, and send them 
		 * and the name of the answer training data answer file to the method that extracts
		 * features from the data
		 */
		File trainingAnswersFile = new File(trainingAnswersFilePath);
		File[] trainingQuestionFiles = new File(trainingQuestionsFolder).listFiles();
		String [] options = new String [3];
		options[0] = "-U";
		options[1] = "-M";
		options[2] = "1";
		try {
			model.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error in the options for the model");
		}
		
		if (trainingQuestionFiles == null) {
		    System.err.println("This folder does not exisit or is not a directory.");
		} else {
			//Creates the annotation pipeline
			Properties props = new Properties();
		    props.put("annotators", "tokenize, ssplit, pos, lemma");
		    pipeline = new StanfordCoreNLP(props);
			
			data = extractTrainingData(trainingQuestionFiles, trainingAnswersFile);
		}
		/*
		for(int i = 0; i < data.numInstances(); i++) {
			System.out.println(data.instance(i));
		}
		*/
		
		try {
			model.buildClassifier(data);
		} catch (Exception e) {
			System.err.println("Error in training the model");
			e.printStackTrace();
		}
		
	}

	/**
	 * Returns a representation of the training documents that the SVM can digest.  Each data 
	 * point is a sentence-question pair with each feature representing some relationship 
	 * between the two.
	 * @param documents The names of the files for training (include sentences 
	 * and questions)
	 * @param answerKey The answer sentences for the questions in the training
	 * @return 
	 * @throws FileNotFoundException 
	 */
	private Instances extractTrainingData(File[] documents, File answerKey) throws FileNotFoundException {
		
		//WEKA Stuff
		Attribute [] attributeNames = new Attribute [classFeature + 1];
		attributeNames[0] = new Attribute("DMWM");
		attributeNames[1] = new Attribute("DMVM");
		attributeNames[2] = new Attribute("DMWM-Prev");
		attributeNames[3] = new Attribute("DMVM-Prev");
		attributeNames[4] = new Attribute("DMWM-Next");
		attributeNames[5] = new Attribute("DMVM-Next");
		FastVector labels = new FastVector();
		labels.addElement("Yes");
		labels.addElement("No");
		attributeNames[6] = new Attribute("IsTitle", labels);
		attributeNames[7] = new Attribute("IsDateline", labels);
		attributeNames[classFeature] = new Attribute("Labels", labels);
		FastVector attributes = new FastVector();
		
		for(Attribute attributeName : attributeNames) 
			attributes.addElement(attributeName);

		Instances dataset = new Instances("testDataset", attributes, 0);
	
		//Extracting the data
		TrainingFileData [] trainingData = Utils.extractAllDataFromFile(documents, answerKey);
	    Annotation sentenceAnno;
	    Annotation questionAnno;
	    int rawWordMatch = 0;
	    int maxWordMatch = 0;
	    int rawVerbMatch = 0;
	    int maxVerbMatch = 0;
	    int questionCounter = 0;
	    int sentenceCounter = 0;
	    double [][] X;
	    double [] Y;
	    ArrayList<Double> tempY = new ArrayList<Double>();
	    ArrayList<double []> tempX = new ArrayList<double []>();
	    ArrayList<double []> unDiffed = new ArrayList<double []>();
	    ArrayList<boolean []> isSpecial = new ArrayList<boolean[]>(); 
	    double [] tempDataPoint;
	
	    for(TrainingFileData dataFile : trainingData) {
	    	questionAnno = new Annotation(dataFile.questions);
	    	sentenceAnno = new Annotation(dataFile.document);
	    	pipeline.annotate(questionAnno);
	    	pipeline.annotate(sentenceAnno);
	    	questionCounter = 0;
	    	//Possibly strip the sentences of punctuation????
	    	//Did it!
	    	for(CoreMap question: questionAnno.get(SentencesAnnotation.class)) {
	    		maxWordMatch = 0;
	    		maxVerbMatch = 0;
	    		sentenceCounter = 0;
	    		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
	    			String [] multipleAnswers = dataFile.answerMap[questionCounter].split("-OR-");
    				double label = dataset.attribute(classFeature).indexOfValue("No");
    				for(String anAnswer : multipleAnswers) {
    					if(anAnswer.trim().equals(sentence.toString().trim()))
		    				label = dataset.attribute(classFeature).indexOfValue("Yes");
    				}
    				tempY.add(label);
    				
	    			rawWordMatch = 0;
	    			rawVerbMatch = 0;
	    			
	    			for (CoreLabel questionToken: question.get(TokensAnnotation.class)) {
	    				String questionLemma = questionToken.get(LemmaAnnotation.class).toLowerCase();
	    				String questionPOS = questionToken.get(PartOfSpeechAnnotation.class);
	    				for (CoreLabel sentenceToken: sentence.get(TokensAnnotation.class)) {
	    					String sentenceLemma = sentenceToken.get(LemmaAnnotation.class).toLowerCase();
	    					String sentencePOS = sentenceToken.get(PartOfSpeechAnnotation.class);
	    					if(questionLemma.equals(sentenceLemma)) {
	    						rawWordMatch++;
	    						if(questionPOS.startsWith("VB") && sentencePOS.startsWith("VB") && !questionLemma.equals("be") && !questionLemma.equals("do") && !questionLemma.equals("have")) 
	    							rawVerbMatch++;
	    					}		
	    				}
	    			}
	    			if(rawWordMatch > maxWordMatch)
    					maxWordMatch = rawWordMatch;
	    			if(rawVerbMatch > maxVerbMatch)
    					maxVerbMatch = rawVerbMatch;
    				tempDataPoint = new double [4];
    				tempDataPoint[0] = rawWordMatch;
    				tempDataPoint[1] = rawVerbMatch;
    				unDiffed.add(tempDataPoint);
    				
    				boolean [] tempIsSpecial = new boolean [2];
    				if(sentenceCounter == 0) {
						tempIsSpecial[0] = true;
						tempIsSpecial[1] = false;
    				}
    				else if(sentenceCounter == 1) {
    					tempIsSpecial[0] = false;
    					tempIsSpecial[1] = true;
    				}
    				else {
    					tempIsSpecial[0] = false;
    					tempIsSpecial[1] = false;
    				}
    				isSpecial.add(tempIsSpecial);
    				sentenceCounter++;
		    	}
	    		questionCounter++;
	    		int size = unDiffed.size();
	    		for(int q = 0; q < size; q++) {
	    			tempDataPoint = new double[2];
	    			tempDataPoint[0] = maxWordMatch - unDiffed.get(0)[0];
	    			if(maxVerbMatch == 0)
	    				tempDataPoint[1] = 200;
	    			else
	    				tempDataPoint[1] = maxVerbMatch - unDiffed.get(0)[1];
	    			tempX.add(tempDataPoint);
	    			unDiffed.remove(0);
	    		}
	    	}
	    }

	    X = new double[tempX.size()][2];
		tempX.toArray(X);
		Y = new double[tempY.size()];
		for(int q=0; q < Y.length; q++) 
			Y[q] = tempY.get(q);

		//WEKA Stuff
		double [][] instances = new double [X.length][classFeature + 1];
		
		//*Its actually a little more complicated than this...Need to find the dateline title, then figure this out
		for(int i = 0; i < instances.length; i ++) {
			instances[i][0] = X[i][0];
			instances[i][1] = X[i][1];
			instances[i][classFeature] = Y[i];
			
			//Its a title
			if(isSpecial.get(i)[0]) {
				instances[i][6] = dataset.attribute(6).indexOfValue("Yes");
				instances[i][2] = 0;
				instances[i][3] = 0;
				instances[i][4] = 0;
				instances[i][5] = 0;
			}
			else
				instances[i][6] = dataset.attribute(6).indexOfValue("No");
			
			//Its a Dateline
			if(isSpecial.get(i)[1]) {
				instances[i][7] = dataset.attribute(7).indexOfValue("Yes");
				instances[i][2] = 0;
				instances[i][3] = 0;
				instances[i][4] = 0;
				instances[i][5] = 0;
			}
			else
				instances[i][7] = dataset.attribute(7).indexOfValue("No");
			

			//If its neither
			if(!isSpecial.get(i)[0] && !isSpecial.get(i)[1]) {
				//Its the last line of the article
				if(i == instances.length-1 || isSpecial.get(i+1)[0]) {
					instances[i][2] = X[i-1][0];
					instances[i][3] = X[i-1][1];
					instances[i][4] = 0;
					instances[i][5] = 0;
				}
				//Its the first line of the article
				else if(i != 0 && isSpecial.get(i-1)[1]) {
					instances[i][2] = 0;
					instances[i][3] = 0;
					instances[i][4] = X[i+1][0];
					instances[i][5] = X[i+1][1];
				}
				//Its some line in the middle of the article
				else {
					instances[i][2] = X[i-1][0];
					instances[i][3] = X[i-1][1];
					instances[i][4] = X[i+1][0];
					instances[i][5] = X[i+1][1];
				}
			}
		}		
		
		for(double [] instance : instances)
			dataset.add(new Instance(1.0, instance));
		
		dataset.setClassIndex(classFeature);
	
		return dataset;
	}
	
	/** 
	 * First I'll focus on training the SVM then I'll work on this
	 * @return
	 */
	public List<Guess> getAnswerLines(List<String> document, String question) {	    
		Instances testData = extractTestData(document, question);
		List<Guess> guessList = new ArrayList<Guess>();
		double currentMax = 0.0;
		//Possibly count the blank lines, because the data that is returned includes those blanks
		for(int i = 0; i < testData.numInstances(); i++) {
			try {
				double[] tempGuesses = model.distributionForInstance(testData.instance(i));
				if(tempGuesses[0] == currentMax) {
					 guessList.add(new Guess(1.0, i+1));
				}
				else if(tempGuesses[0] > currentMax) {
					currentMax = tempGuesses[0];
					guessList.clear();
					guessList.add(new Guess(1.0, i+1));
				}
			} catch (Exception e) {
				System.err.println("Error in classifying an instance");
				e.printStackTrace();
			}
		}
		
		double prob = 1.0/guessList.size();
		
		for(Guess aGuess : guessList) { 
			aGuess.setProb(prob);
			//System.out.println(aGuess);
		}
			
		return guessList;
	}
	
	private Instances extractTestData(List<String> document, String question) {
		String sentences = "";
		
		for(String line: document) 
			sentences += line.trim().replaceAll("[.,!?]", "") + "?" + "\n";
		
		question = question.replaceAll("[.,!?]", "") + "?";
		int maxWordMatch = 0;
		int maxVerbMatch = 0;
		ArrayList<double []> tempX = new ArrayList<double []>();
	    ArrayList<double []> unDiffed = new ArrayList<double []>();
	    ArrayList<boolean []> isSpecial = new ArrayList<boolean[]>(); 
	    double [] tempDataPoint;
	    int sentenceCounter = 0;
		
	    Annotation sentenceAnno = new Annotation(sentences);
	    Annotation questionAnno = new Annotation(question);
	    pipeline.annotate(questionAnno);
    	pipeline.annotate(sentenceAnno);
	    
	    //WEKA Stuff
    	Attribute [] attributeNames = new Attribute [classFeature + 1];
		attributeNames[0] = new Attribute("DMWM");
		attributeNames[1] = new Attribute("DMVM");
		attributeNames[2] = new Attribute("DMWM-Prev");
		attributeNames[3] = new Attribute("DMVM-Prev");
		attributeNames[4] = new Attribute("DMWM-Next");
		attributeNames[5] = new Attribute("DMVM-Next");
		FastVector labels = new FastVector();
		labels.addElement("Yes");
		labels.addElement("No");
		attributeNames[6] = new Attribute("IsTitle", labels);
		attributeNames[7] = new Attribute("IsDateline", labels);
		attributeNames[classFeature] = new Attribute("Labels", labels);
		FastVector attributes = new FastVector();
		
		for(Attribute attributeName : attributeNames) 
			attributes.addElement(attributeName);

		Instances dataset = new Instances("testDataset", attributes, 0);
	    
		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
			int rawWordMatch = 0;
			int rawVerbMatch = 0;
			
			for (CoreLabel questionToken: questionAnno.get(SentencesAnnotation.class).get(0).get(TokensAnnotation.class)) {
				String questionLemma = questionToken.get(LemmaAnnotation.class).toLowerCase();
				String questionPOS = questionToken.get(PartOfSpeechAnnotation.class);
				for (CoreLabel sentenceToken: sentence.get(TokensAnnotation.class)) {
					String sentenceLemma = sentenceToken.get(LemmaAnnotation.class).toLowerCase();
					String sentencePOS = sentenceToken.get(PartOfSpeechAnnotation.class);
					if(questionLemma.equals(sentenceLemma)) {
						rawWordMatch++;
						if(questionPOS.startsWith("VB") && sentencePOS.startsWith("VB") && !questionLemma.equals("be") && !questionLemma.equals("do") && !questionLemma.equals("have")) 
							rawVerbMatch++;
					}	
				}
			}
			if(rawWordMatch > maxWordMatch)
				maxWordMatch = rawWordMatch;
			if(rawVerbMatch > maxVerbMatch)
				maxVerbMatch = rawVerbMatch;
			tempDataPoint = new double [2];
			tempDataPoint[0] = rawWordMatch;
			tempDataPoint[1] = rawVerbMatch;
			unDiffed.add(tempDataPoint);
			
			boolean [] tempIsSpecial = new boolean [2];
			if(sentenceCounter == 0) {
				tempIsSpecial[0] = true;
				tempIsSpecial[1] = false;
			}
			else if(sentenceCounter == 1) {
				tempIsSpecial[0] = false;
				tempIsSpecial[1] = true;
			}
			else {
				tempIsSpecial[0] = false;
				tempIsSpecial[1] = false;
			}
			isSpecial.add(tempIsSpecial);
			sentenceCounter++;
		}
		int size = unDiffed.size();
		for(int q = 0; q < size; q++) {
			tempDataPoint = new double[2];
			tempDataPoint[0] = maxWordMatch - unDiffed.get(0)[0];
			if(maxVerbMatch == 0)
				tempDataPoint[1] = 200;
			else
				tempDataPoint[1] = maxVerbMatch - unDiffed.get(0)[1];
			tempX.add(tempDataPoint);
			unDiffed.remove(0);
		}
		
		double [][] X = new double[tempX.size()][2];
		tempX.toArray(X);
		double [] Y = new double[tempX.size()];
		for(int q=0; q < Y.length; q++) 
			Y[q] = dataset.attribute(classFeature).indexOfValue("No");

		//WEKA Stuff
		double [][] instances = new double [X.length][classFeature + 1];
		
		for(int i = 0; i < instances.length; i ++) {
			instances[i][0] = X[i][0];
			instances[i][1] = X[i][1];
			instances[i][classFeature] = Y[i];
			
			if(isSpecial.get(i)[0])
				instances[i][6] = dataset.attribute(6).indexOfValue("Yes");
			else
				instances[i][6] = dataset.attribute(6).indexOfValue("No");
			
			if(isSpecial.get(i)[1])
				instances[i][7] = dataset.attribute(7).indexOfValue("Yes");
			else
				instances[i][7] = dataset.attribute(7).indexOfValue("No");
			
			if(i == 0 || i == 1) {
				instances[i][2] = 0;
				instances[i][3] = 0;
				instances[i][4] = 0;
				instances[i][5] = 0;
			}
			else if(i == instances.length-1) {
				instances[i][2] = X[i-1][0];
				instances[i][3] = X[i-1][1];
				instances[i][4] = 0;
				instances[i][5] = 0;
			}
			else if(i == 2) {
				instances[i][2] = 0;
				instances[i][3] = 0;
				instances[i][4] = X[i+1][0];
				instances[i][5] = X[i+1][1];
			}
			else {
				instances[i][2] = X[i-1][0];
				instances[i][3] = X[i-1][1];
				instances[i][4] = X[i+1][0];
				instances[i][5] = X[i+1][1];
			}
		}		
		
		
		for(double [] instance : instances)
			dataset.add(new Instance(1.0, instance));
		
		dataset.setClassIndex(classFeature);
		
		return dataset;
	}
}


