
package cs2731;


public class Utils
{
	/**
	 * 
	 * @param str
	 * @return 
	 */
	static boolean containsOnlyWhitespace(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param str
	 * @return 
	 */
	static boolean containsOnlyLetters(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}
	
}
