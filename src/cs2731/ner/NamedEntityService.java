
package cs2731.ner;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cs2731.ner.NamedEntityType.*;

/**
 * Manages the initial loading of the Stanford NER tool
 * and provides a central interface for all other modules that 
 * require Named Entity Recognition.
 * 
 * @author ylegall
 */
public class NamedEntityService
{
	private static final String MODEL_PATH = "lib/muc.7class.distsim.crf.ser.gz";
	
	private AbstractSequenceClassifier classifier;
	
	public NamedEntityService() {
		classifier = CRFClassifier.getClassifierNoExceptions(MODEL_PATH);
	}
	
	/**
	 * Get the set of named entities contained within a string of text
	 * @param text
	 * @return A set of NamedEntity
	 */
	public Set<NamedEntity> getNamedEntities(String text) {
		Set<NamedEntity> set = new LinkedHashSet<NamedEntity>();
		List<List<CoreLabel>> out = classifier.classify(text);
		for (List<CoreLabel> sentence : out) {
			for (CoreLabel word : sentence) {
				NamedEntityType type = getTypeFromString(word.get(AnswerAnnotation.class));
				if (type != null) {
					set.add(new NamedEntity(type, word.word()));
				}
			}
		}
		return set;
	}
	
	// test:
	public static void main(String[] args) {
		NamedEntityService service = new NamedEntityService();
		String text = "I go to school at Stanford University, which is located in California.";
		System.out.println(service.getNamedEntities(text));
	}
}
