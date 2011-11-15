
package cs2731.ner;

/**
 * Represents an instance of a named entity.
 *
 * @author conrada@cs.pitt.edu
 */
public class NamedEntity {

	private NamedEntityType type;
	private String entity;

	// TIME, LOCATION, ORGANIZATION, PERSON, MONEY, PERCENT, DATE
	public NamedEntity(NamedEntityType type, String entity) {
		this.type = type;
		this.entity = entity;
	}

	public String getEntity() {
		return entity;
	}

	public NamedEntityType getType() {
		return type;
	}

	@Override
	public String toString() {
		String output = "(" + entity + ":" + type + ")";
		return output;
	}
	
}
