
package cs2731;

import java.util.Scanner;

/**
 * Detects the type of a given question, using types given in QuestionType.
 *
 * @author conrada@cs.pitt.edu
 */
public class QuestionTypeDetector {



    public static QuestionType getQuestionType(String question) {

        // simple version: just look for keywords (start with most specific)
        String cleanQuestion = question.toLowerCase().trim();

        if (cleanQuestion.contains("how much")) {
            return QuestionType.HOW_MUCH;
        } else if (cleanQuestion.contains("how many")) {
            return QuestionType.HOW_MANY;
        } else if (cleanQuestion.contains("how old")) {
            return QuestionType.HOW_OLD;
        } else if (cleanQuestion.contains("who")) {
            return QuestionType.WHO;
        } else if (cleanQuestion.contains("what")) {
            return QuestionType.WHAT;
        } else if (cleanQuestion.contains("where")) {
            return QuestionType.WHERE;
        } else if (cleanQuestion.contains("when")) {
            return QuestionType.WHEN;
        } else if (cleanQuestion.contains("why")) {
            return QuestionType.WHY;
        } else if (cleanQuestion.contains("how")) {
            return QuestionType.HOW;
        } else if (cleanQuestion.contains("which")) {
            return QuestionType.WHICH;
        } else {
            return QuestionType.OTHER;
        }

    }

    // for testing only
    public static void main(String[] args) {

        // test 1: bunch of sample questions
        String[] sampleQuestions = {
            "Who is the queen of Denmark?",
            "Why are kittens so adorable?",
            "How should we take over the world with cats?",
            "Why did the kitten cross the road?",
            "If I want to take over the world, how many cats do I need?",
            "When did all the dinosaurs decide to nap?",
            "What form will the ultra-cat take when it decends from outer space?",
            "Who will eat at the barbeque after Hitler has taken all the peacocks?",
            "Where did my foot land?",
            "How many dinosaur-cats does it take to screw in a lightbulb?",
            "How old should I be before I adopt a velociraptor?",
            "Why won't my cat let me adopt a velociraptor?",
            "If I were a cat, what would be my favorite part of humans to nom upon?",
            "Where did that TARDIS come from, kitty?",
            "When will The Doctor bring me my cat-bearded Picard?",
            "How much should I pay you for this box of cats?",
            "If cat-bearded Picard agrees to marry me, when should we set the wedding for?",
        };

        for (String question : sampleQuestions) {
            System.out.println("question_type="+getQuestionType(question)+", question=\""+question+"\"");
        }
        
    }

}
