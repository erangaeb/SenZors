package com.score.senzors.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This call implements all the solution of the word wrapping
 * problem with Dynamic Programming approach
 * Mainly contains two function
 *      1. Initialize slack table
 *      2. Find optimal solution(line breaks) of given input texts
 */
public class WordWrap {

    public static int INFINITY = Integer.MAX_VALUE;

    /**
     * Initialize slack table according to the content
     * in input words.
     *
     *            |M| - |Wi|              if i = j
     * S(i, j) =
     *            S(i, j-1) - 1 - |Wj|    otherwise
     *
     * @param words input words
     * @param margin maximum line margin
     *
     * @return slack table(array)
     */
    private static int[][] initSlack(String []words, int margin) {
        // slack table is two dimensional Array
        int [][]slack = new int[words.length + 1][words.length + 1];

        // initialize slack
        for (int i=1; i<=words.length; i++) {
            slack[i][i] = margin - words[i - 1].length();

            for (int j=i+1; j<=words.length; j++) {
                slack[i][j] = slack[i][j-1] - words[j - 1].length() - 1;
            }
        }

        return slack;
    }

    /**
     * Find best possible line breaks(fist words of the line) for given n words
     * which minimizing the total badness(slack). We are using previously calculated
     * slack table values to find best solution
     *
     *            0                              if i = 0
     * best(i) =
     *            j = 0 -> i
     *                min{best(j) + S(j + 1, i)}   otherwise
     *
     * @param wordCount length of the words(this can identifies as n)
     * @param slack slack table
     *
     * @return line breaks
     */
    private static int[] findBestLineBreaks(int wordCount, int [][]slack) {
        int []bestValues = new int[wordCount + 1];
        int []lineBreaks = new int[wordCount + 1];
        bestValues[0] = 0;

        for(int i=1; i<=wordCount; i++) {
            int min = INFINITY;
            int tmp;
            int choice = 0;

            // find min cost values, its is the best value
            for (int j=0; j<i; j++) {
                // we not allow negative costs,
                // negative costs considers as infinity
                if (slack[j + 1][i] < 0) {
                    // ignore here
                    tmp = INFINITY;
                } else if(j == wordCount - 1) {
                    // last line cost is 0
                    tmp = 0;
                } else {
                    // rest of the line cost is "min{best(j) + S(j+1, i)}"
                    tmp = bestValues[j] + ((slack[j + 1][i]) * (slack[j + 1][i]) * (slack[j + 1][i]));
                }

                // refine min value
                if (tmp < min) {
                    min = tmp;
                    choice = j;
                }
            }

            bestValues[i] = min;
            lineBreaks[i] = choice;
        }

        return lineBreaks;
    }

    /**
     * Break words into lines according to the line breaks
     * @param lineBreaks best line breaks
     * @param words input words
     *
     * @return best lines
     */
    private static ArrayList<String> findBestLines(int []lineBreaks, String []words) {
        ArrayList<String> lines = new ArrayList<String>();

        int j = words.length;
        while (j>0) {
            int i = lineBreaks[j];

            // find best lines
            String line = "";
            for (String s: Arrays.copyOfRange(words, i, j)) {
                line = line + " " + s;
            }
            lines.add(line.trim());

            j = i;
        }

        Collections.reverse(lines);

        // print best lines
        for(String line : lines) {
            System.out.println(line);
        }

        return lines;
    }

    /**
     * Calculate penalty of a line
     * @param lines best lines
     * @param margin maximum line width
     *
     * @return total cost
     */
    private static int calculateCost(ArrayList<String> lines, int margin) {
        int totalCost = 0;
        for (int i = 0; i<lines.size(); i++) {
            // we ignore penalty of the last line
            if (i!=(lines.size() -1)) {
                int lineCost = margin - lines.get(i).length();
                totalCost += (lineCost * lineCost * lineCost);
            }
        }

        System.out.println("Cost: " + totalCost);

        return totalCost;
    }

    /**
     * Print best solution according to the line breaks and words
     * @param lineBreaks best line breaks
     * @param words input words
     */
    private static void print(int []lineBreaks, String []words) {
        int j = words.length;
        ArrayList<String> lines = new ArrayList<String>();
        while (j>0) {
            int i = lineBreaks[j];

            // concatenate lines and store in a another array
            String line = "";
            for (String s: Arrays.copyOfRange(words, i, j)) {
                line = line + " " + s;
            }
            lines.add(line.trim());

            j = i;
        }

        Collections.reverse(lines);
        int penalty = 0;
        for (int i = 0; i<lines.size(); i++) {
            if (i!=(lines.size() -1)) {
                penalty += getCost(lines.get(i), 15);
            }

            System.out.println(lines.get(i));
        }

        System.out.println("penalty " + penalty);
    }


    private static int getCost(String s, int maxLength) {

        int penalty = maxLength - s.length();

        return penalty*penalty*penalty;
    }


    public static void main(String []args) {
//        //String word = "One could imagine some of these features being contextual";
//        String word = "She is happy but is a blue gal. I am all gone.";
//        //String word = "The university is a state university, with most of its funding coming from the central government via the University Grants Commission(UGC). Therefore, as with all other state universities in Sri Lanka, the UGC recommends its vice-chancellor for appointment by thePresident of Sri Lanka and makes appointments of its administrative staff";
//        //String word = "Compilable (and afterwards runnable) source file(s) of your implementation and the report you prepared.";
//        //String word = "aaa bb cc ddddd";
//        String []words = word.split(" ");
//        int margin = 15;
//        //int margin = 4;
//        int [][]slack = initSlack(words, 15);
//        int [] lineBreaks = findBestLineBreaks(words.length, slack);
//        print(lineBreaks, words);

        String input = "She is happy but is a blue gal. I am all gone.";
        int margin = 15;
        String []words = input.split(" ");

        int [][]slack = initSlack(words, margin);
        int []lineBreaks = findBestLineBreaks(words.length, slack);
        ArrayList<String> bestLines = findBestLines(lineBreaks, words);
        calculateCost(bestLines, margin);
    }
}
