
package cs2731;

import java.lang.ref.SoftReference;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Indicates the type of a question
 *
 * @author conrada@cs.pitt.edu
 */
public enum QuestionType {

    OTHER,
	WHO,
	WHAT,
	WHERE,
    WHEN,
	WHY,
	HOW,
	WHICH,
    HOW_MUCH,
	HOW_OLD,
	HOW_MANY;
    
	private static SoftReference<WordMap> wordMapRef = new SoftReference<WordMap>(null);
	
	/**
	 * Get a set of question types that might be
	 * associated with a list of words
	 * @param words
	 * @return 
	 */
	public static Set<QuestionType> getQuestionTypes(List<String> words) {
		Set<QuestionType> set = EnumSet.noneOf(QuestionType.class);
		for (String word : words) {
			set.add(getTypeFromWord(word));
		}
		return set;
	}
	
	/**
	 * returns a named entity type associated with a particular word.
	 * For example, "he" would map to PERSON,
	 * and "during" would associate with TIME
	 * @param word
	 * @return 
	 */
	public static QuestionType getTypeFromWord(String word) {
		WordMap map = wordMapRef.get();
		if (map == null) {
			wordMapRef = new SoftReference<WordMap>(new WordMap());
			map = wordMapRef.get();
		}
		
		if (map.containsKey(word)) {
			return map.get(word);
		} else {
			return OTHER;
		}
	}
	
	
	private static class WordMap extends HashMap<String, QuestionType>
	{
		private WordMap() {
			// WHO
			put("he", WHO);
			put("she", WHO);
			put("it", WHO);
			put("they", WHO);
			put("him", WHO);
			put("her", WHO);
			put("them", WHO);
			
			// WHERE
			put("at", WHERE);
			put("located", WHERE);
			put("position", WHERE);
			put("where", WHERE);
			put("near", WHERE);
			put("close", WHERE);
			put("around", WHERE);
			put("inside", WHERE);
			
			// TIME
			put("during", WHEN);
			put("time", WHEN);
			put("when", WHEN);
			put("before", WHEN);
			put("after", WHEN);
			put("until", WHEN);
			
			// REASON
			put("because", WHY);
			put("for", WHY);
			put("reason", WHY);
			put("in order", WHY);
			
			// HOW
			put("by", HOW);
			
			// WHICH
			put("those", WHICH);
			put("ones", WHICH);
			
			put("years", HOW_OLD);
			
			put("many", HOW_MANY);
			put("percent", HOW_MANY);
			put("all", HOW_MANY);
			put("none", HOW_MANY);
			
			put("much", HOW_MUCH);
			put("very", HOW_MUCH);
			put("lot", HOW_MUCH);
		}
	}
	
}

