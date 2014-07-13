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
        int [][]slack = new int[words.length][words.length];

        // initialize slack
        for (int i=0; i<words.length; i++) {
            slack[i][i] = margin - words[i].length();

            for (int j=i+1; j<words.length; j++) {
                slack[i][j] = slack[i][j-1] - words[j].length() - 1;
            }
        }

        // print slack for debug
        for (int[] arr : slack) {
            System.out.println(Arrays.toString(arr));
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
     *                min{best(j) + S(j, i-1)}   otherwise
     *
     * @param wordCount length of the words(this can identifies as n)
     * @param slack slack table
     *
     * @return line breaks
     */
    private static int[] findBestLineBreaks(int wordCount, int [][]slack) {
        int []bestValues = new int[wordCount + 1];
        int []lineBreaks = new int[wordCount];

        bestValues[0] = 0;

        for(int i=1; i<=wordCount; i++) {
            int min = INFINITY;
            int tmp;
            int choice = 0;

            // find min cost values, its is the best value
            for (int j=0; j<i; j++) {
                // we not allow negative costs,
                // negative costs considers as infinity
                if (slack[j][i-1] < 0) {
                    // ignore here
                    tmp = INFINITY;
                } else if(j == wordCount - 1) {
                    // last line cost is 0
                    tmp = 0;
                } else {
                    // rest of the line cost is "min{best(j) + S(j, i-1)}"
                    tmp = bestValues[j] + ((slack[j][i - 1]) * (slack[j][i - 1]) * (slack[j][i - 1]));
                }

                // refine min value
                if (tmp < min) {
                    min = tmp;
                    choice = j;
                }
            }

            bestValues[i] = min;
            lineBreaks[i-1] = choice;
        }

        return lineBreaks;
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
            int i = lineBreaks[j - 1];

            // concatenate lines and store in a another array
            String line = "";
            for (String s: Arrays.copyOfRange(words, i, j)) {
                line = line + " " + s;
            }
            lines.add(line);

            System.out.println(line);
            j = i;
        }

        // reverse content in lines
        Collections.reverse(lines);
    }

    public static void main(String []args) {
        //String word = "One could imagine some of these features being contextual";
        //String word = "She is happy but is a blue gal. I am all gone.";
        String word = "Compilable (and afterwards runnable) source file(s) of your implementation and the report you prepared.";
        //String word = "aaa bb";
        String []words = word.split(" ");
        int margin = 30;
        //int margin = 4;
        int [][]slack = initSlack(words, 30);
        int [] lineBreaks = findBestLineBreaks(words.length, slack);
        print(lineBreaks, words);
    }
}
