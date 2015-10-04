package com.kirthanaa.id3.decisiontree;

import com.kirthanaa.id3.arffreader.ARFFReader;
import com.kirthanaa.id3.entities.ID3Attribute;
import com.kirthanaa.id3.entities.ID3Class;
import com.kirthanaa.id3.entities.ID3ContinuousInstance;
import com.kirthanaa.id3.entities.ID3TreeNode;

import java.util.*;

/**
 * Created by kirthanaaraghuraman on 9/24/15.
 */
public class ID3 {

    /**
     * ARFFReader instance for training set
     */
    private static ARFFReader mTrainSetArffReader;

    /**
     * ARFFReader instance for test set
     */
    private static ARFFReader mTestSetArffReader;

    /**
     * Minimum number of instances less than or equal to which training should stop
     */
    private static int minNoOfInstances = 2;

    /**
     * List of attributes
     */
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
     * @return Entropy Entropy of the nominal attribute
     */
    private static double getEntropyForNominalAttribute(ID3Attribute attribute, ArrayList<String[]> data) {

        ID3Class id3Class = mTrainSetArffReader.getID3Class();
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
     * @return Entropy Entropy of the continuous attribute
     */
    private static double getEntropyForContinuousAttribute(ID3Attribute attribute, double attributeValue, ArrayList<String[]> data) {
        int attributeLessThanCount = 0;
        int attributeGreaterThanCount = 0;
        int attributeLessAndPositiveCount = 0;
        int attributeLessAndNegativeCount = 0;
        int attributeGreaterAndPositiveCount = 0;
        int attributeGreaterAndNegativeCount = 0;
        ID3Class id3Class = mTrainSetArffReader.getID3Class();

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
            //System.out.println("Data instances list is of size 0 for attribute " + attribute.mAttributeName);
        } else {
            int attributeOrdinal = attribute.mAttributeOrdinal;
            double[] entropy;
            int bestCandidateSplitIndex = 0;
            double informationGain = 0.0;

            ArrayList<ID3ContinuousInstance> continuousInstancesList = new ArrayList<ID3ContinuousInstance>(data.size());
            for (int i = 0; i < data.size(); i++) {
                ID3ContinuousInstance id3ContinuousInstance = new ID3ContinuousInstance(i,
                        Double.parseDouble(data.get(i)[attributeOrdinal]),
                        data.get(i)[data.get(i).length - 1]);
                continuousInstancesList.add(id3ContinuousInstance);
            }

            Collections.sort(continuousInstancesList);

            TreeSet<Double> continuousInstanceTreeSet = new TreeSet<Double>();

            for (int i = 0; i < continuousInstancesList.size(); i++) {
                continuousInstanceTreeSet.add(continuousInstancesList.get(i).mInstanceValue);
            }

            List<Double> valueList = new ArrayList<Double>(continuousInstanceTreeSet);
            List<Double> avgList = new ArrayList<Double>();

            for (int i = 0; i < valueList.size() - 1; i++) {
                avgList.add((valueList.get(i) + valueList.get(i + 1)) / 2.0);
            }
            entropy = new double[avgList.size()];
            for (int k = 0; k < avgList.size(); k++) {
                entropy[k] = getEntropyForContinuousAttribute(attribute,
                        avgList.get(k), data);
            }

            bestCandidateSplitIndex = findMinEntropyInstanceIndex(entropy, avgList.size());

            if (bestCandidateSplitIndex != -1) {
                //System.out.println("Min entropy found at index : " + bestCandidateSplitIndex);
                if (avgList.size() == 0) {
                    informationGain = getOverallEntropy(mTrainSetArffReader.getID3Class(), data);
                } else {
                    informationGain = getOverallEntropy(mTrainSetArffReader.getID3Class(), data) - entropy[bestCandidateSplitIndex];
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
        double num = Math.log(n);
        double den = Math.log(2.00);
        double ans = num / den;
        return ans;
    }

    /**
     * Returns the class label of majority instances in the data set
     *
     * @param dataInstanceList Data instance list in which majority class label is to be found
     * @return Majority class label
     */
    private static String getMajorityClassLabel(ArrayList<String[]> dataInstanceList) {
        ID3Class id3Class = mTrainSetArffReader.getID3Class();
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
     * @return ARFFReader instance
     */
    private static ARFFReader parseARFFFile(String filename) {
        ARFFReader arffReader = ARFFReader.getInstance(filename);
        arffReader.parseARFFFile();
        return arffReader;
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
        double overallEntropy = getOverallEntropy(mTrainSetArffReader.getID3Class(), dataInstanceList);
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

            ID3Attribute splitAttribute = mTrainSetArffReader.getAttributeList().get(maxInfoGainIndex);
            //System.out.println("Splitting on attribute : " + splitAttribute.mAttributeName + " at node level : " + nodeLevel);
            if (splitAttribute.mAttributeType == ID3Attribute.NOMINAL) {
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


    /**
     * Prints the decision tree in the required format
     *
     * @param rootNode Root of the decision tree
     */
    private static void printDecisionTree(ID3TreeNode rootNode) {
        int nodeLevel = rootNode.mNodeLevel;
        String classLabel1 = mTrainSetArffReader.getID3Class().mClassLabels[0];
        String classLabel2 = mTrainSetArffReader.getID3Class().mClassLabels[1];

        if (rootNode.mChildren != null) {
            for (int j = 0; j < rootNode.mChildren.size(); j++) {
                int class1 = 0;
                int class2 = 0;
                if (rootNode.mNodeLevel > 0) {
                    for (int i = 0; i < nodeLevel; i++) {
                        System.out.print("|");
                        System.out.print("\t\t");
                    }
                }
                for (int l = 0; l < rootNode.mChildren.get(j).mInstancesAtNode.size(); l++) {
                    if (rootNode.mChildren.get(j).mInstancesAtNode.get(l)[rootNode.mChildren.get(j).mInstancesAtNode.get(l).length - 1].equalsIgnoreCase(classLabel1)) {
                        class1++;
                    } else if (rootNode.mChildren.get(j).mInstancesAtNode.get(l)[rootNode.mChildren.get(j).mInstancesAtNode.get(l).length - 1].equalsIgnoreCase(classLabel2)) {
                        class2++;
                    }
                }
                //System.out.println("Class 1 instances : " + class1);
                //System.out.println("Class 2 instances : " + class2);
                System.out.print(rootNode.mNodeAttribute.mAttributeName);
                if (rootNode.mNodeAttribute.mAttributeType == ID3Attribute.NOMINAL) {
                    System.out.print(" = ");
                    System.out.print(mTrainSetArffReader.getAttributeList().get(rootNode.mAttributeOrdinal).mAttributeValues[j]);
                } else if (rootNode.mNodeAttribute.mAttributeType == ID3Attribute.NUMERIC) {
                    if (rootNode.mChildren.indexOf(rootNode.mChildren.get(j)) == 0) {
                        System.out.print(" <= ");
                    } else if (rootNode.mChildren.indexOf(rootNode.mChildren.get(j)) == 1) {
                        System.out.print(" > ");
                    }
                    System.out.print(rootNode.mContinuousAttributeThreshold);
                }
                System.out.print(" [");
                System.out.print(String.valueOf(class1));
                System.out.print(" ");
                System.out.print(String.valueOf(class2));
                System.out.print("]");

                if (rootNode.mChildren.get(j).mIsLeafNode) {
                    System.out.print(": ");
                    if (rootNode.mChildren.get(j).mLabel == null) {
                        System.out.println(classLabel1);
                    } else {
                        System.out.println(rootNode.mChildren.get(j).mLabel);
                    }
                } else {
                    System.out.println();
                    printDecisionTree(rootNode.mChildren.get(j));
                }
            }
        }
    }

    /**
     * Evaluates a test instance by applying it to the learned tree and predicts the class label
     *
     * @param rootNode     Root node of the decision tree learnt from the training data
     * @param testInstance Instance for which class label is to be predicted
     * @return Class label of the instance
     */
    private static String evaluateInstance(ID3TreeNode rootNode, String[] testInstance) {

        ID3Attribute attribute = rootNode.mNodeAttribute;
        if (rootNode.mIsLeafNode) {
            return rootNode.mLabel;
        } else {
            if (attribute.mAttributeType == ID3Attribute.NUMERIC) {
                double splitValue = rootNode.mContinuousAttributeThreshold;
                if (Double.parseDouble(testInstance[attribute.mAttributeOrdinal]) <= splitValue) {
                    return (evaluateInstance(rootNode.mChildren.get(0), testInstance));
                } else if (Double.parseDouble(testInstance[attribute.mAttributeOrdinal]) > splitValue) {
                    return (evaluateInstance(rootNode.mChildren.get(1), testInstance));
                }
            } else if (attribute.mAttributeType == ID3Attribute.NOMINAL) {
                String instanceAttributeValue = testInstance[attribute.mAttributeOrdinal];
                int attributeIndex = -1;
                for (int l = 0; l < attribute.getNumberOfAttributeValues(); l++) {
                    if (instanceAttributeValue.equalsIgnoreCase(attribute.mAttributeValues[l])) {
                        attributeIndex = l;
                        break;
                    }
                }
                return (evaluateInstance(rootNode.mChildren.get(attributeIndex), testInstance));
            }
        }
        return "";
    }

    /**
     * Evaluates the testing set on the constructed decision tree
     *
     * @param rootNode Root node of the constructed decision tree
     */
    private static void evaluateTestData(ID3TreeNode rootNode) {
        int numCorrectlyClassified = 0;
        ArrayList<String[]> testDataInstance = mTestSetArffReader.getDataInstanceList();

        System.out.println("<Predictions for the Test Set Instances>");

        if (testDataInstance != null && testDataInstance.size() > 0) {
            for (int i = 0; i < testDataInstance.size(); i++) {
                String predictedClassLabel = "";
                String actualClassLabel = testDataInstance.get(i)[testDataInstance.get(i).length - 1];
                System.out.print(String.format("%3d", i+1) + ": Actual: " + actualClassLabel + " Predicted: ");
                predictedClassLabel = evaluateInstance(rootNode, testDataInstance.get(i));
                if (predictedClassLabel.equalsIgnoreCase(actualClassLabel)) {
                    numCorrectlyClassified++;
                }
                System.out.println(predictedClassLabel);
            }
            System.out.println("Number of correctly classified: " + numCorrectlyClassified +
                    " Total number of test instances: " + testDataInstance.size());
        }
    }


    /**
     * Initializes the unprocessed attribute list with all the attributes
     */
    private static void setUnProcessedAttributeList() {
        unProcessedAttributeList = new ArrayList<ID3Attribute>(mTrainSetArffReader.getAttributeList().size());
        unProcessedAttributeList = mTrainSetArffReader.getAttributeList();
    }


    public static void main(String[] args) {

        //String trainFilename = "/Users/kirthanaaraghuraman/Documents/CS760/Assignments/HW#1/src/com/kirthanaa/id3/trainingset/diabetes_train.arff";
        //String testFilename = "/Users/kirthanaaraghuraman/Documents/CS760/Assignments/HW#1/src/com/kirthanaa/id3/trainingset/diabetes_test.arff";

        String trainFilename = args[0];
        String testFilename = args[1];
        minNoOfInstances = Integer.parseInt(args[2]);
        mTrainSetArffReader = parseARFFFile(trainFilename);

        setUnProcessedAttributeList();

        ID3TreeNode mID3RootNode = buildDecisionTree(mTrainSetArffReader.getDataInstanceList(), 0);

        printDecisionTree(mID3RootNode);

        mTestSetArffReader = parseARFFFile(testFilename);

        evaluateTestData(mID3RootNode);

    }

}
