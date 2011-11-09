
package cs2731;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;



public class RandomAnswerFinder implements AnswerFinder
{
	private Random rand;
	
	public RandomAnswerFinder() {
		rand = new Random();
	}

	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		
		int length = document.size();
		List<Guess> guesses = new LinkedList<Guess>();
		
		// return all lines with equal probability:
//		double prob = 1.0/length;
//		for (int line=1; line <= length; line++) {
//			guesses.add(new Guess(prob, line));
//		}
		
		// pick a single random guess with 100% confidence:
		int line = rand.nextInt(length);
		while (document.get(line).length() < 3) {
			line = rand.nextInt(length);
		}
		guesses.add(new Guess(1.0, line + 1));
		
		return guesses;
	}
	
}
