

package cs2731;

import java.util.List;
import java.io.File;
import java.io.IOException;
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
	
	static void printUsage() {
		out.println("Usage: DeanQA input_filename outputfile_name");
	}
	
	
	
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

		// read file into an arraylist
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
		
		while (input.hasNextLine()) {
			questions.add(input.nextLine());
		}
		
		input.close();

		out.println(System.getProperty("user.dir"));
		out.println(lines);
		out.println(questions);
		
	}
}
