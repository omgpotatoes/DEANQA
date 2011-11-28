
package cs2731;

import java.util.Collection;
import cs2731.ner.NamedEntityService;
import cs2731.ner.NamedEntityType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cs2731.Utils.*;
import static cs2731.QuestionType.*;
import static cs2731.ner.NamedEntityType.*;

/**
 * Implements the rule-based AnswerFinder by
 * Ellen Riloff and Michael Thelen
 * Department of Computer Science
 * University of Utah
 * @author ylegall
 */
public class RuleAnswerFinder implements AnswerFinder
{
	
	static final int CLUE = 3;
	static final int GOOD_CLUE = 4;
	static final int CONFIDENT = 6;
	static final int SLAM_DUNK = 20;
	
	private Options options;
	private boolean ignoreCase;
	private boolean ignorePunctuation;
	
	private CoreProcessor coreProcessor;
	private NamedEntityService nerService;
	
	public RuleAnswerFinder() {
		this(Options.getDefaultOptions()); 
	}
	
	public RuleAnswerFinder(Options options) {
		this.options = options;
		ignoreCase = options.get(Options.IGNORE_CASE);
		ignorePunctuation = options.get(Options.IGNORE_PUNCTUATION);
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
		QuestionType type = QuestionTypeDetector.getQuestionType(question);
		List<String> questionWords = Arrays.asList(tokenize(question, options));
		Set<NamedEntityType> questionEntities = coreProcessor.getNamedEntities(question);
		
		// loop over every line of the document and see how many words match:
		int lineNum = 0;
		for (String line : document) {
			lineNum++;
			
			if (containsOnlyWhitespace(line)) { continue; }
			if (ignoreCase) { line = line.toLowerCase(); }
			
			// get the named entities for the line:
			Set<NamedEntityType> lineEntities = coreProcessor.getNamedEntities(line);
			List<String> lineWords = Arrays.asList(tokenize(line, options));
			
			double score = 0;
			
			switch (type) {
				case WHO:
					score += wordMatch(questionWords, lineWords);
					if (!(questionEntities.contains(PERSON) || questionEntities.contains(ORGANIZATION))) {
						if (lineEntities.contains(PERSON) || lineEntities.contains(ORGANIZATION)) {
							score += CONFIDENT;
						} else if (lineWords.contains("name")) {
							score += GOOD_CLUE;
						}
					} else {
						if (lineEntities.contains(PERSON) || lineEntities.contains(ORGANIZATION)) {
							score += GOOD_CLUE;
						}
					}
					break;
					
				case WHEN:
					if (lineEntities.contains(TIME)) {
						score += GOOD_CLUE;
						score += wordMatch(questionWords, lineWords);
					}
					if (question.contains("the last")) {
						if (containsAny(lineWords, "first","last","since","ago")) {
							score += SLAM_DUNK;
						}
					}
					if (containsAny(questionWords, "begin", "start")) {
						if (containsAny(lineWords, "start","begin","since","year")) {
							score += SLAM_DUNK;
						}
					}
					break;
					
				case WHERE:
					score += wordMatch(questionWords, lineWords);
					// TODO: finish
					break;
					
				case WHAT:
					break;
				default:
					break;
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
	
	/**
	 * 
	 */
	public static int wordMatch(Collection<String> question, Collection<String> line) {
		Set<String> qwords = new HashSet<String>(question);
		int count = 0;
		for (String word: line) {
			if (qwords.contains(word)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 
	 */
	public static int questionTypeMatch(Set<QuestionType> questionTypes, Collection<QuestionType> line) {
		int count = 0;
		for (QuestionType wordType: line) {
			if (questionTypes.contains(wordType)) {
				count++;
			}
		}
		return count;
	}
}
