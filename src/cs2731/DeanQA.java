

package cs2731;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import static java.lang.System.*;

/**
 *
 */
public class DeanQA
{

	static List<String> document;
	static List<String> questions;
	static List<Integer> answers;
	
	private DeanQA() {}
	
	static void printUsage() {
		out.println("Usage: DeanQA input_filename outputfile_name");
	}

	/**
	 * Reads the input file and questions into separate lists.
	 * @param inputFile
	 * @throws IOException 
	 */
	private static void readDocuemnt(String inputFile) throws IOException {
		
		// read the input file into a list
		Scanner input = new Scanner(new File(inputFile));
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
			questions.add(input.nextLine());
		}
		
		input.close();
	}
	
	/**
	 * writes the answer file according to the stupid format.
	 * @param outputFile
	 * @throws IOException 
	 */
	private static void writeAnswers(String inputFile, String outputFile) throws IOException {
		
		PrintWriter writer = new PrintWriter(outputFile);
		
		// write the filename
		writer.printf("<FILE>%s\n\n", new File(inputFile).getName());

		// write each answer
		for (int i=0; i < answers.size(); i++) {
			int answerLine = answers.get(i);
			writer.printf("<Q_NUMBER>%d\n",i + 1);
			writer.printf("<A_LINE>%d\n", answerLine);
			writer.printf("<Q_TXT>%s\n", questions.get(i));
			writer.printf("<A_TXT>%s\n\n", document.get(answerLine));
		}
		
		writer.println("</FILE>");
		writer.close();
	}
	
	/**
	 * Invokes the AnswerFinder on each question and adds the answer line to
	 * the list.
	 */
	private static void answerQuestions() {
		answers = new LinkedList<Integer>();
		AnswerFinder oracle = new RandomAnswerFinder();
		for (String question: questions) {
			int answerLine = oracle.getAnswerLine(document, question);
			answers.add(answerLine);
		}
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

		readDocuemnt(args[0]);
		answerQuestions();
		writeAnswers(args[0], args[1]);
		
	}
}
