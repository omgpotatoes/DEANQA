package cs2731;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import libsvm.*;
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

	/** Parameter object for the SVM model parameters */
	private svm_parameter param;
	
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
			SVMData data = extractData(trainingQuestionFiles, trainingAnswersFile);
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
		
		TrainingFileData [] trainingData = Utils.extractAllDataFromFile(documents, answerKey);
		
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    Annotation sentenceAnno;
	    Annotation questionAnno;
	    int rawWordMatch = 0;
	    int maxWordMatch = 0;
	    double [][] X;
	    ArrayList<double []> tempX = new ArrayList<double []>();
	    ArrayList<double []> unDiffed = new ArrayList<double []>();
	    double [] tempDataPoint;

	    /*
    	System.out.println(trainingData[0].document.substring(0, 10));
    	questionAnno = new Annotation(trainingData[0].questions);
    	sentenceAnno = new Annotation(trainingData[0].document);
    	pipeline.annotate(questionAnno);
    	pipeline.annotate(sentenceAnno);
    	int flag = 0;
    	
    	for(CoreMap question: questionAnno.get(SentencesAnnotation.class)) {
    		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
    			rawWordMatch = 0;
    			for (CoreLabel questionToken: question.get(TokensAnnotation.class)) {
    				String questionLemma = questionToken.get(LemmaAnnotation.class);
    				for (CoreLabel sentenceToken: sentence.get(TokensAnnotation.class)) {
    					String sentenceLemma = sentenceToken.get(LemmaAnnotation.class);
    					System.out.print(sentenceLemma + " ");
    					if(questionLemma.equals(sentenceLemma))
    						rawWordMatch++;
    				}
    				System.out.println("");
    			}
				if(rawWordMatch > maxWordMatch)
					maxWordMatch = rawWordMatch;
				tempDataPoint = new double [1];
				tempDataPoint[0] = rawWordMatch;
				System.out.println(rawWordMatch);
				if(flag == 0)
					tempX.add(tempDataPoint);
	    	}
    		flag++;
    	}
    	
	    X = new double[tempX.size()][1];
		tempX.toArray(X);
		
		for(int i = 0; i < X.length; i++) {
			for(int j = 0; j < X[0].length; j++) {
				System.out.print(X[i][j] + " ");
			}
			System.out.println("");
		}
		*/
	   
	    for(TrainingFileData dataFile : trainingData) {
	    	questionAnno = new Annotation(dataFile.questions);
	    	sentenceAnno = new Annotation(dataFile.document);
	    	pipeline.annotate(questionAnno);
	    	pipeline.annotate(sentenceAnno);
	    	
	    	//Possibly strip the sentences of punctuation????
	    	for(CoreMap question: questionAnno.get(SentencesAnnotation.class)) {
	    		maxWordMatch = 0;
	    		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
	    			rawWordMatch = 0;
	    			for (CoreLabel questionToken: question.get(TokensAnnotation.class)) {
	    				String questionLemma = questionToken.get(LemmaAnnotation.class).toLowerCase();
	    				for (CoreLabel sentenceToken: sentence.get(TokensAnnotation.class)) {
	    					String sentenceLemma = sentenceToken.get(LemmaAnnotation.class).toLowerCase();
	    					if(questionLemma.equals(sentenceLemma))
	    						rawWordMatch++;
	    				}
	    			}
	    			if(rawWordMatch > maxWordMatch)
    					maxWordMatch = rawWordMatch;
    				tempDataPoint = new double [1];
    				tempDataPoint[0] = rawWordMatch;
    				unDiffed.add(tempDataPoint);
		    	}
	    		int size = unDiffed.size();
	    		for(int q = 0; q < size; q++) {
	    			tempDataPoint = new double[1];
	    			tempDataPoint[0] = maxWordMatch - unDiffed.get(0)[0];
	    			tempX.add(tempDataPoint);
	    			unDiffed.remove(0);
	    		}
	    	}
	    }

	    X = new double[tempX.size()][1];
		tempX.toArray(X);
		for(int i = 0; i < X.length; i++) {
			for(int j = 0; j < X[0].length; j++) {
				System.out.print(X[i][j] + " ");
			}
			System.out.println("");
		}
	    /*
	    //This code makes me want to cry vomit from my mouth
		ArrayList<ArrayList<Answer>> trainingAnswers = Utils.extractAnswers(answerKey);
		ArrayList<ArrayList<Sentence>> documentSentences = Utils.extractSentences(documents);
		ArrayList<ArrayList<Question>> trainingQuestions = Utils.extractQuestions(documents); 
		
		for(ArrayList<Question> questionSet : trainingQuestions) {
			for(ArrayList<Sentence> document: documentSentences) {
				for(Question question: questionSet) {
					questionAnno = new Annotation(question.questionText);
					pipeline.annotate(questionAnno);
					questionCore = questionAnno.get(SentencesAnnotation.class).get(0);
					for(Sentence sentence : document) {
						//System.out.println(sentence);
						sentenceAnno = new Annotation(sentence.sentenceText);
						pipeline.annotate(sentenceAnno);
					    sentenceCore = sentenceAnno.get(SentencesAnnotation.class).get(0);
					    for (CoreLabel questionToken: questionCore.get(TokensAnnotation.class)) {
					    	String questionPos = questionToken.get(PartOfSpeechAnnotation.class);
				    		String questionLemma = questionToken.get(LemmaAnnotation.class);
				    		String questionNer = questionToken.get(NamedEntityTagAnnotation.class);
					    	for (CoreLabel sentenceToken: sentenceCore.get(TokensAnnotation.class)) {
					    		//String word = token.get(TextAnnotation.class);
					    		String sentencePos = sentenceToken.get(PartOfSpeechAnnotation.class);
					    		String sentenceLemma = sentenceToken.get(LemmaAnnotation.class);
					    		String sentenceNer = sentenceToken.get(NamedEntityTagAnnotation.class);
					    	}
					    }
					}
				}
			}
		}
		*/
		return null;
	}

	/**
	 * First I'll focus on extracting the data then I'll work on this
	 * @return
	 */
	private svm_model trainModel() {
		/*
		 * SVM for classification using the "Nu" model with a Radial Basis Kernel.  These
		 * all other parameters are set to the default (for now)
		 */
		param = new svm_parameter();
		param.svm_type = svm_parameter.NU_SVC;
		param.kernel_type = svm_parameter.RBF;
		//return null for now
		return null;
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


