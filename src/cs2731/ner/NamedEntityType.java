
package cs2731.ner;

/**
 * Indicates the type of a named entity.
 *
 * @author conrada@cs.pitt.edu
 */
public enum NamedEntityType {

    // possible named entity types
    TIME ("time"), LOCATION ("location"), ORGANIZATION ("organization"),
    PERSON ("person"), MONEY ("money"), PERCENT ("percent"), DATE ("date");

    private final String typeName;

    NamedEntityType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }

}
