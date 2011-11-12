
package cs2731;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Utils
{
	/**
	 * 
	 * @param str
	 * @return 
	 */
	static boolean containsOnlyWhitespace(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param str
	 * @return 
	 */
	static boolean containsOnlyLetters(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Given a file, this method will return an array list of answers in that file.
	 * Note:  This will break most likely if you give it a file that is not in the correct
	 * format
	 * @param answerFile A file in which the answers are extracted
	 * @return The list of Answers
	 * @throws FileNotFoundException
	 */
	static ArrayList<Answer> extractAnswers(File answerFile) throws FileNotFoundException {
		ArrayList<Answer> answers = new ArrayList<Answer>();
		Scanner fileInput = new Scanner(answerFile);
		String filename = "";
		int questionNumber;
		int [] answerLines;
		String questionText;
		String [] answerTexts;
		String firstLine; 
		String [] explodedString;
		
		while(fileInput.hasNext()) {
			firstLine = fileInput.nextLine();
			if(firstLine.length() > 7) {
				if(firstLine.substring(0, 6).equals("<FILE>")) 
					filename = firstLine.substring(6);
				else if(firstLine.substring(0, 10).equals("<Q_NUMBER>")) {
					questionNumber = Integer.parseInt(firstLine.substring(10));
					
					explodedString = fileInput.nextLine().substring(8).split(",");
					answerLines = new int [explodedString.length];
					
					for(int i = 0; i < explodedString.length; i++) 
						answerLines[i] = Integer.parseInt(explodedString[i]);
					
					questionText = fileInput.nextLine().substring(7);
					
					explodedString = fileInput.nextLine().substring(7).split("  -OR- ");
					answerTexts = explodedString;
					
					answers.add(new Answer(filename, questionNumber, answerLines, questionText, answerTexts));
				}
			}
		}
		
		return answers;
	}
	
}
