package cs2731.discourse;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import cs2731.AnswerFinder;
import cs2731.Guess;
import cs2731.QuestionType;
import cs2731.QuestionTypeDetector;


/**
 * Generates answer guesses based on the discourse relations in which 
 * each sentence participates, the question type, and the prior 
 * probabilities for each question type to be answered by each 
 * type of discourse relation.
 * 
 * @author conrada@cs.pitt.edu
 *
 */
public class DiscourseAnswerFinder implements AnswerFinder {
	
	//public static double WEIGHT = 0.10;
	public static double WEIGHT = 1.00;   // weights now handled in DEANQA
	
	protected static EnumMap<QuestionType, HashMap<String, Double>> answerProbs = null;
	protected String nextDocPath;
	protected boolean nextDocPathSet;
	
	public DiscourseAnswerFinder() {
		if (answerProbs == null) {
			answerProbs = DiscourseUtils.unserializeProbs();
		}
		nextDocPath = "";
		nextDocPathSet = false;
	}
	
	public void setNextDoc(String nextDocPath) {
		this.nextDocPath = nextDocPath;
		nextDocPathSet = true;
	}
	
	/**
	 * note: must set nextDoc before calling this method, so that we can find discourse rels!
	 * (or alternatively, re-execute the discourse parser on each execution, but that would be *very* slow)
	 */
	@Override
	public List<Guess> getAnswerLines(List<String> document, String question) {
		
		// get type of question
		QuestionType questionType = QuestionTypeDetector.getQuestionType(question);
		
		// get annotated version of document
		if (!nextDocPathSet) {
			System.out.println("err: DiscourseAnswerFinder: nextDocPath not set, cannot find discourse information!");
			return null;
		}
		String annotatedFile = DiscourseUtils.readDoc(nextDocPath + ".annot");
		List<DiscourseAnnotation> annotations = DiscourseUtils.buildDiscourseAnnots(annotatedFile);
		
		// match up annotated lines with lines in original
		DiscourseUtils.integrateSentIndices(document, annotations);
		
		// set guess probability for each line equals to the prior probs
		List<Guess> guesses = new ArrayList<Guess>();
		
		for (int i=0; i<document.size(); i++) {
			
			List<String> thisSentDiscourseRoles = new ArrayList<String>();
			boolean hasRoles = false;
			for (int a=0; a<annotations.size(); a++) {
				if (annotations.get(a).getArg1SentIndex() == i) {
					thisSentDiscourseRoles.add(annotations.get(a).getType()+"-Arg1");
					hasRoles = true;
				}
				if (annotations.get(a).getArg2SentIndex() == i) {
					thisSentDiscourseRoles.add(annotations.get(a).getType()+"-Arg2");
					hasRoles = true;
				}
			}
			if (!hasRoles) {
				thisSentDiscourseRoles.add("no-rel");
			}
			
			// probability of guess for sentence will be average of probs of
			//  all discourse roles this sent participates in
			//  (not technically correct; should be joint probability, but 
			//  this should be close enough for our purposes)
			double prob = 0.0;
			for (String discourseRole : thisSentDiscourseRoles) {
				try {
					prob += (double) answerProbs.get(questionType).get(discourseRole);
				} catch (NullPointerException e) {
					// may happen if particular role isn't seen for particular questionType
				}
			}
			prob /= (double) thisSentDiscourseRoles.size();
			prob *= WEIGHT;
			guesses.add(new Guess(prob, i+1));
			
		}
		
		return guesses;
	}

}
