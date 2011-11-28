package cs2731;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import weka.classifiers.functions.LibSVM;
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
	private LibSVM model = new LibSVM();
	private static boolean flag = true;
	
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
		String [] options = new String [4];
		options[0] = "-S";
		options[1] = "0";
		options[2] = "-K";
		options[3] = "2";
		try {
			model.setOptions(options);
		} catch (Exception e) {
			System.err.println("Error in the options for the SVM model");
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
		Attribute [] attributeNames = new Attribute [3];
		attributeNames[0] = new Attribute("DMWM");
		attributeNames[1] = new Attribute("DMVM");
		FastVector labels = new FastVector();
		labels.addElement("Yes");
		labels.addElement("No");
		attributeNames[2] = new Attribute("Labels", labels);
		FastVector attributes = new FastVector();
		attributes.addElement(attributeNames[0]);
		attributes.addElement(attributeNames[1]);
		attributes.addElement(attributeNames[2]);
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
    				double label = dataset.attribute(2).indexOfValue("No");
    				for(String anAnswer : multipleAnswers) {
    					if(anAnswer.trim().equals(sentence.toString().trim()))
		    				label = dataset.attribute(2).indexOfValue("Yes");
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

		//WEKA Stuff
		double [][] instances = new double [X.length][3];
		
		for(int i = 0; i < instances.length; i ++) {
			instances[i][0] = X[i][0];
			instances[i][1] = X[i][1];
			instances[i][2] = Y[i];
		}		
		
		
		for(double [] instance : instances)
			dataset.add(new Instance(1.0, instance));
		
		dataset.setClassIndex(2);
		return dataset;
	}
	
	/** 
	 * First I'll focus on training the SVM then I'll work on this
	 * @return
	 */
	public List<Guess> getAnswerLines(List<String> document, String question) {	    
		Instances testData = extractTestData(document, question);
		//Possibly count the blank lines, because the data that is returned includes those blanks
		//DEBUGGING CONSIDER CHANGING TO PROBABLEISTIC MODEL
		if(flag) {
			for(int i = 0; i < testData.numInstances(); i++) {
				try {
					double[] tempGuesses = model.distributionForInstance(testData.instance(i));
					for(double guess : tempGuesses) 
						System.out.print(guess + " ");
					System.out.println("");
				} catch (Exception e) {
					System.err.println("Error in classifying an instance");
					e.printStackTrace();
				}
			}
				
			flag = false;
		}
		//
		return null;
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
	    double [] tempDataPoint;
		
	    Annotation sentenceAnno = new Annotation(sentences);
	    Annotation questionAnno = new Annotation(question);
	    pipeline.annotate(questionAnno);
    	pipeline.annotate(sentenceAnno);
	    
	    //WEKA Stuff
	    Attribute [] attributeNames = new Attribute [3];
		attributeNames[0] = new Attribute("DMWM");
		attributeNames[1] = new Attribute("DMVM");
		FastVector labels = new FastVector();
		labels.addElement("Yes");
		labels.addElement("No");
		attributeNames[2] = new Attribute("Labels", labels);
		FastVector attributes = new FastVector();
		attributes.addElement(attributeNames[0]);
		attributes.addElement(attributeNames[1]);
		attributes.addElement(attributeNames[2]);
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
			tempDataPoint = new double [3];
			tempDataPoint[0] = rawWordMatch;
			tempDataPoint[1] = rawVerbMatch;
			tempDataPoint[2] = dataset.attribute(2).indexOfValue("No");
			unDiffed.add(tempDataPoint);
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
		
		for(double [] instance : tempX)
			dataset.add(new Instance(1.0, instance));
		
		//STUFF TO MAKE SURE ONE CAN BE CHOSEN AS YES
		double [] tempInstance = {0, 0, dataset.attribute(2).indexOfValue("No")};
		dataset.add(new Instance(1.0, tempInstance));
		//
		
		dataset.setClassIndex(2);
		return dataset;
	}
}


