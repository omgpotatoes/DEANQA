package cs2731;

public class Question {
	public String filename;
	public String questionText;
	public int questionNumber;
	
	public Question() {}
	
	public Question(String inFilename, String inQuestionText, int inQuestionNumber) {
		this.filename = inFilename;
		this.questionText = inQuestionText;
		this.questionNumber = inQuestionNumber;
	}
	
	public String toString() {
		return ("{" + filename + " | " + questionText + " | " + questionNumber + "}");
	}
}
