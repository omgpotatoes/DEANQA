
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
//	private CoreProcessor processor;
	
	public BagOfWordsAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public BagOfWordsAnswerFinder(Options options) {
		this.options = options;
		ignoreCase = options.get(Options.IGNORE_CASE);
//		processor = CoreProcessor.getInstance();
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
		if (ignoreCase) { question = question.toLowerCase(); }
		String[] tokens = question.split(splitString);
		wordSet.addAll(Arrays.asList(tokens));
		
//		wordSet.addAll(processor.getLemmas(question));
		
		// loop over every line of the document and see how many words match:
		int lineNum = 0;
		for (String line : document) {
			lineNum++;
			if (containsOnlyWhitespace(line)) { continue; }
			
			if (ignoreCase) {
				line = line.toLowerCase();
			}
			
			int score = 0;

			tokens = line.split(splitString);
			for (String token : tokens) {
				if (wordSet.contains(token)) {
					score++;
				}
			}
			scores.add(new Guess(score, lineNum));
			totalScore += score;
		}
		
		// return a list of guesses.
		// Probabilities are based on score/totalScore:
		for (Guess score : scores) {
			guesses.add(new Guess(
					(score.getProb()/totalScore),
					score.getLine()
					));
		}
		
		return guesses;
	}
	
}
