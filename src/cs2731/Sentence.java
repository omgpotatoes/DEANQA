package cs2731;

public class Sentence {
	public String filename;
	public String sentenceText;
	public int sentenceLine;
	
	public Sentence() {}
	
	public Sentence(String inFilename, String inSentenceText, int inSentenceLine) {
		this.filename = inFilename;
		this.sentenceText = inSentenceText;
		this.sentenceLine = inSentenceLine;
	}
	
	public String toString() {
		return ("{" + filename + " | " + sentenceText + " | " + sentenceLine + "}");
	}
	
}
