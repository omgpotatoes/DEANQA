
package cs2731;

import cs2731.ner.NamedEntityType;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;

import static cs2731.ner.NamedEntityType.*;

public class Utils
{

	public static boolean containsOnlyWhitespace(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}
	

	public static boolean containsOnlyLetters(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Given the text of a question, return a set of NamedEntity types
	 * to look for.
	 * @param question
	 * @return 
	 */
	public static Set<NamedEntityType> getAnswerTypes(String question) {
		return getAnswerTypes(QuestionTypeDetector.getQuestionType(question));
	}
	
	/**
	 * Given a question type, return a set of NamedEntity types
	 * to look for.
	 * @param question
	 * @return 
	 */
	public static Set<NamedEntityType> getAnswerTypes(QuestionType type) {
		switch (type) {
			case WHEN:
				return EnumSet.of(TIME, DATE);
			case WHO:
				return EnumSet.of(PERSON, ORGANIZATION);
			case WHERE:
				return EnumSet.of(LOCATION);

			case HOW_MANY:
			case HOW_MUCH:
				return EnumSet.of(PERCENT, MONEY);

			// TODO not sure about these:
			case HOW_OLD:

			case HOW:

			case WHAT:

			case WHY:
			case OTHER:
			default:
				return EnumSet.allOf(NamedEntityType.class);
		}
	}
	
	/**
	 * Given a file, this method will return an array list of answers in that file.
	 * Note:  This will break most likely if you give it a file that is not in the correct
	 * format
	 * @param answerFile A file in which the answers are extracted
	 * @return The list of Answers
	 * @throws FileNotFoundException
	 */
	static ArrayList<ArrayList<Answer>> extractAnswers(File answerFile) throws FileNotFoundException {
		ArrayList<ArrayList<Answer>> answers = new ArrayList<ArrayList<Answer>>();
		ArrayList<Answer> tempAnswers = new ArrayList<Answer>();
		Scanner fileInput = new Scanner(answerFile);
		String filename = "";
		int questionNumber;
		int [] answerLines;
		String questionText;
		String [] answerTexts;
		String firstLine; 
		String [] explodedString;
		boolean addFlag = false;
		
		while(fileInput.hasNext()) {
			firstLine = fileInput.nextLine();
			if(firstLine.length() > 7) {
				if(firstLine.substring(0, 6).equals("<FILE>")) {
					if(addFlag) 
						answers.add(tempAnswers);
					else
						addFlag = true;
				
					filename = firstLine.substring(6);
				}
				else if(firstLine.substring(0, 10).equals("<Q_NUMBER>")) {
					questionNumber = Integer.parseInt(firstLine.substring(10));
					
					explodedString = fileInput.nextLine().substring(8).split(",");
					answerLines = new int [explodedString.length];
					
					for(int i = 0; i < explodedString.length; i++) 
						answerLines[i] = Integer.parseInt(explodedString[i]);
					
					questionText = fileInput.nextLine().substring(7);
					
					explodedString = fileInput.nextLine().substring(7).split("  -OR- ");
					answerTexts = explodedString;
					
					tempAnswers.add(new Answer(filename, questionNumber, answerLines, questionText, answerTexts));
				}
			}
		}
		answers.add(tempAnswers);
		return answers;
	}
	
	/**
	 * Given a directory, this method will return an array list of all questions in the files
	 * in that directory
	 * Note:  This will break most likely if you give it a file that is not in the correct
	 * format
	 * @param questionFiles An array of document files
	 * @return The list of Answers
	 * @throws FileNotFoundException
	 */
	static ArrayList<ArrayList<Question>> extractQuestions(File [] questionFiles) throws FileNotFoundException {
		ArrayList<ArrayList<Question>> questions = new ArrayList<ArrayList<Question>>();
		ArrayList<Question> tempQuestions = new ArrayList<Question>();
		Scanner fileInput;
		String filename;
		String inputLine;
		int questionCounter;
		boolean questionSectionFlag;
		boolean addFlag = false;
		
		for(File file : questionFiles) {
			if(addFlag) 
				questions.add(tempQuestions);
			else
				addFlag = true;
			
			tempQuestions = new ArrayList<Question>();
			fileInput = new Scanner(file);
			filename = file.getName();
			questionCounter = 1;
			questionSectionFlag = false;
			
			while(fileInput.hasNext()) {
				inputLine = fileInput.nextLine();
				if(questionSectionFlag && inputLine.startsWith("<")) {
					tempQuestions.add(new Question(filename, inputLine.substring(inputLine.indexOf(">") + 2).trim(), questionCounter));
					questionCounter++;
				}
				else if(inputLine.startsWith("<QUESTIONS>")) {
					questionSectionFlag = true;
				}
			}
		}
		questions.add(tempQuestions);
		return questions;
	}
	
	/**
	 * Given a directory, this method will return an array list of all sentences in the files
	 * in that directory
	 * Note:  This will break most likely if you give it a file that is not in the correct
	 * format
	 * @param documentFiles An array of document files
	 * @return The list of Answers
	 * @throws FileNotFoundException
	 */
	static ArrayList<ArrayList<Sentence>> extractSentences(File [] documentFiles) throws FileNotFoundException {
		ArrayList<ArrayList<Sentence>> sentences = new ArrayList<ArrayList<Sentence>>();
		ArrayList<Sentence> tempSentences = new ArrayList<Sentence>();
		Scanner fileInput;
		String filename;
		String inputLine;
		int sentenceCounter;
		boolean addFlag = false;
		
		for(File file : documentFiles) {
			if(addFlag) 
				sentences.add(tempSentences);
			else
				addFlag = true;
			
			fileInput = new Scanner(file);
			filename = file.getName();
			sentenceCounter = 1;
			while(fileInput.hasNext()) {
				inputLine = fileInput.nextLine();
				if(inputLine.startsWith("<QUESTIONS>")) 
					break;
				else if(inputLine.trim().length() > 0) {
					tempSentences.add(new Sentence(filename, inputLine.trim(), sentenceCounter));
				}
				sentenceCounter++;
			}
		}
		return sentences;
	}
	
	public static TrainingFileData [] extractAllDataFromFile(File [] documents, File answerKey) throws FileNotFoundException {
		String questions;
		String sentences;
		Scanner documentInput;
		Scanner answerInput;
		String inputLine;
		String filename;
		ArrayList<String> tempAnswers;
		ArrayList<TrainingFileData> tempData = new ArrayList<TrainingFileData>();
		boolean questionSectionFlag;
		int lineCounter = 1;
		
		for(File document : documents) {
			filename = document.getName();
			documentInput = new Scanner(document);
			tempAnswers = new ArrayList<String>();
			answerInput = new Scanner(answerKey);
			questions = "";
			sentences = "";
			questionSectionFlag = false;
			while(documentInput.hasNext()) {
				inputLine = documentInput.nextLine().trim();
				if(questionSectionFlag && inputLine.startsWith("<")) 
					questions += inputLine.substring(inputLine.indexOf(">") + 2) + "\n";
				else if(inputLine.startsWith("<QUESTIONS>"))
					questionSectionFlag = true;
				else if(inputLine.trim().length() > 0) {
					if(lineCounter < 3) 
						inputLine += ".";
					sentences += inputLine + "\n";
				}
				lineCounter++;
			}
			do {
				inputLine = answerInput.nextLine();
			}while(!inputLine.startsWith("<FILE>" + filename));
			
			inputLine = answerInput.nextLine();
			while(!inputLine.startsWith("</FILE>")) {
				answerInput.nextLine();
				answerInput.nextLine();
				inputLine = answerInput.nextLine();
				tempAnswers.add(inputLine.substring(inputLine.indexOf(">") + 1).trim());
				answerInput.nextLine();
				inputLine = answerInput.nextLine();
			}
			String [] tempArray = new String [tempAnswers.size()];
			tempAnswers.toArray(tempArray);
			tempData.add(new TrainingFileData(sentences, questions, tempArray));
		}
		TrainingFileData [] tempDataArray = new TrainingFileData[tempData.size()];
		tempData.toArray(tempDataArray);
		return tempDataArray;
	}
}
