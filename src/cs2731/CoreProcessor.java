package cs2731;

import cs2731.ner.NamedEntityType;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.lang.ref.SoftReference;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author ygl2
 * ygl2@cs.pitt.edu
 * ylegall@gmail.com
 */
public class CoreProcessor
{

	private static SoftReference<CoreProcessor> instance;
	
	private StanfordCoreNLP pipeline;

	static {
		instance = new SoftReference<CoreProcessor>(null);
	}
	
	/**
	 * 
	 * @return 
	 */
	public static CoreProcessor getInstance() {
		CoreProcessor service = instance.get();
		if (service == null) {
			instance = new SoftReference<CoreProcessor>(new CoreProcessor());
			service = instance.get();
		}
		return service;
	}
	
	/**
	 * 
	 */
	static void dispose() {
		instance.clear();
	}
	
	private CoreProcessor() {
		// creates a StanfordCoreNLP object,
		// with POS tagging, lemmatization, and NER
		Properties props = new Properties();
//		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * 
	 * @param docString
	 * @return 
	 */
	public List<CoreMap> annotateDocument(String docString) {
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class
		// objects as keys and has values with custom types
		Annotation document = new Annotation(docString);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		return sentences;
	}
	
	/**
	 * get the lemma annotations corresponding to the list of coremap
	 * @param sentences
	 * @return 
	 */
	public static List<String> getLemmas(List<CoreMap> sentences) {
		return getAnnotations(LemmaAnnotation.class, sentences);
	}
	
	/**
	 * Gets a list of annotations based on the class of annotation passed in
	 * @param clazz
	 * @param sentences
	 * @return 
	 */
	public static List<String> getAnnotations(Class<? extends CoreAnnotation<String>> clazz, List<CoreMap> sentences) {
		List<String> list = new LinkedList<String>();
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				list.add(token.get(clazz));
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param list
	 * @return 
	 */
	public static EnumSet<NamedEntityType> getNamedEntities(List<CoreMap> list) {
		EnumSet<NamedEntityType> types = EnumSet.noneOf(NamedEntityType.class);
		for (CoreMap sentence : list) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				if (token.has(NamedEntityTagAnnotation.class)) {
					types.add(NamedEntityType.getTypeFromString(token.get(NamedEntityTagAnnotation.class)));
				}
			}
		}
		return types;
	}

	public static void main(String[] args) {
		CoreProcessor tp = CoreProcessor.getInstance();
		String line = "Bill goes to school at Stanford University. He lives in California.";
		tp.annotateDocument(line);
	}
}
