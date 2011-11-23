
package cs2731;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cs2731.Utils.*;

/**
 * @author ygl2
 * ygl2@cs.pitt.edu
 * ylegall@gmail.com
 */
public class BagOfLemmasAnswerFinder implements AnswerFinder
{

	private boolean ignoreCase;
	private boolean ignorePunctuation;
	private CoreProcessor coreProcessor;
	
	public BagOfLemmasAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public BagOfLemmasAnswerFinder(Options options) {
		ignoreCase = options.get(Options.IGNORE_CASE);
		ignorePunctuation = options.get(Options.IGNORE_PUNCTUATION);
		coreProcessor = CoreProcessor.getInstance();
	}
	
	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		List<Guess> guesses = new LinkedList<Guess>();
		List<Guess> scores = new LinkedList<Guess>();
		int totalScore = 0;
		
		// populate a set of question words for quick lookup:
		Set<String> wordSet = new HashSet<String>();
		for (String str : coreProcessor.getLemmas(question)) {
			if (ignorePunctuation) {
				if (containsOnlyPunctuation(str)) continue;
			}
			if (ignoreCase) { str = str.toLowerCase(); }
			wordSet.add(str);
		}
		
		
		// loop over every line of the document and see how many words match:
		int lineNum = 0;
		for (String line : document) {
			lineNum++;
			if (containsOnlyWhitespace(line)) { continue; }
			
			int score = 0;
			List<String> tokens = coreProcessor.getLemmas(line);
			for (String token : tokens) {
				if (ignorePunctuation) {
					if (containsOnlyPunctuation(token)) continue;
				}
				
				if (ignoreCase) { token = token.toLowerCase(); }
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
