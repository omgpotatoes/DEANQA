
package cs2731.ner;

/**
 * Indicates the type of a named entity.
 *
 * @author conrada@cs.pitt.edu
 */
public enum NamedEntityType {

    // possible named entity types
    TIME,
	LOCATION,
	ORGANIZATION,
    PERSON,
	MONEY,
	PERCENT,
	DATE;

    @Override
    public String toString() {
        return this.name();
    }
	
	/**
	 * Get the named entity type from its string representation
	 * @param str
	 * @return 
	 */
	public static NamedEntityType getTypeFromString(String str) {
		for (NamedEntityType type : values()) {
			if (type.toString().equals(str)) {
				return type;
			}
		}
		return null;
	}
}
