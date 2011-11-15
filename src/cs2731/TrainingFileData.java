package cs2731;
/**
 * Representation of all information needed from a file to train on the data
 * @author Eric Heim
 *
 */
public class TrainingFileData {
	/** The sentences in the document */
	public String document;
	/** The questions for the above document */
	public String questions;
	/** Used as a map from question number to answer sentence */ 
	public String [] answerMap;
	
	public TrainingFileData() {}
	
	public TrainingFileData(String inDocument, String inQuestions, String [] inAnswerMap) {
		this.document = inDocument;
		this.questions = inQuestions;
		this.answerMap = inAnswerMap;
	}
	
	public String toString() {
		String returnString = "DOCUMENT\n" + document + "\n" + "QUESTIONS\n" + questions + "\n" + "ANSWER MAP\n";
		
		for(int i = 0; i < answerMap.length; i++) 
			returnString += (i + 1) + ": " + answerMap[i] + "\n";
		
		return returnString;
	}
}
