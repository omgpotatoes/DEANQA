package cs2731;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
	//CHANGE
	private LibSVM model = new LibSVM();
	private static final int CLASS_FEATURE = 24;
	private boolean trainedFlag = false;
	
	public SVMAnswerFinder(){
		//Creates the annotation pipeline
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public void trainModel(String trainingQuestionsFolder, String trainingAnswersFilePath, String objectFilePath1) throws FileNotFoundException {
		File trainingAnswersFile = new File(trainingAnswersFilePath);
		File[] trainingQuestionFiles = new File(trainingQuestionsFolder).listFiles();
		
		//CHANGE
		String [] opts = new String[4];
		opts[0] = "-S";
		opts[1] = "0";
		//default = 1
		opts[2] = "-C";
		opts[3] = "4.5";
		//opts[2] = "-N";
		//opts[3] = "1";
		//default = 1/k
		//opts[4] = "-G";
		//opts[5] = "0.035";
		
		try {
			model.setOptions(opts);
			model.setProbabilityEstimates(true);
		} catch (Exception e) {
			System.err.println("Error in the options for the model");
			e.printStackTrace();
		}
		
		if (trainingQuestionFiles == null) {
		    System.err.println("This folder does not exisit or is not a directory.");
		} else {
			data = extractTrainingData(trainingQuestionFiles, trainingAnswersFile);
		}
		
		try {
			model.buildClassifier(data);
		} catch (Exception e) {
			System.err.println("Error in training the model");
			e.printStackTrace();
		}
		//Save model to file
		try{
		      OutputStream file1 = new FileOutputStream(objectFilePath1);
		      OutputStream buffer1 = new BufferedOutputStream( file1 );
		      ObjectOutput output1 = new ObjectOutputStream( buffer1 );
		      try{
		        output1.writeObject(model);  
		      }
		      finally{
		    	trainedFlag = true;
		      }
		    }  
		    catch(IOException e){
		      System.err.println("Error in saving the model");
		      e.printStackTrace();
		    }
		
	}


	public void getModelFromFile(String filename1) { 
	    try{
	      InputStream file1 = new FileInputStream(filename1);
	      InputStream buffer1 = new BufferedInputStream( file1 );
	      ObjectInput input1 = new ObjectInputStream ( buffer1 );
	      try{
	    	  //CHANGE
	    	  model = (LibSVM)input1.readObject();
	      }
	      finally{
	    	trainedFlag = true;
	        input1.close();
	      }
	    }
	    catch(ClassNotFoundException e){
	      System.err.println("Does not recognize file contents as a model");
	      e.printStackTrace();
	    }
	    catch(IOException e){
	    	System.err.println("Error in opening the file");
		     e.printStackTrace();
	    }
	 }
	
	private Instances extractTrainingData(File[] documents, File answerKey) throws FileNotFoundException {

		//WEKA Stuff
		Attribute [] attributeNames = new Attribute [CLASS_FEATURE + 1];
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
		attributeNames[8] = new Attribute("SentenceContainsPerson", labels);
		attributeNames[9] = new Attribute("SentenceContainsOrganization", labels);
		attributeNames[10] = new Attribute("SentenceContainsLocation", labels);
		attributeNames[11] = new Attribute("SentenceContainsDate", labels);
		attributeNames[12] = new Attribute("SentenceContainsTime", labels);
		attributeNames[13] = new Attribute("isWho", labels);
		attributeNames[14] = new Attribute("isWhat", labels);
		attributeNames[15] = new Attribute("isWhere", labels);
		attributeNames[16] = new Attribute("isWhen", labels);
		attributeNames[17] = new Attribute("isWhy", labels);
		attributeNames[18] = new Attribute("isHow", labels);
		attributeNames[19] = new Attribute("hasName", labels);
		attributeNames[20] = new Attribute("hasCall", labels);
		attributeNames[21] = new Attribute("hasYear", labels);
		attributeNames[22] = new Attribute("hasIn", labels);
		attributeNames[23] = new Attribute("hasTo", labels);
		attributeNames[CLASS_FEATURE] = new Attribute("Labels", labels);
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
		ArrayList<boolean []> hasNERs = new ArrayList<boolean[]>();
		ArrayList<boolean []> questionTypes = new ArrayList<boolean[]>();
		ArrayList<boolean []> hasWords = new ArrayList<boolean[]>();
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

		//Question Types
		QuestionType type = QuestionTypeDetector.getQuestionType(question.toString());
		boolean [] tempQuestionTypes = {false, false, false, false, false, false};

		switch(type) {
		case WHO:
		tempQuestionTypes[0] = true;
		break;
		case WHAT:
		tempQuestionTypes[1] = true;
		break;
		case WHERE:
		tempQuestionTypes[2] = true;
		break;
		case WHEN:
		tempQuestionTypes[3] = true;
		break;
		case WHY:
		tempQuestionTypes[4] = true;
		break;
		case HOW:
		case HOW_MUCH:
		case HOW_MANY:
		case HOW_OLD:
		tempQuestionTypes[5] = true;
		break;
		}
		maxWordMatch = 0;
		maxVerbMatch = 0;
		sentenceCounter = 0;
		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
		String [] multipleAnswers = dataFile.answerMap[questionCounter].split("-OR-");
		     double label = dataset.attribute(CLASS_FEATURE).indexOfValue("No");
		     for(String anAnswer : multipleAnswers) {
		     if(anAnswer.trim().equals(sentence.toString().trim()))
		label = dataset.attribute(CLASS_FEATURE).indexOfValue("Yes");
		     }
		     tempY.add(label);
		    
		rawWordMatch = 0;
		rawVerbMatch = 0;

		//Booleans for if it sentence contains NER stuff
		boolean [] tempNERs = {false, false, false, false, false};

		boolean [] tempWords = {false, false, false, false, false};
		for (CoreLabel questionToken: question.get(TokensAnnotation.class)) {
		String questionLemma = questionToken.get(LemmaAnnotation.class).toLowerCase();
		String questionPOS = questionToken.get(PartOfSpeechAnnotation.class);
		for (CoreLabel sentenceToken: sentence.get(TokensAnnotation.class)) {
		String sentenceLemma = sentenceToken.get(LemmaAnnotation.class).toLowerCase();
		String sentencePOS = sentenceToken.get(PartOfSpeechAnnotation.class);
		String sentenceNER = sentenceToken.get(NamedEntityTagAnnotation.class);
		if(questionLemma.equals(sentenceLemma)) {
		rawWordMatch++;
		if(questionPOS.startsWith("VB") && sentencePOS.startsWith("VB") && !questionLemma.equals("be") && !questionLemma.equals("do") && !questionLemma.equals("have"))
		rawVerbMatch++;
		}
		//Check for NERS
		if(sentenceNER.equals("PERSON"))
		tempNERs[0] = true;
		else if(sentenceNER.equals("ORGANIZATION"))
		tempNERs[1] = true;
		else if(sentenceNER.equals("LOCATION"))
		tempNERs[2] = true;
		else if(sentenceNER.equals("DATE"))
		tempNERs[3] = true;
		else if(sentenceNER.equals("TIME"))
		tempNERs[4] = true;

		//Check for special words
		if(sentenceLemma.trim().equals("name"))
		tempWords[0] = true;
		if(sentenceLemma.trim().equals("call"))
		tempWords[1] = true;
		if(sentenceLemma.trim().equals("year"))
		tempWords[2] = true;
		if(sentenceLemma.trim().equals("in"))
		tempWords[3] = true;
		if(sentenceLemma.trim().equals("to"))
		tempWords[4] = true;
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
		    
		     //NER adding it to the list
		     hasNERs.add(tempNERs);
		    
		     //add question type to list
		     questionTypes.add(tempQuestionTypes);
		    
		     //add special words to list
		     hasWords.add(tempWords);
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
		double [][] instances = new double [X.length][CLASS_FEATURE + 1];

		//*Its actually a little more complicated than this...Need to find the dateline title, then figure this out
		for(int i = 0; i < instances.length; i ++) {
		instances[i][0] = X[i][0];
		instances[i][1] = X[i][1];
		instances[i][CLASS_FEATURE] = Y[i];

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

		//Person NER
		if(hasNERs.get(i)[0])
		instances[i][8] = dataset.attribute(8).indexOfValue("Yes");
		else
		instances[i][8] = dataset.attribute(8).indexOfValue("No");

		//Organization NER
		if(hasNERs.get(i)[1])
		instances[i][9] = dataset.attribute(9).indexOfValue("Yes");
		else
		instances[i][9] = dataset.attribute(9).indexOfValue("No");

		//Location NER
		if(hasNERs.get(i)[2])
		instances[i][10] = dataset.attribute(10).indexOfValue("Yes");
		else
		instances[i][10] = dataset.attribute(10).indexOfValue("No");

		//Date NER
		if(hasNERs.get(i)[3])
		instances[i][11] = dataset.attribute(11).indexOfValue("Yes");
		else
		instances[i][11] = dataset.attribute(11).indexOfValue("No");

		//Time NER
		if(hasNERs.get(i)[4])
		instances[i][12] = dataset.attribute(12).indexOfValue("Yes");
		else
		instances[i][12] = dataset.attribute(12).indexOfValue("No");

		//Who Question
		if(questionTypes.get(i)[0])
		instances[i][13] = dataset.attribute(13).indexOfValue("Yes");
		else
		instances[i][13] = dataset.attribute(13).indexOfValue("No");

		//What Question
		if(questionTypes.get(i)[1])
		instances[i][14] = dataset.attribute(14).indexOfValue("Yes");
		else
		instances[i][14] = dataset.attribute(14).indexOfValue("No");

		//Where Question
		if(questionTypes.get(i)[2])
		instances[i][15] = dataset.attribute(15).indexOfValue("Yes");
		else
		instances[i][15] = dataset.attribute(15).indexOfValue("No");

		//When Question
		if(questionTypes.get(i)[3])
		instances[i][16] = dataset.attribute(16).indexOfValue("Yes");
		else
		instances[i][16] = dataset.attribute(16).indexOfValue("No");

		//Why Question
		if(questionTypes.get(i)[4])
		instances[i][17] = dataset.attribute(17).indexOfValue("Yes");
		else
		instances[i][17] = dataset.attribute(17).indexOfValue("No");

		//How Question
		if(questionTypes.get(i)[5])
		instances[i][18] = dataset.attribute(17).indexOfValue("Yes");
		else
		instances[i][18] = dataset.attribute(17).indexOfValue("No");

		//name
		if(hasWords.get(i)[0])
		instances[i][19] = dataset.attribute(19).indexOfValue("Yes");
		else
		instances[i][19] = dataset.attribute(19).indexOfValue("No");

		//call
		if(hasWords.get(i)[1])
		instances[i][20] = dataset.attribute(20).indexOfValue("Yes");
		else
		instances[i][20] = dataset.attribute(20).indexOfValue("No");

		//year
		if(hasWords.get(i)[2])
		instances[i][21] = dataset.attribute(21).indexOfValue("Yes");
		else
		instances[i][21] = dataset.attribute(21).indexOfValue("No");

		//in
		if(hasWords.get(i)[3])
		instances[i][22] = dataset.attribute(22).indexOfValue("Yes");
		else
		instances[i][22] = dataset.attribute(22).indexOfValue("No");

		//to
		if(hasWords.get(i)[4])
		instances[i][23] = dataset.attribute(23).indexOfValue("Yes");
		else
		instances[i][23] = dataset.attribute(23).indexOfValue("No");
		}

		for(double [] instance : instances)
		dataset.add(new Instance(1.0, instance));

		dataset.setClassIndex(CLASS_FEATURE);

		//System.out.println(dataset);
		return dataset;
	}
	
	public List<Guess> getAnswerLines(List<String> document, String question) {	 
		if(trainedFlag) {
			Instances testData = extractTestData(document, question);
			//System.out.println(testData);
			//List<Guess> guessList1 = new ArrayList<Guess>();
			//List<Guess> guessList2 = new ArrayList<Guess>();
			List<Guess> guessList = new ArrayList<Guess>();
			
			/*
			double currentMax = 0.0;
			//Possibly count the blank lines, because the data that is returned includes those blanks
			for(int i = 0; i < testData.numInstances(); i++) {
				try {
					//CHANGE
					double[] tempGuesses = model.distributionForInstance(testData.instance(i));
					if(!document.get(i).trim().equals("")) {
						if(tempGuesses[0] == currentMax) {
							 guessList.add(new Guess(1.0, i+1));
						}
						else if(tempGuesses[0] > currentMax) {
							currentMax = tempGuesses[0];
							guessList.clear();
							guessList.add(new Guess(1.0, i+1));
						}
					}
				} catch (Exception e) {
					System.err.println("Error in classifying an instance");
					e.printStackTrace();
				}
			}
			*/
			double total = 0;
			for(int i = 0; i < testData.numInstances(); i++) {
				try {
					//CHANGE
					double[] tempGuesses = model.distributionForInstance(testData.instance(i));
					if(!document.get(i).trim().equals("")) {
						total += tempGuesses[0];
					    guessList.add(new Guess(tempGuesses[0], i+1));
					}
					else {
						guessList.add(new Guess(0, i+1));
					}
				} catch (Exception e) {
					System.err.println("Error in classifying an instance");
					e.printStackTrace();
				}
			}
			
			for(Guess guess : guessList)
				guess.setProb(guess.getProb()/total);
				
			/*
			for(Guess guess : guessList)
				System.out.println(guess);
			System.out.println("total = " + total);
			 */
			return guessList;
		}
		else {
			System.err.println("You have no model!");
			return null;
		}
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
	    ArrayList<boolean []> hasNERs = new ArrayList<boolean[]>(); 
	    ArrayList<boolean []> hasWords = new ArrayList<boolean[]>();
	    double [] tempDataPoint;
	    int sentenceCounter = 0;
		
	    Annotation sentenceAnno = new Annotation(sentences);
	    Annotation questionAnno = new Annotation(question);
	    pipeline.annotate(questionAnno);
    	pipeline.annotate(sentenceAnno);
	    
	    //WEKA Stuff
    	Attribute [] attributeNames = new Attribute [CLASS_FEATURE + 1];
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
		attributeNames[8] = new Attribute("SentenceContainsPerson", labels);
		attributeNames[9] = new Attribute("SentenceContainsOrganization", labels);
		attributeNames[10] = new Attribute("SentenceContainsLocation", labels);
		attributeNames[11] = new Attribute("SentenceContainsDate", labels);
		attributeNames[12] = new Attribute("SentenceContainsTime", labels);
		attributeNames[13] = new Attribute("isWho", labels);
		attributeNames[14] = new Attribute("isWhat", labels);
		attributeNames[15] = new Attribute("isWhere", labels);
		attributeNames[16] = new Attribute("isWhen", labels);
		attributeNames[17] = new Attribute("isWhy", labels);
		attributeNames[18] = new Attribute("isHow", labels);
		attributeNames[19] = new Attribute("hasName", labels);
		attributeNames[20] = new Attribute("hasCall", labels);
		attributeNames[21] = new Attribute("hasYear", labels);
		attributeNames[22] = new Attribute("hasIn", labels);
		attributeNames[23] = new Attribute("hasTo", labels);
		attributeNames[CLASS_FEATURE] = new Attribute("Labels", labels);
		FastVector attributes = new FastVector();
		
		for(Attribute attributeName : attributeNames) 
			attributes.addElement(attributeName);

		Instances dataset = new Instances("testDataset", attributes, 0);
		
		//Question Types
		QuestionType type = QuestionTypeDetector.getQuestionType(question.toString());
		boolean [] tempQuestionTypes = {false, false, false, false, false, false};
		
		switch(type) {
			case WHO:
				tempQuestionTypes[0] = true;
				break;
			case WHAT:
				tempQuestionTypes[1] = true;
				break;
			case WHERE:
				tempQuestionTypes[2] = true;
				break;
			case WHEN:
				tempQuestionTypes[3] = true;
				break;
			case WHY:
				tempQuestionTypes[4] = true;
				break;
			case HOW:
			case HOW_MUCH:
			case HOW_MANY:
			case HOW_OLD:
				tempQuestionTypes[5] = true;
				break;
		}
	    
		for(CoreMap sentence: sentenceAnno.get(SentencesAnnotation.class)) {
			int rawWordMatch = 0;
			int rawVerbMatch = 0;
			//Booleans for if it sentence contains NER stuff
			boolean [] tempNERs = {false, false, false, false, false};
			boolean [] tempWords = {false, false, false, false, false};
			
			for (CoreLabel questionToken: questionAnno.get(SentencesAnnotation.class).get(0).get(TokensAnnotation.class)) {
				String questionLemma = questionToken.get(LemmaAnnotation.class).toLowerCase();
				String questionPOS = questionToken.get(PartOfSpeechAnnotation.class);
				for (CoreLabel sentenceToken: sentence.get(TokensAnnotation.class)) {
					String sentenceLemma = sentenceToken.get(LemmaAnnotation.class).toLowerCase();
					String sentencePOS = sentenceToken.get(PartOfSpeechAnnotation.class);
					String sentenceNER = sentenceToken.get(NamedEntityTagAnnotation.class);
					if(questionLemma.equals(sentenceLemma)) {
						rawWordMatch++;
						if(questionPOS.startsWith("VB") && sentencePOS.startsWith("VB") && !questionLemma.equals("be") && !questionLemma.equals("do") && !questionLemma.equals("have")) 
							rawVerbMatch++;
					}	
					//Check for NERS
					if(sentenceNER.equals("PERSON"))
						tempNERs[0] = true;
					else if(sentenceNER.equals("ORGANIZATION"))
						tempNERs[1] = true;
					else if(sentenceNER.equals("LOCATION"))
						tempNERs[2] = true;
					else if(sentenceNER.equals("DATE"))
						tempNERs[3] = true;
					else if(sentenceNER.equals("TIME"))
						tempNERs[4] = true;
					
					//Check for special words
					if(sentenceLemma.trim().equals("name"))
						tempWords[0] = true;
					if(sentenceLemma.trim().equals("call"))
						tempWords[1] = true;
					if(sentenceLemma.trim().equals("year"))
						tempWords[2] = true;
					if(sentenceLemma.trim().equals("in"))
						tempWords[3] = true;
					if(sentenceLemma.trim().equals("to"))
						tempWords[4] = true;
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
			
			//NER adding it to the list
			hasNERs.add(tempNERs);
			
			//add special words to list
			hasWords.add(tempWords);
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
			Y[q] = dataset.attribute(CLASS_FEATURE).indexOfValue("No");

		//WEKA Stuff
		double [][] instances = new double [X.length][CLASS_FEATURE + 1];
		
		for(int i = 0; i < instances.length; i ++) {
			instances[i][0] = X[i][0];
			instances[i][1] = X[i][1];
			instances[i][CLASS_FEATURE] = Y[i];
			
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
			
			//Person NER
			if(hasNERs.get(i)[0])
				instances[i][8] = dataset.attribute(8).indexOfValue("Yes");
			else
				instances[i][8] = dataset.attribute(8).indexOfValue("No");
			
			//Organization NER
			if(hasNERs.get(i)[1])
				instances[i][9] = dataset.attribute(9).indexOfValue("Yes");
			else
				instances[i][9] = dataset.attribute(9).indexOfValue("No");
			
			//Location NER
			if(hasNERs.get(i)[2])
				instances[i][10] = dataset.attribute(10).indexOfValue("Yes");
			else
				instances[i][10] = dataset.attribute(10).indexOfValue("No");
			
			//Date NER
			if(hasNERs.get(i)[3])
				instances[i][11] = dataset.attribute(11).indexOfValue("Yes");
			else
				instances[i][11] = dataset.attribute(11).indexOfValue("No");
			
			//Time NER
			if(hasNERs.get(i)[4])
				instances[i][12] = dataset.attribute(12).indexOfValue("Yes");
			else
				instances[i][12] = dataset.attribute(12).indexOfValue("No");
			
			//Who Question
			if(tempQuestionTypes[0]) 
				instances[i][13] = dataset.attribute(13).indexOfValue("Yes");
			else
				instances[i][13] = dataset.attribute(13).indexOfValue("No");
			
			//What Question
			if(tempQuestionTypes[1]) 
				instances[i][14] = dataset.attribute(14).indexOfValue("Yes");
			else
				instances[i][14] = dataset.attribute(14).indexOfValue("No");
			
			//Where Question
			if(tempQuestionTypes[2]) 
				instances[i][15] = dataset.attribute(15).indexOfValue("Yes");
			else
				instances[i][15] = dataset.attribute(15).indexOfValue("No");
			
			//When Question
			if(tempQuestionTypes[3]) 
				instances[i][16] = dataset.attribute(16).indexOfValue("Yes");
			else
				instances[i][16] = dataset.attribute(16).indexOfValue("No");
			
			//Why Question
			if(tempQuestionTypes[4]) 
				instances[i][17] = dataset.attribute(17).indexOfValue("Yes");
			else
				instances[i][17] = dataset.attribute(17).indexOfValue("No");
			
			//How Question
			if(tempQuestionTypes[5]) 
				instances[i][18] = dataset.attribute(18).indexOfValue("Yes");
			else
				instances[i][18] = dataset.attribute(18).indexOfValue("No");
			
			//name
			if(hasWords.get(i)[0]) 
				instances[i][19] = dataset.attribute(19).indexOfValue("Yes");
			else
				instances[i][19] = dataset.attribute(19).indexOfValue("No");
			
			//call
			if(hasWords.get(i)[1]) 
				instances[i][20] = dataset.attribute(20).indexOfValue("Yes");
			else
				instances[i][20] = dataset.attribute(20).indexOfValue("No");
			
			//year
			if(hasWords.get(i)[2]) 
				instances[i][21] = dataset.attribute(21).indexOfValue("Yes");
			else
				instances[i][21] = dataset.attribute(21).indexOfValue("No");
			
			//in
			if(hasWords.get(i)[3]) 
				instances[i][22] = dataset.attribute(22).indexOfValue("Yes");
			else
				instances[i][22] = dataset.attribute(22).indexOfValue("No");
			
			//to
			if(hasWords.get(i)[4]) 
				instances[i][23] = dataset.attribute(23).indexOfValue("Yes");
			else
				instances[i][23] = dataset.attribute(23).indexOfValue("No");
		}		
		
		
		for(double [] instance : instances)
			dataset.add(new Instance(1.0, instance));
		
		dataset.setClassIndex(CLASS_FEATURE);

		return dataset;
	}
}


