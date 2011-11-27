

package cs2731.discourse;

import cs2731.Sentence;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Various important functions for doing stuff with discourse.
 *
 * @author conrada@cs.pitt.edu
 */
public class DiscourseUtils {

    public static final String DATASET_PATH_ROOT = "resources/";

    public static final String DISCOURSE_EXEC = "lib\\pdtb-parser-v110102\\src\\parse.rb";

    public static String executeDiscourseParser(String filePath) {

        String outputString = "";
        String errString = "";

        try {

            Process p = Runtime.getRuntime().exec("ruby "+DISCOURSE_EXEC+" "+filePath);

            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line;
            while ((line = output.readLine()) != null) {
                outputString += line + "\n";
            }
            output.close();

            while ((line = err.readLine()) != null) {
                errString += line + "\n";
            }
            err.close();

            p.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("error executing external discourse parser "+DISCOURSE_EXEC);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("error executing external discourse parser "+DISCOURSE_EXEC);
        }

        if (!errString.equals("")) {
            System.out.println("errors occured during parsing:\n"+errString);
        }

        return outputString;

    }

    public static Scanner docScanner(String file) {

        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new BufferedReader(new FileReader(file)));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("could not read file: " + file + ", exiting");
            System.exit(1);
        }
        return fileScanner;

    }

    public static String readDoc(String file) {

        Scanner fileScanner = docScanner(file);

        String doc = "";
        while (fileScanner.hasNextLine()) {

            Scanner lineScanner = new Scanner(fileScanner.nextLine());
            while (lineScanner.hasNext()) {
                doc += lineScanner.next() + " ";
            }

            lineScanner.close();
            doc += "\n";

        }

        fileScanner.close();

        return doc;

    }

    /**
     * Given an annotated doc, build set of DiscourseAnnotations.
     *
     * @param annotatedDoc string output of discourse parser
     * @return list of discourse annotations
     */
    public static List<DiscourseAnnotation> buildDiscourseAnnots(String annotatedDoc) {

        List<DiscourseAnnotation> annotations = new ArrayList<DiscourseAnnotation>();

        Scanner docScanner = new Scanner(annotatedDoc);

        // map containing all observed annotations
        // key format mirrors text form: [Exp|NonExp]_id
        HashMap<String, DiscourseAnnotation> annotMap = new HashMap<String, DiscourseAnnotation>();

        // map containing current text of all open annotations
        // key format mirrors text form: [Exp|NonExp]_id_[Arg1|Arg2|conn]
        HashMap<String, String> openAnnotMap = new HashMap<String, String>();

        while (docScanner.hasNext()) {

            String token = docScanner.next();
            if (token.length() > 0
                    && (token.substring(0, 1).equals("{"))) {
                // beginning a new annot

                Scanner annotSplitter = new Scanner(token.substring(1));
                annotSplitter.useDelimiter("_");

                String isExp = "";
                int id = -1;
                String spanType = "";
                String relType = "unknown";

                if (annotSplitter.hasNext()) {
                    isExp = annotSplitter.next();
                }
                if (annotSplitter.hasNext()) {
                    id = annotSplitter.nextInt();
                }
                if (annotSplitter.hasNext()) {
                    spanType = annotSplitter.next();
                }
                if (annotSplitter.hasNext()) {
                    relType = annotSplitter.next();
                }

                DiscourseAnnotation thisAnnotation = null;
                if (!annotMap.containsKey(isExp + "_" + id)) {
                    thisAnnotation = new DiscourseAnnotation(isExp, id, DiscourseRelType.valueOf(relType));
                    annotMap.put(isExp + "_" + id, thisAnnotation);
                    annotations.add(thisAnnotation);

                } else {
                    thisAnnotation = annotMap.get(isExp + "_" + id);

                    if (!relType.equals("unknown")) {
                        // debug
                        //System.out.println("debug: setting type for annotation "+thisAnnotation.toString()+": "+relType+"-"+DiscourseRelType.valueOf(relType));
                        thisAnnotation.setType(DiscourseRelType.valueOf(relType));
                    }
                }

                openAnnotMap.put(isExp + "_" + id + "_" + spanType, "");

            } else if (token.length() > 0
                    && (token.substring(token.length() - 1).equals("}"))) {
                // closing an existing annot span

                Scanner annotSplitter = new Scanner(token.substring(0, token.length() - 1));
                annotSplitter.useDelimiter("_");

                String isExp = "";
                int id = -1;
                String spanType = "";
                String relType = "";

                if (annotSplitter.hasNext()) {
                    isExp = annotSplitter.next();
                }
                if (annotSplitter.hasNext()) {
                    id = annotSplitter.nextInt();
                }
                if (annotSplitter.hasNext()) {
                    spanType = annotSplitter.next();
                }
                if (annotSplitter.hasNext()) {
                    relType = annotSplitter.next();
                }

                DiscourseAnnotation thisAnnotation = annotMap.get(isExp + "_" + id);
                String textSpan = openAnnotMap.remove(isExp + "_" + id + "_" + spanType);

                if (spanType.equals("Arg1")) {
                    thisAnnotation.setArg1(textSpan);
                } else if (spanType.equals("Arg2")) {
                    thisAnnotation.setArg2(textSpan);
                } else if (spanType.equals("conn")) {
                    thisAnnotation.setConn(textSpan);
                } else {
                    System.err.println("anomalous span annotation type: " + spanType + ", skipping");
                    continue;
                }

            } else {
                // just an ordinary token, append to all open annotations

                Set<String> openAnnotSet = openAnnotMap.keySet();
                for (String openAnnot : openAnnotSet) {
                    String history = openAnnotMap.get(openAnnot);
                    openAnnotMap.put(openAnnot, history + " " + token);
                }

            }

        }

        return annotations;

    }

    public static List<String> generateNGrams(String text) {

        // split text on whitespace, build ngrams
        // figure out how long text is, so we know how much space
        //  to allocate for array

        Scanner textScanner = new Scanner(text);
        List<String> tokens = new ArrayList<String>();
        while (textScanner.hasNext()) {
            tokens.add(textScanner.next());
        }

        List<String> nGrams = new ArrayList<String>();
        for (int start = 0; start < tokens.size() - 1; start++) {
            for (int end = start + 1; end < tokens.size(); end++) {
                String nGram = "";
                for (int i = 0; i < end - start; i++) {
                    if (i > 0) {
                        nGram += " ";
                    }
                    nGram += tokens.get(start + i);
                }
                nGrams.add(nGram);
            }
        }

        return nGrams;

    }

    /**
     * For each DiscourseAnnotation, map to a sentence as generated by
     * the DatasetBuilder parser.
     *
     * @param sentences
     * @param annotations
     */
    public static void integrateSentIndices(List<String> sentences, List<DiscourseAnnotation> annotations) {

        for (DiscourseAnnotation annotation : annotations) {

            // strategy: look for sentence which matches longest sequence of tokens
            // warning: biased towards longer sentences!


            // arg1:
            List<String> nGramsArg1 = generateNGrams(annotation.getArg1());

            int maxMatchArg1 = -1;
            int maxMatchIndexArg1 = -1;
            for (int i = 0; i < sentences.size(); i++) {
                String sentStr = sentences.get(i);
                int match = 0;
                for (String nGram : nGramsArg1) {
                    if (sentStr.contains(nGram)) {
                        match++;
                    }
                }
                if (match > maxMatchArg1) {
                    maxMatchArg1 = match;
                    maxMatchIndexArg1 = i;
                }
            }

            // debug
            System.out.println("debug: matching arg1 span \n\t\"" + annotation.getArg1() + "\" to sentence \n\t\"" + sentences.get(maxMatchIndexArg1) + "\"");

            // arg2:
            List<String> nGramsArg2 = generateNGrams(annotation.getArg2());

            int maxMatchArg2 = -1;
            int maxMatchIndexArg2 = -1;
            for (int i = 0; i < sentences.size(); i++) {
                String sentStr = sentences.get(i);
                int match = 0;
                for (String nGram : nGramsArg2) {
                    if (sentStr.contains(nGram)) {
                        match++;
                    }
                }
                if (match > maxMatchArg2) {
                    maxMatchArg2 = match;
                    maxMatchIndexArg2 = i;
                }
            }

            // debug
            System.out.println("debug: matching arg2 span \n\t\"" + annotation.getArg2() + "\" to sentence \n\t\"" + sentences.get(maxMatchIndexArg2) + "\"");

            // conn:
            if (!annotation.getConn().equals("")) {
                List<String> nGramsConn = generateNGrams(annotation.getConn());

                int maxMatchConn = -1;
                int maxMatchIndexConn = -1;
                for (int i = 0; i < sentences.size(); i++) {
                    String sentStr = sentences.get(i);
                    int match = 0;
                    for (String nGram : nGramsConn) {
                        if (sentStr.contains(nGram)) {
                            match++;
                        }
                    }
                    if (match > maxMatchConn) {
                        maxMatchConn = match;
                        maxMatchIndexConn = i;
                    }
                }

                // debug
                System.out.println("debug: matching conn span \n\t\"" + annotation.getConn() + "\" to sentence \n\t\"" + sentences.get(maxMatchIndexConn) + "\"");
            }





        }

    }

    // for testing only
    public static void main(String[] args) {

        test1();

    }

    public static void test1() {

        String sent = "This Thanksgiving it is worth taking a moment to be grateful for something nearly 50 million Americans live without : health care .";
        System.out.println("nGrams for sent: " + sent);
        System.out.println(generateNGrams(sent).toString());

        String sampleDocPath = DATASET_PATH_ROOT + "input/1999-W02-5.txt";
        String annotatedDoc = readDoc(sampleDocPath);

        System.out.println("doc " + sampleDocPath + ": \n" + annotatedDoc);

        List<DiscourseAnnotation> discourseAnnots = buildDiscourseAnnots(annotatedDoc);
        System.out.println("discourse annotations: ");
        for (DiscourseAnnotation annotation : discourseAnnots) {
            System.out.println("\t" + annotation.toString());
        }

        // match up
        System.out.println("\n\nmatching test:");
        integrateSentIndices(sampleDocTextSents(), discourseAnnots);

    }

    public static void test2() {

        String sampleDocPath = DATASET_PATH_ROOT + "input/1999-W02-5.txt";

        String parserOutput = executeDiscourseParser(sampleDocPath);

        System.out.println("discourse parser output: "+parserOutput);

    }

    public static List<String> sampleDocTextSents() {

        List<String> sentences = new ArrayList<String>();







        return sentences;

    }
    
}
