

package cs2731;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import static java.lang.System.*;

/**
 *
 */
public class DeanQA
{

	static List<String> lines;
	static List<String> questions;
	static RandomAnswerFinder randomAnswerer;
	
	static void printUsage() {
		out.println("Usage: DeanQA input_filename outputfile_name");
	}
	
	
	
	public static void main(String[] args) throws IOException {
		int answerLine, questionNumber = 1;
		String questionWithTag;
		
		// get the input and output file paths from the command line:
		if (args.length < 1) {
			out.println("too few arguments: missing input-file");
			printUsage();
		}
		
		if (args.length < 2) {
			out.println("too few arguments: missing output-file");
			printUsage();
		}

		// read file into an list
		Scanner input = new Scanner(new File(args[0]));
		lines = new ArrayList<String>();
		questions = new ArrayList<String>();
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if (line.contains("<QUESTIONS>")) {
				break;
			}
			lines.add(line);
		}
		
		//Strip off the <Q?> tag from the question
		while (input.hasNextLine()) {
			questionWithTag = input.nextLine();
			questions.add(questionWithTag.substring(questionWithTag.lastIndexOf(">") + 2));
		}
		
		input.close();

		// finds the answers to the questions by getting them from randomAnswerer
		// answer lines start counting from 1! 
		randomAnswerer = new RandomAnswerFinder();
		PrintWriter output = new PrintWriter(new FileWriter(args[1]));
		output.println("<FILE>" + args[1].substring(args[1].lastIndexOf("/") + 1));
		
		for(String question : questions) {
			answerLine = randomAnswerer.getAnswerLine(lines, question);
			output.println("<Q_NUMBER>" + questionNumber);
			output.println("<A_LINE>" + (answerLine + 1));
			output.println("<Q_TXT>" + question);
			output.println("<A_TXT>" + lines.get(answerLine));
			output.println("");
			questionNumber++;
		}
		output.close();
		
	}
}
