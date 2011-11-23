
package cs2731.ner;

import cs2731.AnswerFinder;
import cs2731.Guess;
import cs2731.QuestionType;
import cs2731.QuestionTypeDetector;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Uses stanford named entity recognizer to identify sentences containing names
 * of time, location, organization, person, money, percent, date.
 *
 * @author conrada@cs.pitt.edu
 */
public class RandomNameAnswerFinder implements AnswerFinder {

    // path to trained NER model
    public static String MODEL_PATH = "lib/muc.7class.distsim.crf.ser.gz";


    // random number generator
    private Random rand;

    // NER classifier
    private static AbstractSequenceClassifier classifier = null;


    public RandomNameAnswerFinder() {

        rand = new Random();
        if (classifier == null) {
            classifier = CRFClassifier.getClassifierNoExceptions(MODEL_PATH);
        }

    }

    public List<Guess> getAnswerLines(List<String> document, String question) {
        
        // look for question keyword which will help us to
        List<List<NamedEntity>> docEntityList = findAllEntities(document);

        // compute totals for each question type, totals for each sentence
        EnumMap<NamedEntityType, Integer> entityTypeTotal = new EnumMap<NamedEntityType, Integer>(NamedEntityType.class);
        List<EnumMap<NamedEntityType, Integer>> entityTypeSentList = new ArrayList<EnumMap<NamedEntityType, Integer>>();
        for (List<NamedEntity> sentEntityList : docEntityList) {
            EnumMap<NamedEntityType, Integer> entityTypeSent = new EnumMap<NamedEntityType, Integer>(NamedEntityType.class);
            entityTypeSentList.add(entityTypeSent);
            // we only want to add each mention of an entity once
            HashMap<String, Integer> prevEntityNames = new HashMap<String, Integer>();
            for (NamedEntity entity : sentEntityList) {
                NamedEntityType type = entity.getType();
                String entityName = entity.getEntity();
                if (!prevEntityNames.containsKey(entityName)) {
                	if (!entityTypeTotal.containsKey(type)) {
                		entityTypeTotal.put(type, 0);
                	}
                	if (!entityTypeSent.containsKey(type)) {
                		entityTypeSent.put(type, 0);
                	}
                	entityTypeTotal.put(type, entityTypeTotal.get(type)+1);
                	entityTypeSent.put(type, entityTypeSent.get(type)+1);
                	prevEntityNames.put(entityName, 1);
                }
            }
        }

        // identify type of question
        QuestionType qType = QuestionTypeDetector.getQuestionType(question);
        
        List<Guess> guesses = new ArrayList<Guess>(document.size());

        // for particular types of question, compute probability based on
        // number of entities of that type
        if (qType == QuestionType.WHEN) {
            // look for instances of "time", "date"
            int instanceTotal = 0;
            if (entityTypeTotal.containsKey(NamedEntityType.TIME)) {
                instanceTotal += entityTypeTotal.get(NamedEntityType.TIME);
            }
            if (entityTypeTotal.containsKey(NamedEntityType.DATE)) {
                instanceTotal += entityTypeTotal.get(NamedEntityType.DATE);
            }

            if (instanceTotal == 0) {

                for (int s = 0; s < docEntityList.size(); s++) {
                    guesses.add(new Guess(1.0 / docEntityList.size(), s + 1));
                }

            } else {

                // probability of sentence == # of instances present / # of total instances
                for (int s = 0; s < docEntityList.size(); s++) {
                    EnumMap<NamedEntityType, Integer> entityTypeSent = entityTypeSentList.get(s);
                    int instanceSent = 0;
                    if (entityTypeSent.containsKey(NamedEntityType.TIME)) {
                        instanceSent += entityTypeSent.get(NamedEntityType.TIME);
                    }
                    if (entityTypeSent.containsKey(NamedEntityType.DATE)) {
                        instanceSent += entityTypeSent.get(NamedEntityType.DATE);
                    }

                    double sentProb = (double) instanceSent / (double) instanceTotal;
                    guesses.add(new Guess(sentProb, s + 1));

                }

            }

        } else if (qType == QuestionType.WHERE) {
            // look for instances of "location"
            int instanceTotal = 0;
            if (entityTypeTotal.containsKey(NamedEntityType.LOCATION)) {
                instanceTotal += entityTypeTotal.get(NamedEntityType.LOCATION);
            }

            if (instanceTotal == 0) {

                for (int s = 0; s < docEntityList.size(); s++) {
                    guesses.add(new Guess(1.0 / docEntityList.size(), s + 1));
                }

            } else {

                // probability of sentence == # of instances present / # of total instances
                for (int s = 0; s < docEntityList.size(); s++) {
                    EnumMap<NamedEntityType, Integer> entityTypeSent = entityTypeSentList.get(s);
                    int instanceSent = 0;
                    if (entityTypeSent.containsKey(NamedEntityType.LOCATION)) {
                        instanceSent += entityTypeSent.get(NamedEntityType.LOCATION);
                    }

                    double sentProb = (double) instanceSent / (double) instanceTotal;
                    guesses.add(new Guess(sentProb, s + 1));

                }

            }

        } else if (qType == QuestionType.WHO) {
            // look for instances of "person", "organization"?
            int instanceTotal = 0;
            if (entityTypeTotal.containsKey(NamedEntityType.PERSON)) {
                instanceTotal += entityTypeTotal.get(NamedEntityType.PERSON);
            }
            if (entityTypeTotal.containsKey(NamedEntityType.ORGANIZATION)) {
                instanceTotal += entityTypeTotal.get(NamedEntityType.ORGANIZATION);
            }

            if (instanceTotal == 0) {

                for (int s = 0; s < docEntityList.size(); s++) {
                    guesses.add(new Guess(1.0 / docEntityList.size(), s + 1));
                }

            } else {

                // probability of sentence == # of instances present / # of total instances
                for (int s = 0; s < docEntityList.size(); s++) {
                    EnumMap<NamedEntityType, Integer> entityTypeSent = entityTypeSentList.get(s);
                    int instanceSent = 0;
                    if (entityTypeSent.containsKey(NamedEntityType.PERSON)) {
                        instanceSent += entityTypeSent.get(NamedEntityType.PERSON);
                    }
                    if (entityTypeSent.containsKey(NamedEntityType.ORGANIZATION)) {
                        instanceSent += entityTypeSent.get(NamedEntityType.ORGANIZATION);
                    }

                    double sentProb = (double) instanceSent / (double) instanceTotal;
                    guesses.add(new Guess(sentProb, s + 1));

                }

            }
            
        } else if (qType == QuestionType.HOW_MUCH) {
            // look for instances of "money"
            int instanceTotal = 0;
            if (entityTypeTotal.containsKey(NamedEntityType.MONEY)) {
                instanceTotal += entityTypeTotal.get(NamedEntityType.MONEY);
            }

            if (instanceTotal == 0) {

                for (int s = 0; s < docEntityList.size(); s++) {
                    guesses.add(new Guess(1.0 / docEntityList.size(), s + 1));
                }

            } else {

                // probability of sentence == # of instances present / # of total instances
                for (int s = 0; s < docEntityList.size(); s++) {
                    EnumMap<NamedEntityType, Integer> entityTypeSent = entityTypeSentList.get(s);
                    int instanceSent = 0;
                    if (entityTypeSent.containsKey(NamedEntityType.MONEY)) {
                        instanceSent += entityTypeSent.get(NamedEntityType.MONEY);
                    }

                    double sentProb = (double) instanceSent / (double) instanceTotal;
                    guesses.add(new Guess(sentProb, s + 1));

                }

            }

        } else if (qType == QuestionType.HOW_MANY) {
            
            // set equal prob on each sent?
            for (int s=0; s<docEntityList.size(); s++) {

                guesses.add(new Guess(1.0/docEntityList.size(), s+1));

            }

        } else {

            // set equal prob on each sent?
            for (int s=0; s<docEntityList.size(); s++) {

                guesses.add(new Guess(1.0/docEntityList.size(), s+1));

            }

        }


        // debug
        //System.out.println("debug: for question \"" + question + "\", potential sentences:");
        Collections.sort(guesses);
        Collections.reverse(guesses);
        for (Guess guess : guesses) {
            if (guess.getProb() > 1.0/guesses.size()) {
                //System.out.println("debug:\tpotential answer: prob="+guess.getProb()+", sent=\""+document.get(guess.getLine()-1)+"\"");
            }
        }

        return guesses;

    }

