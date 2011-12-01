package cs2731;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cs2731.discourse.DiscourseUtils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class BagOfNGramsAnswerFinder implements AnswerFinder {


    // pipeline which performs annotation actions
    StanfordCoreNLP pipeline;
	
	
	public BagOfNGramsAnswerFinder() {
		
		if (pipeline == null) {
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			pipeline = new StanfordCoreNLP(props);
		}
		
		
	}
	
	
	
	
	
	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		
		// preprocess question
		String thisQuestion = question;
		thisQuestion = thisQuestion.replace('.', ' ');
		thisQuestion = thisQuestion.replace('?', ' ');
		thisQuestion = thisQuestion.replace('!', ' ');
		thisQuestion = thisQuestion + " . ";
		
		Annotation questionAnnot = new Annotation(thisQuestion);
		pipeline.annotate(questionAnnot);
		
		String thisQuestionTokenized = "";
		for (CoreLabel token : questionAnnot.get(TokensAnnotation.class)) {
			thisQuestionTokenized += token.getString(TextAnnotation.class)+ " ";
		}

		// build mega ngrams for question
		List<String> thisQuestionNGrams = DiscourseUtils.generateNGrams(thisQuestionTokenized);
		
		
		// pipe question, documents through preprocessor to get matching tokenizations
		List<String> processedDocument = new ArrayList<String>();
		List<Integer> matchCounts = new ArrayList<Integer>();
		int totalMatches = 0;
		for (String sentence : document) {
			
			String thisSent = sentence;
			thisSent = thisSent.replace('.', ' ');
			thisSent = thisSent.replace('?', ' ');
			thisSent = thisSent.replace('!', ' ');
			thisSent = thisSent + " . ";
			
			Annotation sentAnnot = new Annotation(thisSent);
			pipeline.annotate(sentAnnot);
			
			String thisSentenceTokenized = "";
			for (CoreLabel token : sentAnnot.get(TokensAnnotation.class)) {
				thisSentenceTokenized += token.getString(TextAnnotation.class)+ " ";
			}
			
			// sum number of ngrams matched by sent
			int thisSentMatchCount = 0;
			for (String ngram : thisQuestionNGrams) {
				if (thisSentenceTokenized.contains(ngram)) {
					thisSentMatchCount++;
				}
			}
			matchCounts.add(thisSentMatchCount);
			totalMatches += thisSentMatchCount;
			
			// debug:
			//System.out.println("debug: number of matches for question: "+thisQuestionTokenized+"\n\t for sentence: "+thisSentenceTokenized+"\n\t: "+thisSentMatchCount);
			
		}

		List<Guess> guesses = new ArrayList<Guess>();
		
		for (int i=0; i<document.size(); i++) {
			guesses.add(new Guess((double) matchCounts.get(i) / (double) totalMatches, i+1));
		}
		
		
		return guesses; 
		
	}
	
	
	
	// for testing only!
	public static void main(String[] args) {
		
		List<String> sampleSents = new ArrayList<String>();
        sampleSents.add("Yesterday, Dr. James Howard ate five hundred dollars worth of deluxe seafood biscuits while he was at the opera.");
        sampleSents.add("");
        sampleSents.add("After devouring all of the munchies, he decided to bake a cake for the local chapter of the Rotary Club.");
        sampleSents.add("The club loved his delicious offering, and decided to dedicate the month of October to him.");
        sampleSents.add("");
        sampleSents.add("");
        sampleSents.add("Dweezil Rickenbacker, long time biscuit afficionado, was not pleased with James' good fortune or his betrayal by his friends in the club.");

        List<String> questions = new ArrayList<String>();
        questions.add("Who ate all of the seafood biscuits?");
        questions.add("To whom was the month of October dedicated?");
        questions.add("When did Dr. James Howard eat the biscuits?");
        questions.add("With what was Dweezil Rickenbacker not pleased?");
        
		BagOfNGramsAnswerFinder answerFinder = new BagOfNGramsAnswerFinder();
		
		for (String question : questions) {
			System.out.println();
			List<Guess> guesses = answerFinder.getAnswerLines(sampleSents, question);
		}
		
		
		
	}
	

}
