package cs2731;

import java.io.File;
import java.util.List;
import libsvm.*;

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
	 */
	public SVMAnswerFinder(String trainingQuestionsFolder, String trainingAnswersFilePath){
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
	 * @param trainingQuestionsFiles The names of the files for training (include sentences 
	 * and questions)
	 * @param trainingAnswersFile The answer sentences for the questions in the training
	 * @return 
	 */
	private SVMData extractData(File[] trainingQuestionsFiles, File trainingAnswersFile) {
		//This is where I stopped.  I should use extract answers here and possibly create 
		//an "extractQuestions" and/or "extractSentences"
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


