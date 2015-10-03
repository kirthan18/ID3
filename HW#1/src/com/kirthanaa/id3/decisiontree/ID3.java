package com.kirthanaa.id3.decisiontree;

import com.kirthanaa.id3.arffreader.ARFFReader;
import com.kirthanaa.id3.entities.ID3Attribute;
import com.kirthanaa.id3.entities.ID3Class;
import com.kirthanaa.id3.entities.ID3ContinuousInstance;
import com.kirthanaa.id3.entities.ID3TreeNode;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;

/**
 * Created by kirthanaaraghuraman on 9/24/15.
 */
public class ID3 {

    private static ARFFReader mArffReader;

    private static ID3TreeNode mID3RootNode = null;

    private static int minNoOfInstances = 2;

    private static ArrayList<ID3Attribute> unProcessedAttributeList;

    /**
     * Given an array of entropies, returns the index with the minimum entropy
     *
     * @param data Array containing various entropies calculated for continuous attributes at identified candidate splits
     * @return Index of element with minimum entropy
     */
    private static int findMinEntropyInstanceIndex(double[] data, int size) {
        int minIndex = 0;
        if (data.length > 0) {
            double min = data[0];

            for (int i = 1; i < size; i++) {
                if (data[i] < min) {
                    min = data[i];
                    minIndex = i;
                }
            }
        }
        return minIndex;
    }

    /**
     * Returns Information gain given the entropy
     *
     * @param entropy        Entropy of the attribute
     * @param overallEntropy Entropy of instances w.r.t class labels
     * @return Information Gain
     */
    private double getInformationGain(double overallEntropy, double entropy) {
        return overallEntropy - entropy;
    }

