package com.kirthanaa.id3.decisiontree;

import com.kirthanaa.id3.arffreader.ARFFReader;
import com.kirthanaa.id3.entities.ID3Attribute;
import com.kirthanaa.id3.entities.ID3Class;
import com.kirthanaa.id3.entities.ID3ContinuousInstance;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by kirthanaaraghuraman on 9/24/15.
 */
public class ID3 {

    private static ARFFReader mArffReader;
    private static double mOverallEntropy = 0.0;

    /**
     * Given an array of entropies, returns the index with the minimum entropy
     *
     * @param data Array containing various entropies calculated for continuous attributes at identified candidate splits
     * @return Index of element with minimum entropy
     */
    private int findMinEntropyInstanceIndex(double[] data) {

        double min = data[0];
        int minIndex = 0;

        for (int i = 1; i < data.length; i++) {
            if (data[i] < min) {
                min = data[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    /**
     * Returns Information gain given the entropy
     *
     * @param entropy Entropy of the attribute
     * @return Information Gain
     */
    private double getInformationGain(double entropy) {
        return mOverallEntropy - entropy;
    }

    /**
     * Gets the entropy for the nominal attribute
     * Identifies the number of values of the nominal attribute, loops over the instances to find the number of instances
     * belonging to each value and calculates entropy.
     *
     * @param attribute Nominal ID3Attribute for which entropy is to be calculated
     * @param data      Data instances to be considered while calculating the entropy
     * @return Entropy
     */
    private double getEntropyForNominalAttribute(ID3Attribute attribute, ArrayList<String[]> data) {

        int attributeCount = 0;
        double entropy = 0.0;

        if (data.size() == 0) {
            //TODO Decide how to handle this
            System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {

            String attributeValues[] = attribute.mAttributeValues;
            int attributeOrdinal = attribute.mAttributeOrdinal;
            for (int i = 0; i < attribute.getNumberOfAttributeValues(); i++) {
                attributeCount = 0;
                for (int j = 0; j < data.size(); j++) {
                    if (data.get(j)[attributeOrdinal].equalsIgnoreCase(attributeValues[i])) {
                        attributeCount++;
                    }
                }
                double pi = ((double) attributeCount / (double) data.size());
                double logpi = log2(pi);
                entropy = entropy + (-pi * logpi);
            }
        }
        return entropy;
    }

    /**
     * Calculates the entropy for continuous attribute
     * Loops over the data and identifies number of instances lesser than/equal to and greater than the split value
     * Calculates entropy
     *
     * @param attribute      Continuous ID3Attribute for which entropy is to be calculated
     * @param attributeValue Value of attribute over which instances are to be split
     * @param data           Data instances to be considered while calculating the entropy
     * @return Entropy
     */
    private double getEntropyForContinuousAttribute(ID3Attribute attribute, double attributeValue, ArrayList<String[]> data) {
        int attributeLessThanCount = 0;
        int attributeGreaterThanCount = 0;
        double entropy = 0.0;

        if (data.size() == 0) {
            //TODO Decide how to handle this
            System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {
            int attributeOrdinal = attribute.mAttributeOrdinal;
            for (int i = 0; i < data.size(); i++) {
                if (Double.parseDouble(data.get(i)[attributeOrdinal]) <= attributeValue) {
                    attributeLessThanCount++;
                } else {
                    attributeGreaterThanCount++;
                }
            }
            double p1 = (double) attributeLessThanCount / (double) data.size();
            double p2 = (double) attributeGreaterThanCount / (double) data.size();
            entropy = -(p1 * log2(p1)) - (p2 * log2(p2));
        }
        return entropy;
    }

    /**
     * Gets the best split value for the continuous attribute
     * Extracts the required attribute value, ordinal and class label.
     * Sorts the list by the value. Check for candidate splits by taking points where the class label changes.
     * Store all the points of candidate splits in an array.
     * Calculate entropy at each point and find the maximum information gain. Split and return the value on which you are splitting.
     *
     * @param attribute Continuous Attribute for which the candidate splits are to be avaluated.
     * @param data      Data instances to be considered while calculating the entropy
     * @return Value of the continuous attribute where the instances are to be split
     */
    private double getBestSplitForContinuousAttribute(ID3Attribute attribute, ArrayList<String[]> data) {
        if (data.size() == 0) {
            //TODO Decide how to handle this
            System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {
            /*

             */
            int attributeOrdinal = attribute.mAttributeOrdinal;

            int[] candidateSplitPoints = new int[data.size()];
            double[] entropy = new double[data.size()];
            int noOfCandidateSplits = 0;
            int bestCandidateSplitIndex = 0;
            double informationGain = 0.0;

            ArrayList<ID3ContinuousInstance> continuousInstancesList = new ArrayList<ID3ContinuousInstance>(data.size());
            for (int i = 0; i < data.size(); i++) {
                ID3ContinuousInstance id3ContinuousInstance = new ID3ContinuousInstance(i,
                        Double.parseDouble(data.get(i)[attributeOrdinal]),
                        data.get(i)[data.size() - 1]);
                continuousInstancesList.add(id3ContinuousInstance);
            }

            Collections.sort(continuousInstancesList);

            int k = 0;
            for (int j = 0; j < continuousInstancesList.size() - 1; j++) {
                //TODO Check condition for attributes with same value but different class labels
                if (!continuousInstancesList.get(j).mInstanceLabel.equalsIgnoreCase(continuousInstancesList.get(j + 1).mInstanceLabel)) {
                    candidateSplitPoints[k++] = j;
                    System.out.println("###############################################################");
                    System.out.println("Candidate split identified at index " + j + " and index " + j + 1);
                    System.out.println("Class at index " + j + " : " + continuousInstancesList.get(j).mInstanceLabel);
                    System.out.println("Class at index " + j + 1 + " : " + continuousInstancesList.get(j + 1).mInstanceLabel);
                    System.out.println("###############################################################");
                }
            }
            noOfCandidateSplits = k;

            for (k = 0; k < noOfCandidateSplits; k++) {
                entropy[k] = getEntropyForContinuousAttribute(attribute,
                        continuousInstancesList.get(candidateSplitPoints[k]).mInstanceValue, data);
                System.out.println("Entropy for candidate split at index " + k + " : " + entropy[k]);
            }

            bestCandidateSplitIndex = findMinEntropyInstanceIndex(entropy);

            if (bestCandidateSplitIndex != -1) {
                System.out.println("Min entropy found at index : " + bestCandidateSplitIndex);
                informationGain = mOverallEntropy - entropy[bestCandidateSplitIndex];
                if (informationGain < 0) {
                    //TODO Decide how to handle this
                } else {
                    System.out.println("Value of attribute after which we split : " + continuousInstancesList.get(bestCandidateSplitIndex).mInstanceValue);
                    double splitValue = (continuousInstancesList.get(bestCandidateSplitIndex).mInstanceValue +
                            continuousInstancesList.get(bestCandidateSplitIndex + 1).mInstanceValue) / 2.0;
                    System.out.println("Split value : " + splitValue);
                    return splitValue;
                }
            }

        }

        return 0.0;
    }

    /**
     * Calculates entropy for the entire dataset
     *
     * @param id3Class ID3Class variable
     * @param data     Data instances in the file
     * @return Entropy
     */
    private static double getOverallEntropy(ID3Class id3Class, ArrayList<String[]> data) {
        int numberOfClassLabels = id3Class.mNoOfClasses;
        double entropy = 0.0;

        //TODO - Check again for accuracy
        int totalInstances = data.size();
        for (int i = 0; i < numberOfClassLabels; i++) {
            int classCount = 0;
            String classLabel = id3Class.mClassLabels[i];
            for (int j = 0; j < data.size(); j++) {
                if (data.get(j)[data.get(j).length - 1].equalsIgnoreCase(classLabel)) {
                    classCount++;
                }
            }
            double pi = ((double) classCount / (double) totalInstances);
            double logpi = log2(pi);
            entropy = entropy + (-pi * logpi);
        }
        return entropy;
    }

    /**
     * Calculates log base 2 of number
     *
     * @param n number whose log is to be calculates
     * @return log2 (n)
     */
    public static double log2(double n) {
        double num = Math.log(n);
        double den = Math.log(2.00);
        double ans = num / den;
        return ans;
        //return (Math.log(n) / Math.log((double)2));
    }

    /**
     * Parses the given input ARFF File
     *
     * @param filename Name/Path of file to be parsed
     */
    private static void parseARFFFile(String filename) {
        mArffReader = new ARFFReader(filename);
        mArffReader.parseARFFFile();
    }

    public static void main(String[] args) {
        //TODO - Fetch the input and output filenames from command line
        String filename = "/Users/kirthanaaraghuraman/Documents/CS760/Assignments/HW#1/src/com/kirthanaa/id3/trainingset/heart_train.arff";
        parseARFFFile(filename);
        System.out.println("Entropy value of entire data set: ");
        mOverallEntropy = getOverallEntropy(mArffReader.getID3Class(), mArffReader.getDataInstanceList());
        System.out.println(String.valueOf(mOverallEntropy));

        if (mOverallEntropy == 0.0) {
            System.out.println("Overall Entropy is 0. All instances belong to same class.");
            //TODO print tree with all instances in the root node
        }

    }

}
