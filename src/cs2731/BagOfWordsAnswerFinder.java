
package cs2731;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cs2731.Utils.*;

/**
 * Implements a simple bag-of-words model to predict answers
 * @author ylegall
 */
public class BagOfWordsAnswerFinder implements AnswerFinder
{

	private Options options;
	private boolean ignoreCase;
	private Set<String> stopWords;
	
	public BagOfWordsAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public BagOfWordsAnswerFinder(Options options) {
		this.options = options;
		options.set(Options.IGNORE_PUNCTUATION, false);
		ignoreCase = options.get(Options.IGNORE_CASE);
		
		stopWords = new HashSet<String>();
		stopWords.addAll(Arrays.asList(
				"the","a","to","and","or","an","it",
				"was","were","is","did","be",
				"as","at","by","for","if"
				));
	}
	
	static String sanitize(String line) {
		line = line.trim();
		line = line.toLowerCase();
		line = line.replaceAll("[-_;:,\\.\"!\\?]+", " ");
//		int period = line.lastIndexOf('.');
//		if (line.length() - period < 10) {
//			line = line.substring(0, line.lastIndexOf('.'));
//		}
		return line;
	}
	
	/**
	 * 
	 * @param document
	 * @param question
	 * @return 
	 */
	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		List<Guess> guesses = new LinkedList<Guess>();
		List<Guess> scores = new LinkedList<Guess>();
		int totalScore = 0;
		
		// populate a set of question words for quick lookup:
		Set<String> wordSet = new HashSet<String>();
		String splitString = (options.get(Options.IGNORE_PUNCTUATION))? "\\W+" : "\\s+";
		
//		if (ignoreCase) { question = question.toLowerCase(); }
		question = sanitize(question);
		
		String[] tokens = question.split(splitString);
		wordSet.addAll(Arrays.asList(tokens));
		
		// loop over every line of the document and see how many words match:
		int lineNum = 0;
		
//		System.out.println("\n!!! QUESTION: " + question);
		
		for (String line : document) {
			lineNum++;
			if (containsOnlyWhitespace(line)) { continue; }
			
//			if (ignoreCase) { line = line.toLowerCase(); }
			line = sanitize(line);
			
			int score = 0;
			tokens = line.split(splitString);
			for (String token : tokens) {
				if (stopWords.contains(token)) {
					continue;
				}
				
				if (wordSet.contains(token)) {
					score++;
				}
			}
			scores.add(new Guess(score, lineNum));
			totalScore += score;
			
//			System.out.println("!!!\tSENTENCE: " + lineNum + "  "+ line);
//			System.out.println("!!!\tSCORE: " + score);
			
		}
		
		// return a list of guesses.
		// Probabilities are based on score/totalScore:
		for (Guess score : scores) {
			double prob = (score.getProb()/totalScore);
			guesses.add(new Guess(
					prob,
//					prob*prob,
					score.getLine()
					));
		}
		
		return guesses;
	}

	public static void main(String[] args) {
		String line = "asdad   adsgf   sopi  ";
		System.out.println(Arrays.toString(line.split("\\s+")));
	}
	
}