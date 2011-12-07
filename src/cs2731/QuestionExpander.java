package cs2731;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * 
 * Preprocessor which expands question by adding WordNet synonyms to query.
 * 
 * @author conrada@cs.pitt.edu
 *
 */
public class QuestionExpander {
	
	private static final String WN_PATH = "lib/dict";
	private static URL wnUrl = null;
	public static final POS[] POS_LIST = {
		POS.NOUN, 
		POS.ADJECTIVE, 
		POS.ADVERB, 
		POS.VERB
	};
	
	private static IDictionary dict = null;
	
    // pipeline which performs annotation actions
    private static StanfordCoreNLP pipeline = null;
	
	public QuestionExpander() {
		
		if (dict == null) {
			try {
				wnUrl = new URL("file", null, WN_PATH);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.err.println("bad url to wordnet dictionary, exiting");
				System.exit(1);
			}
			dict = new Dictionary(wnUrl);
			try {
				dict.open();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("unable to read from wordnet dictionay, exiting");
				System.exit(1);
			}
		}
		
		if (pipeline == null) {
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos");
			pipeline = new StanfordCoreNLP(props);
		}
		
	}
	
	/**
	 * Returns all synonyms of a given word for all POS.
	 * 
	 * @param word
	 * @return
	 */
	public List<String> getAllSynonyms(String word, String posName) {
		
		List<String> synList = new ArrayList<String>();
		//System.out.println("debug: pos: "+posName);
		// TODO: translate stanford pos info to wordnet pos info
		for (POS pos : POS_LIST) {
			IIndexWord idxWord = dict.getIndexWord(word, pos);
			if (idxWord != null) {
				for (IWordID synWordID : idxWord.getWordIDs()) {
					IWord synWord = dict.getWord(synWordID);
					ISynset synset = synWord.getSynset();
					for (IWord w : synset.getWords()) {
						
						String synLemma = w.getLemma().replace("_", " ");
						if (!synList.contains(synLemma)) {
							synList.add(synLemma);
							// debug
							//System.out.println("debug: adding synword "+synLemma+" to synlist");
							
						}
						
					}
				}
			}
		}
		
		return synList;
		
	}
	
	public String addSynwordsToQuestion(String origQuestion) {
		
		String origQuestionPlain = Preprocessor.removePunct(origQuestion).toLowerCase()+" ";
		Annotation q = new Annotation(origQuestionPlain);
		pipeline.annotate(q);

		for (CoreLabel token : q.get(TokensAnnotation.class)) {
			
            String word = token.get(TextAnnotation.class);
            String pos = token.get(PartOfSpeechAnnotation.class);
            
            List<String> wordSyns = getAllSynonyms(word, pos);
            
            for (String syn : wordSyns) {
            	if (!origQuestionPlain.contains(syn+" ")) {
            		origQuestionPlain += syn + " ";
            	}
            }
            
		}
		
		return origQuestionPlain;
		
	}
	
	
	// for testing only!
	public static void main(String[] args) {
		
		QuestionExpander qExp = new QuestionExpander();
		
		/*
		System.out.println("\nbuilding syns for 'donate':");
		qExp.getAllSynonyms("donate");

		System.out.println("\nbuilding syns for 'benefit':");
		qExp.getAllSynonyms("benefit");

		System.out.println("\nbuilding syns for 'native':");
		qExp.getAllSynonyms("native");
		*/
		
		String q1 = "Why are most native Canandians exposed to second hand smoke in their homes?";
		String q2 = "How much does Alexi Yashin earn as a hockey player?";
		String q3 = "When did Wang leave China?";
		
		System.out.println("q1 orig: "+q1);
		System.out.println("q1 expanded: "+qExp.addSynwordsToQuestion(q1));

		System.out.println("q2 orig: "+q2);
		System.out.println("q2 expanded: "+qExp.addSynwordsToQuestion(q2));

		System.out.println("q3 orig: "+q3);
		System.out.println("q3 expanded: "+qExp.addSynwordsToQuestion(q3));
		
	}
	
	

}
