
package cs2731;

import java.util.List;


public interface AnswerFinder
{
	public List<Guess> getAnswerLines(List<String> document, String question);
}
