package cs2731;

/**
 * Class that holds all the information about an answer
 * @author Eric Heim
 */
public class Answer {
	/** The name of the file where the question appears that this is the answer to (I have no
	 * idea how to explain this better)*/
	public String fileName;
	/** The question that this answers */
	public String questionText;
	/** The sentence that answers the question */
	public String [] answerTexts;
	/** The question number that this answers */
	public int questionNumber;
	/** What line the answer sentence appears on */
	public int [] answerLines;
	
	/**
	 * Default Constructor (Make sure to manually set fields if you use this)
	 */
	public Answer() {}
	
	/**
	 * Constructor that takes sets the fields to the parameter values
	 * @param inFileName
	 * @param inQuestionNumber
	 * @param inAnswerLine
	 * @param inQuestionText
	 * @param inAnswerText
	 */
	public Answer(String inFileName, int inQuestionNumber, int [] inAnswerLine, 
			String inQuestionText, String [] inAnswerText) {
		this.fileName = inFileName;
		this.questionNumber = inQuestionNumber;
		this.answerLines = inAnswerLine;
		this.questionText = inQuestionText;
		this.answerTexts = inAnswerText;
	}
	
	@Override public String toString() {
		String lines = "";
		String texts = "";
		for(int i = 0; i < answerLines.length; i++) {
			lines += answerLines[i];
			if(i < answerLines.length-1)
				lines += ", ";
		}  
		for(int i = 0; i < answerTexts.length; i++) {
			texts += answerTexts[i];
			if(i < answerTexts.length-1)
				texts += "  -OR- ";
		}  
		return ("{" + fileName + " | " + questionNumber + " | " + lines + " | " + questionText + 
			    " | " + texts + "}");
	}
}
