
package cs2731;

import java.util.List;
import java.util.Random;

/**
 * @author ygl2
 * ygl2@cs.pitt.edu
 * ylegall@gmail.com
 */
public class RandomAnswerFinder implements AnswerFinder
{

	@Override
	public int getAnswerLine(List<String> document, String question) {
		int lineNum;
		Random rand = new Random();
		String line = "";
		
		do {
			lineNum = rand.nextInt(document.size());
			line = document.get(lineNum);
		} while (line.length() < 3);
		
		return lineNum;
	}
	
}