    /**
     * Identifies all named entities in a document and returns entities in a
     * 2d list. d1 = sentence, d2 = all NamedEntities in sentence .
     *
     * @param document list of sentences in document
     * @return list containing all NamedEntities in document
     */
    public List<List<NamedEntity>> findAllEntities(List<String> document) {

        // parse all lines, keeping track of entities for each line

        // stores all named entities for each sentence in the document
        List<List<NamedEntity>> docEntityList = new ArrayList<List<NamedEntity>>(document.size());

        // parse each sentence, storing all named entities
        for (String sentence : document) {
            docEntityList.add(findEntities(sentence));
        }

        return docEntityList;

    }

    public List<NamedEntity> findEntities(String sentence) {

        // pass string through classifier to identify named entities
        String parsedSent = classifyStringXml(sentence);
        // debug
        //System.out.println("debug: ner-parsed sentence: "+parsedSent);

        // process resulting XML
        try {

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(new StringReader("<sent>"+parsedSent+"</sent>"));
            Document doc = db.parse(is);

            ArrayList<NamedEntity> namedEntityList = new ArrayList<NamedEntity>();

            // TIME, LOCATION, ORGANIZATION, PERSON, MONEY, PERCENT, DATE
            NodeList timeNodes = doc.getElementsByTagName("TIME");
            for (int i=0; i<timeNodes.getLength(); i++) {
                Element element = (Element)timeNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.TIME, name));
                }
            }

            NodeList locationNodes = doc.getElementsByTagName("LOCATION");
            for (int i=0; i<locationNodes.getLength(); i++) {
                Element element = (Element)locationNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.LOCATION, name));
                }
            }

            NodeList organizationNodes = doc.getElementsByTagName("ORGANIZATION");
            for (int i=0; i<organizationNodes.getLength(); i++) {
                Element element = (Element)organizationNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.ORGANIZATION, name));
                }
            }

            NodeList personNodes = doc.getElementsByTagName("PERSON");
            for (int i=0; i<personNodes.getLength(); i++) {
                Element element = (Element)personNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.PERSON, name));
                }
            }

            NodeList moneyNodes = doc.getElementsByTagName("MONEY");
            for (int i=0; i<moneyNodes.getLength(); i++) {
                Element element = (Element)moneyNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.MONEY, name));
                }
            }

            NodeList percentNodes = doc.getElementsByTagName("PERCENT");
            for (int i=0; i<percentNodes.getLength(); i++) {
                Element element = (Element)percentNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.PERCENT, name));
                }
            }

            NodeList dateNodes = doc.getElementsByTagName("DATE");
            for (int i=0; i<dateNodes.getLength(); i++) {
                Element element = (Element)dateNodes.item(i);
                Node child = element.getFirstChild();
                if (child instanceof CharacterData) {
                    CharacterData cd = (CharacterData) child;
                    String name = cd.getData();
                    namedEntityList.add(new NamedEntity(NamedEntityType.DATE, name));
                }
            }

            return namedEntityList;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.err.println("error building xml parser for document "+parsedSent);
        } catch (SAXException e) {
            e.printStackTrace();
            System.err.println("error parsing document "+parsedSent);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error parsing document "+parsedSent);
        }
        
        return null;
        
    }

    public String classifyString(String rawString) {
        return classifier.classifyToString(rawString);
    }

    /**
     * Returns rawString with XML markup identifying named entities. Tags:
     * <ORGANIZATION>
     * <DATE>
     * <PERSON>
     * ...
     *
     * @param rawString string in which to identify named entities
     * @return rawString with XML markup identifying named entities
     */
    public String classifyStringXml(String rawString) {
        return classifier.classifyWithInlineXML(rawString);
    }



    // for testing only!
    public static void main(String[] args) {

        /*
        // test 1: get classifier working
        RandomNameAnswerFinder rnaf = new RandomNameAnswerFinder();
        String testDoc = "The fate of Lehman Brothers, the beleaguered investment bank, hung in the balance on Sunday as Federal Reserve officials and the leaders of major financial institutions continued to gather in emergency meetings trying to complete a plan to rescue the stricken bank.  Several possible plans emerged from the talks, held at the Federal Reserve Bank of New York and led by Timothy R. Geithner, the president of the New York Fed, and Treasury Secretary Henry M. Paulson Jr.";
        System.out.println(testDoc);
        System.out.println(rnaf.classifyString(testDoc));
        System.out.println(rnaf.classifyStringXml(testDoc));
        //*/

        ///*
        // test 2: get xml parser working correctly
        RandomNameAnswerFinder rnaf = new RandomNameAnswerFinder();
        String testDoc = "The fate of Lehman Brothers, the beleaguered investment bank, hung in the balance on Sunday as Federal Reserve officials and the leaders of major financial institutions continued to gather in emergency meetings trying to complete a plan to rescue the stricken bank.  Several possible plans emerged from the talks, held at the Federal Reserve Bank of New York and led by Timothy R. Geithner, the president of the New York Fed, and Treasury Secretary Henry M. Paulson Jr.";
        System.out.println(testDoc);
        System.out.println(rnaf.classifyStringXml(testDoc));
        System.out.println("recognized named entities:");
        List<NamedEntity> namedEntityList = rnaf.findEntities(testDoc);
        for (NamedEntity entity : namedEntityList) {
            System.out.println("\t"+entity.toString());
        }
        //*/

        ///*
        // test 3: get document parser working correctly
        //RandomNameAnswerFinder rnaf = new RandomNameAnswerFinder();
        List<String> testDocLines = new ArrayList<String>();
        testDocLines.add("The fate of Lehman Brothers, the beleaguered investment bank, hung in the balance on Sunday as Federal Reserve officials and the leaders of major financial institutions continued to gather in emergency meetings trying to complete a plan to rescue the stricken bank.");
        testDocLines.add("Several possible plans emerged from the talks, held at the Federal Reserve Bank of New York and led by Timothy R. Geithner, the president of the New York Fed, and Treasury Secretary Henry M. Paulson Jr.");

        String[] sampleQuestions = {
            "Who is the queen of Denmark?",
            "Why are kittens so adorable?",
            "How should we take over the world with cats?",
            "If I want to take over the world, how many cats do I need?",
            "When did all the dinosaurs decide to nap?",
            "What form will the ultra-cat take when it decends from outer space?",
            "Where did my foot land?",
            "How old should I be before I adopt a velociraptor?",
            "How much should I pay you for this box of cats?",
        };

        for (String question : sampleQuestions) {
            List<Guess> guesses = rnaf.getAnswerLines(testDocLines, question);
            System.out.println("question: "+question);
            for (Guess guess  : guesses) {
                System.out.println("\t"+guess.toString());
            }
        }
        //*/
        
    }

}