    private static double getEntropy(double num, double den) {
        if (num == 0 || den == 0) {
            return 0;
        }
        return (num / den) * log2(num / den);
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
    private static double getEntropyForNominalAttribute(ID3Attribute attribute, ArrayList<String[]> data) {

        ID3Class id3Class = mArffReader.getID3Class();
        int attributeCount = 0;
        double entropy = 0.0;

        if (data.size() == 0) {
            //TODO Decide how to handle this
            System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {

            String attributeValues[] = attribute.mAttributeValues;
            int attributeOrdinal = attribute.mAttributeOrdinal;
            int labelAttrCount = attributeValues.length;

            for (int i = 0; i < labelAttrCount; i++) {
                int labelSubCountFirstClass = 0;
                int labelSubCountSecondClass = 0;
                int labelCount = 0;
                for (int j = 0; j < data.size(); j++) {
                    if (data.get(j)[attributeOrdinal].equalsIgnoreCase(attributeValues[i])) {
                        if (data.get(j)[data.get(j).length - 1].equalsIgnoreCase(id3Class.mClassLabels[0])) {
                            labelSubCountFirstClass++;
                        } else if (data.get(j)[data.get(j).length - 1].equalsIgnoreCase(id3Class.mClassLabels[1])) {
                            labelSubCountSecondClass++;
                        }
                        labelCount++;
                    }
                }
                double subEntropy =
                        getEntropy(labelSubCountFirstClass, labelCount) +
                                getEntropy(labelSubCountSecondClass, labelCount);
                entropy += ((double) labelCount / data.size()) * subEntropy;


/*
                if (attributeCount != 0) {
                    double pi = ((double) attributeCount / (double) data.size());
                    double logpi = log2(pi);
                    entropy = entropy + (-pi * logpi);
                } else {
                    entropy = entropy + 0.0;
                }
                */
            }
        }
        return -entropy;
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
    private static double getEntropyForContinuousAttribute(ID3Attribute attribute, double attributeValue, ArrayList<String[]> data) {
        int attributeLessThanCount = 0;
        int attributeGreaterThanCount = 0;
        int attributeLessAndPositiveCount = 0;
        int attributeLessAndNegativeCount = 0;
        int attributeGreaterAndPositiveCount = 0;
        int attributeGreaterAndNegativeCount = 0;
        ID3Class id3Class = mArffReader.getID3Class();

        double entropy = 0.0;

        if (data.size() == 0) {
            //TODO Decide how to handle this
            System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {
            int attributeOrdinal = attribute.mAttributeOrdinal;
            for (int i = 0; i < data.size(); i++) {
                if (Double.parseDouble(data.get(i)[attributeOrdinal]) <= attributeValue) {
                    attributeLessThanCount++;
                    if (data.get(i)[data.get(i).length - 1].equalsIgnoreCase(id3Class.mClassLabels[0])) {
                        attributeLessAndPositiveCount++;
                    } else if (data.get(i)[data.get(i).length - 1].equalsIgnoreCase(id3Class.mClassLabels[1])) {
                        attributeLessAndNegativeCount++;
                    }
                } else {
                    attributeGreaterThanCount++;
                    if (data.get(i)[data.get(i).length - 1].equalsIgnoreCase(id3Class.mClassLabels[0])) {
                        attributeGreaterAndPositiveCount++;
                    } else if (data.get(i)[data.get(i).length - 1].equalsIgnoreCase(id3Class.mClassLabels[1])) {
                        attributeGreaterAndNegativeCount++;
                    }
                }
            }

            double p1 = 0.0, p2 = 0.0, p11 = 0.0, p12 = 0.0, p21 = 0.0, p22 = 0.0;
            if (attributeLessThanCount != 0) {
                p1 = (double) attributeLessThanCount / (double) data.size();
                if (attributeLessAndPositiveCount != 0) {
                    p11 = (double) attributeLessAndPositiveCount / (double) attributeLessThanCount;
                } else {
                    p11 = 0.0;
                }

                if (attributeLessAndNegativeCount != 0) {
                    p12 = (double) attributeLessAndNegativeCount / (double) attributeLessThanCount;
                } else {
                    p12 = 0.0;
                }
            } else {
                p1 = 0.0;
                p11 = 0.0;
                p12 = 0.0;
            }

            if (attributeGreaterThanCount != 0) {
                p2 = (double) attributeGreaterThanCount / (double) data.size();
                if (attributeGreaterAndPositiveCount != 0) {
                    p21 = (double) attributeGreaterAndPositiveCount / (double) attributeGreaterThanCount;
                } else {
                    p21 = 0.0;
                }

                if (attributeGreaterAndNegativeCount != 0) {
                    p22 = (double) attributeGreaterAndNegativeCount / (double) attributeGreaterThanCount;
                } else {
                    p22 = 0.0;
                }
            } else {
                p2 = 0.0;
                p21 = 0.0;
                p22 = 0.0;
            }

            double log2p11 = 0.0, log2p12 = 0.0, log2p21 = 0.0, log2p22 = 0.0;
            if (p11 == 0.0) {
                log2p11 = 0.0;
            } else {
                log2p11 = log2(p11);
            }
            if (p12 == 0.0) {
                log2p12 = 0.0;
            } else {
                log2p12 = log2(p12);
            }
            if (p21 == 0.0) {
                log2p21 = 0.0;
            } else {
                log2p21 = log2(p21);
            }
            if (p22 == 0.0) {
                log2p22 = 0.0;
            } else {
                log2p22 = log2(p22);
            }
            entropy = -((p1 * ((p11 * log2p11) + (p12 * log2p12))) + (p2 * ((p21 * log2p21) + (p22 * log2p22))));
            //entropy = -((p1 * (p11 * log2(p11) + p12 * log2(p12))) + (p2 * (p21 * log2(p21) + p22 * log2(p22))));

            /*double p1 = 0.0;
            double p2 = 0.0;
            double p1logp1 = 0.0;
            double p2logp2 = 0.0;
            if (attributeLessThanCount == 0) {
                p1 = 0.0;
                p1logp1 = 0.0;
            } else {
                p1 = (double) attributeLessThanCount / (double) data.size();
                p1logp1 = log2(p1);
            }
            if (attributeGreaterThanCount == 0) {
                p2 = 0.0;
                p2logp2 = 0.0;
            } else {
                p2 = (double) attributeGreaterThanCount / (double) data.size();
                p2logp2 = log2(p2);
            }
            entropy = -(p1 * p1logp1) - (p2 * p2logp2);*/

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
    private static double getBestSplitForContinuousAttribute(ID3Attribute attribute, ArrayList<String[]> data) {
        if (data.size() == 0) {
            //TODO Decide how to handle this
            System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {

            int attributeOrdinal = attribute.mAttributeOrdinal;

            int[] candidateSplitPoints = new int[data.size()];
            double[] entropy;
            int noOfCandidateSplits = 0;
            int bestCandidateSplitIndex = 0;
            double informationGain = 0.0;

            ArrayList<ID3ContinuousInstance> continuousInstancesList = new ArrayList<ID3ContinuousInstance>(data.size());
            for (int i = 0; i < data.size(); i++) {
                ID3ContinuousInstance id3ContinuousInstance = new ID3ContinuousInstance(i,
                        Double.parseDouble(data.get(i)[attributeOrdinal]),
                        data.get(i)[data.get(i).length - 1]);
                continuousInstancesList.add(id3ContinuousInstance);
            }
            /*if (attribute.mAttributeName.equalsIgnoreCase("thalach") && data.size() == 103) {
                for(int i = 0; i < continuousInstancesList.size(); i++) {
                    System.out.println("Continuous instance list : " + continuousInstancesList.get(i).mInstanceValue);
                }
            }*/

            Collections.sort(continuousInstancesList);
            /*f (attribute.mAttributeName.equalsIgnoreCase("thalach") && data.size() == 103) {
                for(int i = 0; i < continuousInstancesList.size(); i++) {
                    System.out.println("Sorted Continuous instance list : " + continuousInstancesList.get(i).mInstanceValue);
                }
            }*/
            TreeSet<Double> continuousInstanceTreeSet = new TreeSet<Double>();
            /*if (attribute.mAttributeName.equalsIgnoreCase("thalach")) {
                System.out.println("Candidate key length : " + continuousInstancesList.size());
            }*/

            for (int i = 0; i < continuousInstancesList.size(); i++) {
                continuousInstanceTreeSet.add(continuousInstancesList.get(i).mInstanceValue);
            }
            /*System.out.println("Set list : ");
            for (double d : continuousInstanceTreeSet) {
                System.out.println(d);
            }*/

            int k = 0;
            /*for (int j = 0; j < continuousInstancesList.size(); j++) {
                //TODO Check condition for attributes with same value but different class labels
                //if (!continuousInstancesList.get(j).mInstanceLabel.equalsIgnoreCase(continuousInstancesList.get(j + 1).mInstanceLabel)) {
                candidateSplitPoints[k++] = j;
                    *//*System.out.println("###############################################################");
                    System.out.println("Candidate split identified at index " + j + " and index " + (j+1));
                    System.out.println("Class at index " + j + " : " + continuousInstancesList.get(j).mInstanceLabel);
                    System.out.println("Class at index " + (j+1) + " : " + continuousInstancesList.get(j + 1).mInstanceLabel);
                    System.out.println("###############################################################");*//*
                //}
            }
            noOfCandidateSplits = k - 1;

            *//*for (k = 0; k < noOfCandidateSplits; k++) {
                entropy[k] = getEntropyForContinuousAttribute(attribute,
                        (continuousInstancesList.get(candidateSplitPoints[k]).mInstanceValue +
                        continuousInstancesList.get(candidateSplitPoints[k]+1).mInstanceValue)/2.0, data);
                //System.out.println("Entropy for candidate split at index " + k + " : " + entropy[k]);
            }

            for (k = 0; k < data.size() - 1; k++) {
                entropy[k] = getEntropyForContinuousAttribute(attribute,
                        (continuousInstancesList.get(k).mInstanceValue + continuousInstancesList.get(k + 1).mInstanceValue) / 2.0,
                        data);
            }*/

            List<Double> valueList = new ArrayList<Double>(continuousInstanceTreeSet);
            /*if (attribute.mAttributeName.equalsIgnoreCase("thalach")) {
                System.out.println("Candidate keys for attribute " + attribute.mAttributeName + " : ");
                for (double d : valueList) {
                    System.out.println(d);
                }
            }*/
            List<Double> avgList = new ArrayList<Double>();
            //continuousInstanc
            //System.out.println("avg list");
            for (int i = 0; i < valueList.size() - 1; i++) {
                avgList.add((valueList.get(i) + valueList.get(i + 1)) / 2.0);
                //System.out.println(avgList.get(i));
            }
            entropy = new double[avgList.size()];
            for (k = 0; k < avgList.size(); k++) {
                entropy[k] = getEntropyForContinuousAttribute(attribute,
                        avgList.get(k), data);
            }

            bestCandidateSplitIndex = findMinEntropyInstanceIndex(entropy, avgList.size());

            if (bestCandidateSplitIndex != -1) {
                //System.out.println("Min entropy found at index : " + bestCandidateSplitIndex);
                if (avgList.size() == 0) {
                    informationGain = getOverallEntropy(mArffReader.getID3Class(), data);
                } else {
                    informationGain = getOverallEntropy(mArffReader.getID3Class(), data) - entropy[bestCandidateSplitIndex];
                }
                if (informationGain < 0) {
                    //TODO Decide how to handle this
                } else {
                    //System.out.println("Value of attribute after which we split : " + continuousInstancesList.get(candidateSplitPoints[bestCandidateSplitIndex]).mInstanceValue);
                    double splitValue = 0.0;
                    if (bestCandidateSplitIndex >= avgList.size() - 1) {
                        splitValue = valueList.get(bestCandidateSplitIndex);
                    } else {
                        splitValue = avgList.get(bestCandidateSplitIndex);
                    }
                    //System.out.println("Split value for attribute " + attribute.mAttributeName + " : " + splitValue);
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
            if (classCount == 0) {
                entropy = entropy + 0.0;
            } else {
                double pi = ((double) classCount / (double) totalInstances);
                double logpi = log2(pi);
                entropy = entropy + (-pi * logpi);
            }
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
        /*if(n == 0.000){
            return 0.0;
        }*/
        double num = Math.log(n);
        double den = Math.log(2.00);
        double ans = num / den;
        return ans;
        //return (Math.log(n) / Math.log((double)2));
    }

    /**
     * Returns the class label of majority instances in the data set
     *
     * @param dataInstanceList Data instance list in which majority class label is to be found
     * @return Majority class label
     */
    private static String getMajorityClassLabel(ArrayList<String[]> dataInstanceList) {
        ID3Class id3Class = mArffReader.getID3Class();
        int noOfClasses = id3Class.mNoOfClasses;
        int countClassLabels[] = new int[noOfClasses];

        if (noOfClasses == 2) {
            if (dataInstanceList != null && !dataInstanceList.isEmpty()) {
                int classLabelIndex = dataInstanceList.get(0).length - 1;
                int class1Count = 0;
                int class2Count = 0;
                for (int i = 0; i < dataInstanceList.size(); i++) {
                    if (dataInstanceList.get(i)[classLabelIndex].equalsIgnoreCase(id3Class.mClassLabels[0])) {
                        class1Count++;
                    } else {
                        class2Count++;
                    }
                }
                if (class1Count > class2Count) {
                    return id3Class.mClassLabels[0];
                } else if (class1Count < class2Count) {
                    return id3Class.mClassLabels[1];
                } else {
                    return id3Class.mClassLabels[0];
                }
            }
        } else if (dataInstanceList != null && !dataInstanceList.isEmpty()) {
            int classLabelIndex = dataInstanceList.get(0).length - 1;
            for (int i = 0; i < dataInstanceList.size(); i++) {
                for (int j = 0; j < noOfClasses; j++) {
                    if (dataInstanceList.get(i)[classLabelIndex].equalsIgnoreCase(id3Class.mClassLabels[j])) {
                        countClassLabels[j]++;
                        break;
                    }
                }
            }

            int maxIndex = -1;
            int max = 0;
            for (int k = 0; k < noOfClasses; k++) {
                if (countClassLabels[k] > max) {
                    max = countClassLabels[k];
                    maxIndex = k;
                }
            }
            return id3Class.mClassLabels[maxIndex];
        }
        return null;
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

    /**
     * Constructs leaf node
     *
     * @param dataInstance Data instances
     * @param nodeLevel    Level of the leaf node
     * @return Leaf Node constructed
     */
    private static ID3TreeNode getLeafNode(ArrayList<String[]> dataInstance, int nodeLevel) {

        ID3TreeNode id3TreeNode = new ID3TreeNode();
        id3TreeNode.mIsLeafNode = true;
        id3TreeNode.mChildren = null;
        id3TreeNode.mContinuousAttributeThreshold = 0.0;
        id3TreeNode.mNodeType = -1;
        id3TreeNode.mAttributeOrdinal = -1;
        id3TreeNode.mNodeAttribute = null;
        id3TreeNode.mInstancesAtNode = dataInstance;
        id3TreeNode.mLabel = getMajorityClassLabel(dataInstance);
        id3TreeNode.mNodeLevel = nodeLevel;
        return id3TreeNode;

    }

    /**
     * Constructs the ID3 decision tree
     *
     * @param dataInstanceList List of Data Instances
     * @param nodeLevel        Level of node
     * @return Root node of the decision tree
     */
    private static ID3TreeNode buildDecisionTree(ArrayList<String[]> dataInstanceList, int nodeLevel) {
        double overallEntropy = getOverallEntropy(mArffReader.getID3Class(), dataInstanceList);
        //System.out.println("Entropy value of entire data set: " + overallEntropy);

        if (dataInstanceList.size() < minNoOfInstances) {
            //System.out.println("No of instances in node is less than specified value.");
            return getLeafNode(dataInstanceList, nodeLevel);
        }

        if (dataInstanceList.size() <= 0) {
            return getLeafNode(dataInstanceList, nodeLevel);
        }

        if (overallEntropy == 0.0) {
            //System.out.println("Overall Entropy is 0. All instances belong to same class.");
            return getLeafNode(dataInstanceList, nodeLevel);
        } else {
            //System.out.println("Node level : " + nodeLevel);
            double informationGain[] = new double[unProcessedAttributeList.size()];
            //System.out.println("Unprocessed attribute list size : " + unProcessedAttributeList.size());
            for (int i = 0; i < unProcessedAttributeList.size(); i++) {
                if (unProcessedAttributeList.get(i).mAttributeType == ID3Attribute.NOMINAL) {
                    informationGain[i] = overallEntropy - getEntropyForNominalAttribute(unProcessedAttributeList.get(i), dataInstanceList);
                } else if (unProcessedAttributeList.get(i).mAttributeType == ID3Attribute.NUMERIC) {
                    double splitValue = getBestSplitForContinuousAttribute(unProcessedAttributeList.get(i), dataInstanceList);
                    informationGain[i] = overallEntropy - getEntropyForContinuousAttribute(unProcessedAttributeList.get(i)
                            , splitValue, dataInstanceList);
                }
                //System.out.println("Information gain for attribute " + unProcessedAttributeList.get(i).mAttributeName + " : " + informationGain[i]);
            }

            double maxInfoGain = informationGain[0];
            int maxInfoGainIndex = 0;
            boolean isDuplicateInfoGain = false;
            for (int l = 1; l < informationGain.length; l++) {
                if (informationGain[l] > maxInfoGain) {
                    maxInfoGain = informationGain[l];
                    maxInfoGainIndex = l;
                } else {
                    if (maxInfoGain == informationGain[l]) {
                        isDuplicateInfoGain = true;
                    }
                }
            }
            //System.out.println("Maximum information gain : " + maxInfoGain);
            //System.out.println("Maximum information gain index : " + maxInfoGainIndex);
            //TODO - check if info gain of first attribute is always taken when several attributes have same information gain
            ID3Attribute splitAttribute = mArffReader.getAttributeList().get(maxInfoGainIndex);
            System.out.println("Splitting on attribute : " + splitAttribute.mAttributeName + " at node level : " + nodeLevel);
            if (splitAttribute.mAttributeType == ID3Attribute.NOMINAL) {
                //Remove the nominal attribute from the list as we do not need it further
                //unProcessedAttributeList.remove(splitAttribute);
                ID3TreeNode id3TreeNode = new ID3TreeNode();
                id3TreeNode.mIsLeafNode = false;
                id3TreeNode.mNodeLevel = nodeLevel;
                id3TreeNode.mNodeAttribute = splitAttribute;
                id3TreeNode.mLabel = null;
                id3TreeNode.mAttributeOrdinal = maxInfoGainIndex;
                id3TreeNode.mContinuousAttributeThreshold = -1.0;
                id3TreeNode.mNodeType = ID3Attribute.NOMINAL;
                id3TreeNode.mInstancesAtNode = dataInstanceList;
                id3TreeNode.mChildren = new ArrayList<ID3TreeNode>(splitAttribute.getNumberOfAttributeValues());

                for (int i = 0; i < id3TreeNode.mNodeAttribute.getNumberOfAttributeValues(); i++) {
                    ArrayList<String[]> dataInstance = new ArrayList<String[]>();
                    for (int j = 0; j < dataInstanceList.size(); j++) {
                        if (dataInstanceList.get(j)[id3TreeNode.mAttributeOrdinal].equalsIgnoreCase(splitAttribute.mAttributeValues[i])) {
                            dataInstance.add(dataInstanceList.get(j));
                        }
                    }
                    id3TreeNode.mChildren.add(buildDecisionTree(dataInstance, nodeLevel + 1));
                }
                return id3TreeNode;
            } else if (splitAttribute.mAttributeType == ID3Attribute.NUMERIC) {
                double splitValue = getBestSplitForContinuousAttribute(splitAttribute, dataInstanceList);
                ID3TreeNode id3TreeNode = new ID3TreeNode();
                id3TreeNode.mNodeLevel = nodeLevel;
                id3TreeNode.mNodeAttribute = splitAttribute;
                id3TreeNode.mLabel = null;
                id3TreeNode.mAttributeOrdinal = maxInfoGainIndex;
                id3TreeNode.mContinuousAttributeThreshold = splitValue;
                id3TreeNode.mIsLeafNode = false;
                id3TreeNode.mNodeType = ID3Attribute.NUMERIC;
                id3TreeNode.mInstancesAtNode = dataInstanceList;
                id3TreeNode.mChildren = new ArrayList<ID3TreeNode>(2);

                ArrayList<String[]> leftBranchData = new ArrayList<String[]>();
                ArrayList<String[]> rightBranchData = new ArrayList<String[]>();
                for (int a = 0; a < dataInstanceList.size(); a++) {
                    if (Double.parseDouble(dataInstanceList.get(a)[id3TreeNode.mAttributeOrdinal]) <= splitValue) {
                        leftBranchData.add(dataInstanceList.get(a));
                    } else {
                        rightBranchData.add(dataInstanceList.get(a));
                    }
                }

                id3TreeNode.mChildren.add(buildDecisionTree(leftBranchData, nodeLevel + 1));
                id3TreeNode.mChildren.add(buildDecisionTree(rightBranchData, nodeLevel + 1));
                return id3TreeNode;
            }
        }

        return null;
    }


    private static void printDecisionTree() {

    }


    private static ID3TreeNode getDummyNode() {
        ID3TreeNode id3TreeNode = new ID3TreeNode();
        id3TreeNode.mChildren = null;
        id3TreeNode.mAttributeOrdinal = -1;
        id3TreeNode.mIsLeafNode = false;
        id3TreeNode.mNodeAttribute = null;
        id3TreeNode.mContinuousAttributeThreshold = 0.0;
        id3TreeNode.mInstancesAtNode = mArffReader.getDataInstanceList();
        id3TreeNode.mLabel = "";
        id3TreeNode.mNodeLevel = 0;
        id3TreeNode.mNodeType = -1;
        return id3TreeNode;
    }

    /**
     * Initializes the unprocessed attribute list with all the attributes
     */
    private static void setUnProcessedAttributeList() {
        unProcessedAttributeList = new ArrayList<ID3Attribute>(mArffReader.getAttributeList().size());
        unProcessedAttributeList = mArffReader.getAttributeList();
    }


    public static void main(String[] args) {
        //TODO - Fetch the input and output filenames from command line
        String filename = "/Users/kirthanaaraghuraman/Documents/CS760/Assignments/HW#1/src/com/kirthanaa/id3/trainingset/diabetes_train.arff";
        parseARFFFile(filename);
        setUnProcessedAttributeList();
        //mID3RootNode = getDummyNode();
        mID3RootNode = buildDecisionTree(mArffReader.getDataInstanceList(), 0);

        printDecisionTree();


    }

}
