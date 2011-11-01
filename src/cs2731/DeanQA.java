

package cs2731;

import static java.lang.System.*;

/**
 *
 */
public class DeanQA
{
	static void printUsage() {
		out.println("Usage: DeanQA input_filename outputfile_name");
	}
	
	public static void main(String[] args) {
		
		// get the input and output file paths from the command line:
		if (args.length < 1) {
			out.println("too few arguments: missing input-file");
			printUsage();
		}
		
		if (args.length < 2) {
			out.println("too few arguments: missing output-file");
			printUsage();
		}
		
		
		
	}
}
