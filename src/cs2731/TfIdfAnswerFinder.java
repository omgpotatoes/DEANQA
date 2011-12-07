
package cs2731;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * AnswerFinder which uses TF-IDF vectors to predict answers
 * 
 * @author conrada@cs.pitt.edu 
 */
public class TfIdfAnswerFinder implements AnswerFinder {
	
    // path to trained NER model
    public static String MODEL_PATH = "lib/muc.7class.distsim.crf.ser.gz";

    // pipeline which performs annotation actions
    private static StanfordCoreNLP pipeline = null;
    
    private static HashMap<String, Integer> termDocCount = null;
    private static int totalSents;
	
	
	private boolean ignoreCase;
	
	public TfIdfAnswerFinder(String inputFile) {
		if (pipeline == null) {
			Properties props = new Properties();
			props.put("annotators", "tokenize");
			pipeline = new StanfordCoreNLP(props);
			
			termDocCount = new HashMap<String, Integer>();
			totalSents = 0;
			
			// build termDocCounts using all documents
			try {
				Scanner input = new Scanner(new File(inputFile));

				// read the root path of the input files:
				String rootPath = input.nextLine();
				out.println("root dataset path = " + rootPath);

				while (input.hasNextLine()) {
					String filename = input.nextLine().trim();
					File story = new File(rootPath, filename);

					Scanner inputStory = new Scanner(story);
					out.println("TfIdfAnswerFinder: preprocessing " + story.getName());

					while (inputStory.hasNextLine()) {
						String line = inputStory.nextLine();
						if (line.contains("<QUESTIONS>")) {
							break;
						}

						String sentText = Preprocessor.removePunct(line).toLowerCase();
						Annotation sent = new Annotation(sentText);
						pipeline.annotate(sent);
						
						HashMap<String, Boolean> termHasAppeared = new HashMap<String, Boolean>();
						totalSents++;
						
						for (CoreLabel token : sent.get(TokensAnnotation.class)) {

							String word = token.get(TextAnnotation.class);
							if (!termHasAppeared.containsKey(word)) {
								if (!termDocCount.containsKey(word)) {
									termDocCount.put(word, 0);
								}
								termDocCount.put(word, termDocCount.get(word) + 1);
								termHasAppeared.put(word, true);
							}
						}
						
					}

				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err
						.println("TfIdfAnswerFinder: error reading files for count building");
			}

		}
	}
	
	
	/**
	 * Selects answer sentence based on tf-idf similarity to question. Treats
	 * each sentence within the document as a doc.
	 * 
	 * @param document
	 * @param question
	 * @return 
	 */
	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		
		// use stanford to tokenize each sentence
		// sum number of times each token occurs in document, sentence
		//HashMap<String, Integer> termDocCount = new HashMap<String, Integer>();
		List<HashMap<String, Integer>> sentTermCount = new ArrayList<HashMap<String, Integer>>();
		for (int s=0; s<document.size(); s++) {
			
			HashMap<String, Integer> thisSentTermCount = new HashMap<String, Integer>();
			sentTermCount.add(thisSentTermCount);
			String sentText = Preprocessor.removePunct(document.get(s)).toLowerCase();
			Annotation sent = new Annotation(sentText);
			pipeline.annotate(sent);
			
			// tokens which have already appeared in this document 
			//  (so we don't count a term more than once for a single sent)
			//HashMap<String, Integer> appearedTokens = new HashMap<String, Integer>();
			
			//HashMap<String, Boolean> termHasAppeared = new HashMap<String, Boolean>();
			
			for (CoreLabel token : sent.get(TokensAnnotation.class)) {
				
                String word = token.get(TextAnnotation.class);
                
                // debug
                //System.out.println("debug: word token: "+word);
                
                //if (!appearedTokens.containsKey(word)) {
                	//if (!termDocCount.containsKey(word)) {
                	//	termDocCount.put(word,  0);
                	//}
                	//termDocCount.put(word, termDocCount.get(word)+1);
                	//appearedTokens.put(word, 1);
                //}  // let's only count a word once also for its appearance in a sentence (to mitigate the grr of repeated mentions from the coref res.)
                //if (!termHasAppeared.containsKey(word)) {
                	if (!thisSentTermCount.containsKey(word)) {
                		thisSentTermCount.put(word,  0);
                	}
                	thisSentTermCount.put(word, thisSentTermCount.get(word)+1);
                	//termHasAppeared.put(word, true);
                //}
                //}
                
			}
			
		}
		
		
		// compute tf-idf scores for each word in each sentence
		List<HashMap<String, Double>> sentTermTfIdf = new ArrayList<HashMap<String, Double>>();
		for (int s=0; s<sentTermCount.size(); s++) {

			HashMap<String, Double> thisSentTermTfIdf = new HashMap<String, Double>();
			sentTermTfIdf.add(thisSentTermTfIdf);
			HashMap<String, Integer> thisSentTermCount = sentTermCount.get(s);
			
			Set<String> sentTerms = thisSentTermCount.keySet();
			//int totalSents = document.size();
			int wordsInSent = sentTerms.size();
			for (String term : sentTerms) {
				
				double tf = (double)thisSentTermCount.get(term) / (double)wordsInSent;
				double idf = Math.log10((double)totalSents / (double)termDocCount.get(term));
				double tfidf = tf * idf;
				thisSentTermTfIdf.put(term, tfidf);
				
                // debug
                //System.out.println("debug: tfidf for \""+term+"\" in sent \""+s+"\": "+tfidf);
				
			}
			
			
		}
		
		// rank sentences based on sum of tf-idf values of terms appearing in query
		String questText = Preprocessor.removePunct(question).toLowerCase();
		Annotation quest = new Annotation(questText);
		pipeline.annotate(quest);
		List<Double> sentScores = new ArrayList<Double>();
		double totalScore = 0.0;
		for (int s=0; s<sentTermTfIdf.size(); s++) {
			
			HashMap<String, Double> thisSentTermTfIdf = sentTermTfIdf.get(s);
			double totalSentScore = 0.0;
			
			for (CoreLabel token : quest.get(TokensAnnotation.class)) {
				
                String word = token.get(TextAnnotation.class);
                double wordSentScore = 0.0;
                if (thisSentTermTfIdf.containsKey(word)) {
                	wordSentScore = thisSentTermTfIdf.get(word);
                }
                
                // debug
                //System.out.println("debug: tfidf for question term \""+word+"\" in sent \""+s+"\": "+wordSentScore);
                
                totalSentScore += wordSentScore;
                
			}
			
			sentScores.add(totalSentScore);
			totalScore += totalSentScore;
			
		}
		
		// build guesses (normalize scores)
		List<Guess> guesses = new ArrayList<Guess>();
		for (int s=0; s<sentScores.size(); s++) {
			// debug:
			//System.out.println("debug: score for sentence "+s+" on question \""+question+"\":"+sentScores.get(s)/totalScore);
			guesses.add(new Guess(sentScores.get(s)/totalScore, s+1));
		}
		
		Collections.sort(guesses);
		Collections.reverse(guesses);
		
		return guesses;
		
	}
	
	// for testing only!
	public static void main(String[] args) {

        List<String> sampleSents = new ArrayList<String>();
        sampleSents.add("Yesterday, Dr. James Howard ate five hundred dollars worth of deluxe seafood biscuits while he was at the opera.");
        sampleSents.add("");
        sampleSents.add("After devouring all of the munchies, he said, \"I shall bake a cake for the local chapter of the Rotary Club.\"");
        sampleSents.add("The club loved his delicious offering, and decided to dedicate the month of October to him.");
        sampleSents.add("");
        sampleSents.add("");
        sampleSents.add("Dweezil Rickenbacker, long time biscuit afficionado, was not pleased with James' good fortune or his betrayal by his friends in the club.");
        
        TfIdfAnswerFinder answerFinder = new TfIdfAnswerFinder(null);
        
        answerFinder.getAnswerLines(sampleSents, "Of what was Dweezil Rickenbacker an afficionado?");
        answerFinder.getAnswerLines(sampleSents, "What month was dedicated to Dr. James Howard?");
        
	}
	
}