package cs2731;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * 
 * Simple class for removing stopwords. (TODO: merge this with other preprocessors)
 * 
 * @author conrada@cs.pitt.edu
 *
 */
public class StopwordRemover {
	
	// mysql 5.5 stopwords list
	public static final String STOPWORDS_PATH = "lib/stopwords.txt";
	
	public static List<String> stopwordList = null;

    // pipeline which performs annotation actions
    private static StanfordCoreNLP pipeline = null;
	
	public StopwordRemover() {
		
		if (pipeline == null) {
			Properties props = new Properties();
			props.put("annotators", "tokenize");
			pipeline = new StanfordCoreNLP(props);
		}
		
		if (stopwordList == null) {
			stopwordList = new ArrayList<String>();
			
			try {
				
				Scanner stopwordsScanner = new Scanner(new FileReader(STOPWORDS_PATH));
				while (stopwordsScanner.hasNext()) {
					stopwordList.add(stopwordsScanner.next());
				}
				stopwordsScanner.close();
				
			} catch (IOException e) {
				
			}
			
		}
		
	}
	
	public String removeStopwords(String orig) {
		
		for (String stopword : stopwordList) {
			orig = orig.replace(" "+stopword+" ", " ");
		}
		
		return orig;
		
	}
	
	public void removeStopwordsFromDocument(List<String> document) {
		
		for (int s=0; s<document.size(); s++) {
			String sentence = document.remove(s);
			Annotation sent = new Annotation(sentence);
			pipeline.annotate(sent);
			String newString = " ";
			for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                newString += word + " ";
			}
			
			newString = removeStopwords(newString);
			document.add(s, newString);
		}
		
	}
	
	// for testing only!
	public static void main(String[] args) {
		
	}
	
	
}
