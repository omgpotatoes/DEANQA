
package cs2731;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
	 * 
	 */
	private static WordMap getWordMap() {
		WordMap map = wordMapRef.get();
		if (map == null) {
			wordMapRef = new SoftReference<WordMap>(new WordMap());
			map = wordMapRef.get();
		}
		return map;
	}
	
	
	/**
	 * 
	 * @param type
	 * @return 
	 */
	public static Set<String> getWords(QuestionType type) {
		return getWordMap().get(type);
	}

	/**
	 * 
	 */
	private static class WordMap extends HashMap<QuestionType, Set<String>>
	{
		private WordMap() {
			
			// WHO
			put(WHO, new HashSet<String>(
					Arrays.asList(
					"he","she","it","they","them","him","her"
					)));
			
			put(WHERE, new HashSet<String>(
					Arrays.asList(
					"at","in","inside","around","under","over",
					"close","near","where","next","aside","below",
					"located","on","along","against","between","beneath"
					)));
			
			put(WHEN, new HashSet<String>(
					Arrays.asList(
					"after","afternoon","ago","before",
					"day","during","evening","first","last",
					"later","month","morning","night",
					"then","time","today","tomorrow",
					"until","when","year","yesterday"
					)));
			
			put(HOW, new HashSet<String>(
					Arrays.asList(
					"by"
					)));
			
			put(WHY, new HashSet<String>(
					Arrays.asList(
					"because","for","so","to","reason","want"
					)));
			
			put(WHICH, new HashSet<String>(
					Arrays.asList(
					"those","that","this","ones","these"
					)));

			put(HOW_MANY, new HashSet<String>(
					Arrays.asList(
					"many","all","none","some","few","percent"
					)));
			
			put(HOW_OLD, new HashSet<String>(
					Arrays.asList(
					"years","old","young"
					)));

			put(HOW_MUCH, new HashSet<String>(
					Arrays.asList(
					"much","all","no","some","lot",
					"little","very"
					)));
		}
	}
	
}

