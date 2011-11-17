
package cs2731;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Preprocesses an input document using the Stanford nlp tools.
 * 
 *
 * @author conrada@cs.pitt.edu
 */
public class Preprocessor {


    // pipeline which performs annotation actions
    StanfordCoreNLP pipeline;

    public Preprocessor() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Replaces all mentions of an entity with the most representative mention. 
     * 
     * @param sentencesOrig
     * @return
     */
    public List<String> resolveMentions(List<String> sentencesOrig) {

        String allSents = " ";
        // indices of all blank lines (for rebuilding list form after preprocessing)
        List<Integer> blankLineIndices = new ArrayList<Integer>();
        for (int s=0; s<sentencesOrig.size(); s++) {
            String thisSentOrig = sentencesOrig.get(s).trim();
            // if no proper ending char, add one so that stanford doesn't mangle sentence splits
            if (thisSentOrig.length() > 3 &&
                    (!thisSentOrig.substring(thisSentOrig.length()-1).equals(".") &&
                    !thisSentOrig.substring(thisSentOrig.length()-1).equals("!") &&
                    !thisSentOrig.substring(thisSentOrig.length()-1).equals("?") &&
                    !thisSentOrig.substring(thisSentOrig.length()-2,thisSentOrig.length()-1).equals(".") &&
                    !thisSentOrig.substring(thisSentOrig.length()-2,thisSentOrig.length()-1).equals("!") &&
                    !thisSentOrig.substring(thisSentOrig.length()-2,thisSentOrig.length()-1).equals("?") )) {
                thisSentOrig += ".";
            }
            allSents += thisSentOrig+"\n";
            if (sentencesOrig.get(s).length() < 3) {
                blankLineIndices.add(s);
            }
        }

        // debug
        //System.out.println("debug: number of blank lines detected: "+blankLineIndices.size());

        Annotation document = new Annotation(allSents);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);

        Object[] corefChains = graph.values().toArray();


        // build sentences
        // NOTE: new approach: find mapping from Stanford sents to orig sents.
        List<String> sentencesNew = new ArrayList<String>(sentencesOrig.size());
        for (CoreMap sentence : sentences) {
            String thisSent = "";
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                thisSent += token.get(TextAnnotation.class)+" ";
            }
            thisSent += "\n";
            sentencesNew.add(thisSent);
        }


        // find mapping from sentencesNew to sentencesOrig
        Map<Integer, Integer> newToOrigMap = new HashMap<Integer, Integer>();
        int origIndex = 0;
        int newIndex = 0;
        int maxDist = 3;
        String[] possOrigMatches;
        Integer[] possOrigTokenCounts;
        for (CoreMap sentence : sentences) {

            possOrigMatches = new String[maxDist*2+1];

            int possOrigIndex = 0;
            for (int i=origIndex-maxDist; i<=origIndex+maxDist; i++) {

                if (i >= 0 && i < sentencesOrig.size()) {
                    possOrigMatches[possOrigIndex] = sentencesOrig.get(i);
                } else {
                    possOrigMatches[possOrigIndex] = "";
                }

                possOrigIndex++;

            }

            // for each possible match, see how many tokens from newSent are
            //  contained in possOldSent
            possOrigTokenCounts = new Integer[maxDist*2+1];
            for (int s=0; s<possOrigMatches.length; s++) {
                possOrigTokenCounts[s] = 0;
                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                    String word = token.get(TextAnnotation.class);
                    if (possOrigMatches[s].contains(word)) {
                        possOrigTokenCounts[s]++;
                    }

                }

            }

            // find poss match which matches most tokens
            int bestMatchIndex = 0;
            int bestMatchCount = 0;
            for (int s=0; s<possOrigMatches.length; s++) {

                if (possOrigTokenCounts[s] > bestMatchCount) {
                    bestMatchCount = possOrigTokenCounts[s];
                    bestMatchIndex = s;
                }

            }

            // assign mapping, adjust indices
            origIndex += (bestMatchIndex - maxDist);

            // debug
            System.out.println("debug: mapping newSent "+newIndex+", \""+sentencesNew.get(newIndex)+"\" to oldSent "+origIndex+", \""+sentencesOrig.get(origIndex));

            newToOrigMap.put(newIndex, origIndex);

            
            origIndex++;
            newIndex++;

        }


        // replace mentions with best form
        for (int i=0; i<corefChains.length; i++) {

            CorefChain corefChain = (CorefChain)corefChains[i];

            if (corefChain.getCorefMentions().size() > 1) {

                String repMention = corefChain.getRepresentativeMention().mentionSpan;
                // debug
                //System.out.println("debug: repMention.toString(): "+repMention);

                for (CorefChain.CorefMention corefMention : corefChain.getCorefMentions()) {

                    // debug
                    //System.out.println("debug: corefMention.toString(): "+corefMention.mentionSpan);
                    //System.out.println("debug: owning sentence: "+sentencesNew.get(corefMention.sentNum-1));

                    //String sent = sentencesNew.remove(corefMention.sentNum-1);
                    //sent = sent.replace(" "+corefMention.mentionSpan+" ", " "+repMention+" ");
                    //sentencesNew.add(corefMention.sentNum-1, sent);

                    int origSentNum = newToOrigMap.get(corefMention.sentNum-1);
                    String sent = sentencesOrig.remove(origSentNum);
                    sent = sent.replace(" "+corefMention.mentionSpan+" ", " "+repMention+" ");
                    sentencesOrig.add(origSentNum, sent);

                }

            }
            

        }

        // insert blanks
        // NOTE: don't bother anymore?
        for (Integer blankIndex : blankLineIndices) {
            if (blankIndex > sentencesNew.size()) {
                sentencesNew.add("\n");
            } else {
                sentencesNew.add(blankIndex, "\n");
            }
        }
        
        //return sentencesNew;
        return sentencesOrig;

    }



    
    public void printAnnotations(List<String> sentencesOrig) {

        String allSents = "";
        // indices of all blank lines (for rebuilding list form after preprocessing)
        List<Integer> blankLineIndices = new ArrayList<Integer>();
        for (int s=0; s<sentencesOrig.size(); s++) {
            allSents += sentencesOrig.get(s)+"\n";
            if (sentencesOrig.get(s).length() < 3) {
                blankLineIndices.add(s);
            }
        }

        System.out.println("number of blank lines detected: "+blankLineIndices.size());

        Annotation document = new Annotation(allSents);
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        // debug

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);

                System.out.println("word="+word+", pos="+pos+", ne="+ne);

            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);

            System.out.println("tree="+tree.toString());

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

            System.out.println("dependencies="+dependencies.toString());

        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefChainAnnotation.class);

        System.out.println("graph="+graph.toString());

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

        Preprocessor preproc = new Preprocessor();
        //preproc.printAnnotations(sampleSents);

        List<String> sampleSentsResolved = preproc.resolveMentions(sampleSents);

        System.out.println(sampleSentsResolved.toString());

    }



}
