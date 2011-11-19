
package cs2731;

import edu.stanford.nlp.util.CoreMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cs2731.Utils.*;

/**
 *
 * @author ylegall
 */
public class BagOfVerbsAnswerFinder implements AnswerFinder
{
	
	private Options options;
	private CoreProcessor coreProcessor;
	
	public BagOfVerbsAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public BagOfVerbsAnswerFinder(Options options) {
		this.options = options;
		coreProcessor = CoreProcessor.getInstance();
	}

	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		List<Guess> guesses = new LinkedList<Guess>();
		List<Guess> scores = new LinkedList<Guess>();
		int totalScore = 0;
		
		// populate a set of question words for quick lookup:
		Set<String> verbSet = new HashSet<String>();

		// get the lemmas for verbs only
		List<CoreMap> verbs = coreProcessor.annotateDocument(question);
		verbSet.addAll(CoreProcessor.getVerbLemmas(verbs));
		
		// loop over every line of the document and see how many verbs match:
		int lineNum = 0;
		for (String line : document) {
			lineNum++;
			if (containsOnlyWhitespace(line)) { continue; }

			// count the number of matching verb lemmas:
			int score = 0;
			verbs = coreProcessor.annotateDocument(line);
			List<String> verbLemmas = CoreProcessor.getVerbLemmas(verbs);
			for (String verb : verbLemmas) {
				if (verbSet.contains(verb)) {
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
