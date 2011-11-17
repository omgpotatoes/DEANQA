
package cs2731.ner;

import cs2731.AnswerFinder;
import cs2731.CoreProcessor;
import cs2731.Guess;
import cs2731.Options;
import cs2731.Utils;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cs2731.Utils.*;

/**
 * Implements a simple bag-of-words model to predict answers
 * @author ylegall
 */
public class NameAnswerFinder implements AnswerFinder
{

	private NamedEntityService nerService;
	
	private boolean ignoreCase;
	private boolean ignorePunctuation;
//	private CoreProcessor coreProcessor;
	
	public NameAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public NameAnswerFinder(Options options) {
		ignoreCase = options.get(Options.IGNORE_CASE);
		ignorePunctuation = options.get(Options.IGNORE_PUNCTUATION);
		
		nerService = NamedEntityService.getInstance();
//		coreProcessor = CoreProcessor.getInstance();
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
		
		// get the target of the question type
		if (ignoreCase) { question = question.toLowerCase(); }
		Set<NamedEntityType> targetSet = Utils.getAnswerTypes(question);
		
		// loop over every line of the document and see how many words match:
		int lineNum = 0;
		for (String line : document) {
			lineNum++;
			if (containsOnlyWhitespace(line)) { continue; }
//			if (ignoreCase) {
//				line = line.toLowerCase();
//			}
			
			int score = 0;
			EnumSet<NamedEntityType> entityTypes = nerService.getNamedEntityTypes(line);
			
			// perform the set intersection
			entityTypes.retainAll(targetSet);
			score += entityTypes.size();

			// add the total
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
