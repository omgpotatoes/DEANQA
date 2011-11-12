
package cs2731;

import java.util.BitSet;

/**
 * Encapsulates various options and configurations that might be
 * useful when processing text.
 * @author ylegall
 */
public class Options extends BitSet	// TODO: come up with a better name
{
	public static final int IGNORE_CASE = 1;
	public static final int IGNORE_PUNCTUATION = 2;
	public static final int IGNORE_AFFIX = 3;
	public static final int IGNORE_COMMON_WORDS = 4;
	
}
