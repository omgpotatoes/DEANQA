
package cs2731;

import cs2731.ner.NameAnswerFinder;
import cs2731.ner.RandomNameAnswerFinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for running AnswerFinders (potentially in parallel)
 * and collecting the results.
 * 
 * Also, depending on the question type, we may want to only rely on a subset of 
 * AnswerFinders, or assign different weights to each guess, instead of
 * combining the results equally.
 */
public class AnswerFinderManager
{
	private List<AnswerFinder> finders;
	private List<Guess> guesses;
	private List<Guess> answers;
	
	private List<String> document;
	private List<String> questions;
	
	AnswerFinderManager() {
		finders = new ArrayList<AnswerFinder>();
		guesses = new LinkedList<Guess>();
		
		finders.add(new BagOfWordsAnswerFinder());
		finders.add(new BagOfLemmasAnswerFinder());
		finders.add(new BagOfVerbsAnswerFinder());
		finders.add(new NameAnswerFinder());
		finders.add(new RandomNameAnswerFinder());
		//finders.add(new TfIdfAnswerFinder());
		finders.add(new RuleAnswerFinder());
//		finders.add(new SVMAnswerFinder(null, null));
	}

	/**
	 * 
	 * @param questions
	 * @param document
	 * @return 
	 */
	public List<Guess> getAnswers(List<String> questions, List<String> document) {
		this.document = document;
		this.questions = questions;
		
		answers.clear();
		for (String question : questions) {
			
			guesses.clear();
			for (AnswerFinder finder : finders) {
				guesses.addAll(finder.getAnswerLines(document, question));
			}
			
			// combine guesses
			// add final answer to answers list
		}
		
		return answers;
	}
	
	/**
	 * Sum probabilities for all guesses for each sentence
	 * 
	 * @param guesses
	 * @return
	 */
	static List<Guess> combineGuesses(List<Guess> guesses) {
		Map<Integer, Double> guessMap = new HashMap<Integer, Double>();
		for (Guess guess : guesses) {
			if (!guessMap.containsKey(guess.getLine())) {
				guessMap.put(guess.getLine(), 0.0);
			}
			guessMap.put(guess.getLine(), guessMap.get(guess.getLine()) + guess.getProb());
		}

		List<Guess> combinedGuesses = new ArrayList<Guess>();
		for (Integer lineNum : guessMap.keySet()) {
			combinedGuesses.add(new Guess(guessMap.get(lineNum), lineNum));
		}
		return combinedGuesses;
	}
	
	public static void main(String[] args) {
		AnswerFinderManager m = new AnswerFinderManager();
	}
}
