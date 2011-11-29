package cs2731;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
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

	private SVMData data;
	
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
		if (trainingQuestionFiles == null) {
		    System.err.println("This folder does not exisit or is not a directory.");
		} else {
			data = extractData(trainingQuestionFiles, trainingAnswersFile);
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
	private SVMData extractData(File[] documents, File answerKey) throws FileNotFoundException {
		
		//WEKA STARTS HERE
		
		TrainingFileData [] trainingData = Utils.extractAllDataFromFile(documents, answerKey);
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    Annotation sentenceAnno;
	    Annotation questionAnno;
	    int rawWordMatch = 0;
	    int maxWordMatch = 0;
	    int rawVerbMatch = 0;
	    int maxVerbMatch = 0;
	    int questionCounter = 0;
	    double [][] X;
	    double [] Y;
	    ArrayList<Double> tempY = new ArrayList<Double>();
	    ArrayList<double []> tempX = new ArrayList<double []>();
	    ArrayList<double []> unDiffed = new ArrayList<double []>();
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
	    		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
	    			String [] multipleAnswers = dataFile.answerMap[questionCounter].split("-OR-");
    				double label = -1.0;
    				for(String anAnswer : multipleAnswers) {
    					if(anAnswer.trim().equals(sentence.toString().trim()))
		    				label = 1.0;
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
    				tempDataPoint = new double [2];
    				tempDataPoint[0] = rawWordMatch;
    				tempDataPoint[1] = rawVerbMatch;
    				unDiffed.add(tempDataPoint);
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

	    X = new double[tempX.size()][1];
		tempX.toArray(X);
		Y = new double[tempY.size()];
		for(int q=0; q < Y.length; q++) 
			Y[q] = tempY.get(q);
		
		for(int i = 0; i < X.length; i++) {
			for(int j = 0; j < X[0].length; j++) {
				//System.out.print(X[i][j] + " ");
			}
			//System.out.println(" | " + Y[i]);
		}

		return new SVMData(X, Y);
	}


	/** 
	 * First I'll focus on training the SVM then I'll work on this
	 * @return
	 */
	public List<Guess> getAnswerLines(List<String> document, String question) {	    
		return null;
	}
	
	/**
	 * Feature and label representation of the data for the SVM to digest
	 * @author Eric Heim
	 */
	private class SVMData {
		public double [][] X;
		public double [] Y;
		public SVMData(double [][] inX, double [] inY) {
			this.X = inX;
			this.Y = inY;
		}
		
	}

}


