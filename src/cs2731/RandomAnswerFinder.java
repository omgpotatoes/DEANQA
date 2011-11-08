
package cs2731;

import java.util.LinkedList;
import java.util.List;



public class RandomAnswerFinder implements AnswerFinder
{

	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		
		int length = document.size();
		double prob = 1.0/length;
		
		List<Guess> guesses = new LinkedList<Guess>();
		for (int line=1; line <= length; line++) {
			guesses.add(new Guess(prob, line));
		}
		return guesses;
	}
	
}
