package cs2731;

import cs2731.ner.NameAnswerFinder;
import cs2731.ner.RandomNameAnswerFinder;
import java.util.Map;
import cs2731.ner.NamedEntityService;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import static java.lang.System.*;

/**
 *
 */
public class DeanQA
{

	static String rootPath = "";
	static String outputFile = "output.txt";
	
	static PrintWriter writer;
	static List<String> document;
	static List<String> questions;
	static List<Guess> answers;
	
	static Preprocessor preprocessor;

	private DeanQA() {}

	/**
	 * Reads the input file and questions into separate lists.
	 * @param inputFile
	 * @throws IOException 
	 */
	private static void readStory(File story) throws IOException {
		document.clear();
		questions.clear();

		// read the input file into a list
		Scanner input = new Scanner(story);
		out.println("processing " + story.getName());
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.contains("<QUESTIONS>")) {
				break;
			}
			document.add(line);
		}

		// read the questions into a list:
		while (input.hasNextLine()) {
			String line = input.nextLine();
			line = line.substring(line.lastIndexOf(">") + 2);
			questions.add(line);
		}

		input.close();

		// preprocess story before answering questions
		// debug:
		//System.out.println("debug: orig doc: " + document.toString());
		//System.out.println("debug: orig numsents: " + document.size() + "\n");
		int origLen = document.size();
		preprocessDocument();
		StopwordRemover swRem = new StopwordRemover();
		swRem.removeStopwordsFromDocument(document);
		//System.out.println("debug: new doc: " + document.toString());
		//System.out.println("debug: new numsents: " + document.size() + "\n");
		int newLen = document.size();
		if (origLen != newLen) {
			System.out.println("DERP! origLen != newLen !!!");
		}

	}

	/**
	 * Reads the dataset file. 
	 * The first line of the input file will be a directory path and all
	 * subsequent lines will be story filenames. Your Q/A system should
	 * then process each story file in the list from the specified directory.
	 * Answers are written to the output file as 
	 * @param inputFile
	 * @throws IOException 
	 */
	private static void processDataset(String inputFile) throws IOException {
		Scanner input = new Scanner(new File(inputFile));

		// read the root path of the input files:
		rootPath = input.nextLine();
		out.println("root dataset path = " + rootPath);

		while (input.hasNextLine()) {
			String filename = input.nextLine().trim();
			File story = new File(rootPath, filename);

			readStory(story);
			answerQuestions(filename);
		}

		writer.close();
		input.close();
	}

	/**
	 * writes the answer file according to the stupid format.
	 * @param outputFile
	 * @throws IOException 
	 */
	private static void writeAnswers(String inputFile) throws IOException {

		// write the filename
		writer.println();
		writer.printf("<FILE>%s\n\n", new File(inputFile).getName());

		// write each answer
		for (int i = 0; i < answers.size(); i++) {
			Guess answer = answers.get(i);
			writer.printf("<Q_NUMBER>%d\n", i + 1);
			writer.printf("<A_LINE>%d\n", answer.getLine());
			writer.printf("<Q_TXT>%s\n", questions.get(i));
			writer.printf("<A_TXT>%s\n\n", document.get(answer.getLine() - 1));
		}

		writer.println("</FILE>\n");
	}

	/**
	 * Invokes the AnswerFinder on each question and adds the answer line to
	 * the list.
	 */
	private static void answerQuestions(String input) throws IOException {
		answers = new ArrayList<Guess>();
		AnswerFinder bowFinder = new BagOfWordsAnswerFinder();
		AnswerFinder verbFinder = new BagOfVerbsAnswerFinder();
		AnswerFinder lemmaFinder = new BagOfLemmasAnswerFinder();
		AnswerFinder nerFinder = new RandomNameAnswerFinder();
		AnswerFinder nameFinder = new NameAnswerFinder();
		AnswerFinder qtFinder = new QuestionTypeAnswerFinder();
		AnswerFinder tfidfFinder = new TfIdfAnswerFinder();
		QuestionExpander qExp = new QuestionExpander();

		// for each question get a list of possible answers
		List<Guess> guesses = new ArrayList<Guess>();
		for (String question : questions) {

			guesses.clear();
			
			// expand question with synonyms according to wordnet?
			question = qExp.addSynwordsToQuestion(question);

			// get guesses for this question
			// TODO: parallel execution of a number of different strategies:

			guesses.addAll(bowFinder.getAnswerLines(document, question));
			guesses.addAll(lemmaFinder.getAnswerLines(document, question));
			guesses.addAll(nerFinder.getAnswerLines(document, question));
			guesses.addAll(qtFinder.getAnswerLines(document, question));
			guesses.addAll(verbFinder.getAnswerLines(document, question));
			guesses.addAll(nameFinder.getAnswerLines(document, question));
			guesses.addAll(tfidfFinder.getAnswerLines(document, question));

			// combine probabilities from multiple oracles
			guesses = combineGuesses(guesses);

			Collections.sort(guesses);
			Collections.reverse(guesses);
			answers.add(guesses.get(0));

//			answers.addAll(guesses);
		}

		// write the answers to the file
		writeAnswers(input);

		answers.clear();
	}

	/**
	 * Sum probabilities for all guesses for each sentence
	 *
	 * @param guesses
	 * @return
	 */
	static List<Guess> combineGuesses(List<Guess> guesses) {

		Map<Integer, Double> guessMap = new HashMap<Integer, Double>();

		for (Guess guess : guesses) {
			if (!guessMap.containsKey(guess.getLine())) {
				guessMap.put(guess.getLine(), 0.0);
			}
			guessMap.put(guess.getLine(), guessMap.get(guess.getLine()) + guess.getProb());
		}

		List<Guess> combinedGuesses = new ArrayList<Guess>();
		for (Integer lineNum : guessMap.keySet()) {
			combinedGuesses.add(new Guess(guessMap.get(lineNum), lineNum));
		}

		return combinedGuesses;
	}

	static void printUsage() {
		out.println("Usage: DeanQA input_filename outputfile_name");
	}

	/**
	 * TODO:
	 * Here we do any preprocessing
	 * that occurs once per document,
	 * such as coreference resolution.
	 */
	private static void preprocessDocument() {
		document = preprocessor.resolveMentions(document);
	}

	/**
	 * TODO:
	 * Here we do any one-time expensive things,
	 * such as load models, classifiers, etc.
	 */
	private static void initializeModels() {
		NamedEntityService.getInstance();	// loads the model
		preprocessor = new Preprocessor();
	}

	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//SVMAnswerFinder test = new SVMAnswerFinder("./resources/input", "./resources/answerkey.txt");
		// get the input and output file paths  from the command line:
		if (args.length < 1) {
			out.println("too few arguments: missing input-file");
			printUsage();
			exit(1);
		}

		if (args.length < 2) {
			out.println("too few arguments: missing output-file");
			printUsage();
			exit(1);
		}

		rootPath = args[0];
		outputFile = args[1];

		// do any one-time expensive initialization
		initializeModels();

		writer = new PrintWriter(outputFile);
		document = new ArrayList<String>();
		questions = new LinkedList<String>();

		processDataset(rootPath);
	}
}
