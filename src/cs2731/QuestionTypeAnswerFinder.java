
package cs2731;

import cs2731.ner.NamedEntityService;
import cs2731.ner.NamedEntityType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cs2731.Utils.*;
import static cs2731.QuestionType.*;

/**
 * Implements a simple bag-of-words model to predict answers
 * @author ylegall
 */
public class QuestionTypeAnswerFinder implements AnswerFinder
{
	private boolean ignoreCase;
	private boolean ignorePunctuation;
	private CoreProcessor coreProcessor;
	private NamedEntityService nerService;
	
	public QuestionTypeAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public QuestionTypeAnswerFinder(Options options) {
		ignoreCase = options.get(Options.IGNORE_CASE);
		ignorePunctuation = options.get(Options.IGNORE_PUNCTUATION);
		
//		nerService = NamedEntityService.getInstance();
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
			
			double score = 0;
//			EnumSet<NamedEntityType> types = nerService.getNamedEntityTypes(line);
//			EnumSet<NamedEntityType> types = coreProcessor.getNamedEntities(line);
			
			// get the set of possible question types
			String splitString = (ignorePunctuation)? "\\W+" : "\\s+";
			if (ignoreCase) { question = question.toLowerCase(); }
			Set<QuestionType> types = getQuestionTypes(Arrays.asList(line.split(splitString)));
			
			// perform the set intersection
			int maxCorrect = targetSet.size();
			targetSet.retainAll(types);
			score = 1.0 / ( maxCorrect - targetSet.size() + 1);

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
