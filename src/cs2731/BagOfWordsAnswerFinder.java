
package cs2731;

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
	
	public BagOfWordsAnswerFinder() {
		this(new Options()); 
	}
	
	public BagOfWordsAnswerFinder(Options options) {
		this.options = options;
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
		List<Score> scores = new LinkedList<Score>();
		int totalScore = 0;
		
		// populate a set of question words for quick lookup:
		Set<String> wordSet = new HashSet<String>();
		String splitString = (options.get(Options.IGNORE_PUNCTUATION))? "\\W+" : "\\s+";
		String[] tokens = question.split(splitString);
		for (String token : tokens) {
			wordSet.add(token.toLowerCase());
		}
		
		// loop over every line of the document and see how many words match:
		int lineNum = 0;
		for (String line : document) {
			lineNum++;
			if (containsOnlyWhitespace(line)) { continue; }
			
			int score = 0;
			tokens = line.split(splitString);
			for (String token : tokens) {
				if (wordSet.contains(token)) {
					score++;
				}
			}
			scores.add(new Score(lineNum, score));
			totalScore += score;
		}
		
		// return a list of guesses.
		// Probabilities are based on score/totalScore:
		for (Score score : scores) {
			guesses.add(new Guess( (score.val/(double)totalScore), score.line ));
		}
		
		return guesses;
	}
	
	
	private class Score {
		int line;
		int val;
		Score(int l, int v) {line = l; val = v;}
	}
	
}
