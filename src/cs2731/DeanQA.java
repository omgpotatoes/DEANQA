

package cs2731;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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

	static List<String> document;
	static List<String> questions;
	static List<Guess> answers;
	
	private DeanQA() {}
	
	static void printUsage() {
		out.println("Usage: DeanQA input_filename outputfile_name");
	}

	/**
	 * Reads the input file and questions into separate lists.
	 * @param inputFile
	 * @throws IOException 
	 */
	private static void readStory(File story) throws IOException {
		
		// read the input file into a list
		Scanner input = new Scanner(story);
		document = new ArrayList<String>();
		questions = new LinkedList<String>();
		
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
		PrintWriter writer = new PrintWriter(outputFile);
		
		// read the root path of the input files:
		rootPath = input.nextLine();
		out.println("root dataset path = " + rootPath);
		
		while (input.hasNextLine()) {
			String filename = input.nextLine();
			File story = new File(rootPath, filename);
			out.println("processing " + story.getPath());
			
			readStory(story);
			
			answerQuestions();
			
			writeAnswers(writer, filename);
		}
		
		writer.close();
		input.close();
	}
	
	/**
	 * writes the answer file according to the stupid format.
	 * @param outputFile
	 * @throws IOException 
	 */
	private static void writeAnswers(PrintWriter writer, String inputFile) throws IOException {
		
//		PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true));
		
		// write the filename
		writer.println();
		writer.printf("<FILE>%s\n\n", new File(inputFile).getName());

		// write each answer
		for (int i=0; i < answers.size(); i++) {
			Guess answer = answers.get(i);
			writer.printf("<Q_NUMBER>%d\n",i + 1);
			writer.printf("<A_LINE>%d\n", answer);
			writer.printf("<Q_TXT>%s\n", questions.get(i));
			writer.printf("<A_TXT>%s\n\n", document.get(answer.getLine()));
		}
		
		writer.println("</FILE>\n");
		writer.close();
	}
	
	/**
	 * Invokes the AnswerFinder on each question and adds the answer line to
	 * the list.
	 */
	private static void answerQuestions() {
		answers = new LinkedList<Guess>();
		AnswerFinder oracle = new RandomAnswerFinder();
		for (String question: questions) {
//			int answerLine = oracle.getAnswerLines(document, question);
			List<Guess> guesses = oracle.getAnswerLines(document, question);
			
			// TODO use a module to select an answer from the list
			answers.addAll(guesses);
		}
		
		Collections.sort(answers);
		Collections.reverse(answers);
	}
	
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// get the input and output file paths from the command line:
		if (args.length < 1) {
			out.println("too few arguments: missing input-file");
			printUsage();
		}
		
		if (args.length < 2) {
			out.println("too few arguments: missing output-file");
			printUsage();
		}

		rootPath = args[0];
		outputFile = args[1];
		
		processDataset(rootPath);
		
	}
}
