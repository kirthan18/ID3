package com.kirthanaa.id3.arffreader;

import com.kirthanaa.id3.entities.ID3Attribute;
import com.kirthanaa.id3.entities.ID3Class;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by kirthanaaraghuraman on 9/23/15.
 */
public class ARFFReader {

    private String mFileName = "";

    private static ArrayList<String[]> mDataInstanceList = null;

    private static ID3Class mId3Class = null;

    private static ArrayList<ID3Attribute> mID3AttributeList = null;

    public ARFFReader(String fileName) {
        if (fileName == "") {
            System.out.println("Filename is empty!");
        } else {
            System.out.println("Filename : " + fileName);
        }
        this.mFileName = fileName;
    }

    /**
     * Given data of Instances format, parses them and stores in ID3Attribute format
     *
     * @param data Instance variable containing data
     */
    public void setID3Attributes(Instances data) {
        int attributeOrdinal = -1;
        String attributeName = "";
        String[] attributeValues = {};
        int attributeType = -1;

        if (mID3AttributeList == null) {
            mID3AttributeList = new ArrayList<ID3Attribute>();
        }
        if (data.classIndex() == -1) {
            for (int i = 0; i < data.numAttributes() - 1; i++) {
                attributeOrdinal = i;
                attributeName = data.attribute(i).name();
                if (data.attribute(i).isNumeric()) {
                    attributeValues = null;
                    attributeType = ID3Attribute.NUMERIC;
                } else if (data.attribute(i).isNominal()) {
                    attributeType = ID3Attribute.NOMINAL;
                    attributeValues = new String[data.attribute(i).numValues()];
                    for (int j = 0; j < data.attribute(i).numValues(); j++) {
                        attributeValues[j] = data.attribute(i).value(j);
                    }
                }
                ID3Attribute id3Attribute = new ID3Attribute(attributeOrdinal, attributeName, attributeType, attributeValues);
                mID3AttributeList.add(id3Attribute);
                System.out.println("Added attribute " + id3Attribute.mAttributeName + " to attribute list!");
            }
        }
    }

    /**
     * Returns the attribute list in the currently parsed ARFF File
     *
     * @return List of ID3Attributes
     */
    public ArrayList<ID3Attribute> getAttributeList() {
        if (mID3AttributeList == null) {
            System.out.println("Attribute list is null!");
            return null;
        } else {
            return mID3AttributeList;
        }
    }

    /**
     * Identifies the class labels from the Instances variable and stores them as ID3Class variable
     *
     * @param data Instance variable containing data
     */
    public void setID3Class(Instances data) {
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
            String classLabels[] = new String[data.attribute(data.numAttributes() - 1).numValues()];
            for (int j = 0; j < data.attribute(data.numAttributes() - 1).numValues(); j++) {
                classLabels[j] = data.attribute(data.numAttributes() - 1).value(j);
                System.out.println("Added class label " + classLabels[j] + " to ID3 class ");
            }
            mId3Class = new ID3Class(classLabels);
        }
    }

    /**
     * Returns the ID3Class variable of the current file
     *
     * @return ID3Class variable
     */
    public ID3Class getID3Class() {
        if (mId3Class == null) {
            System.out.println("Class label is null!");
            return null;
        } else {
            return mId3Class;
        }
    }

    /**
     * Returns the list of data instances
     *
     * @return List of Data Instances
     */
    public ArrayList<String[]> getDataInstanceList() {
        if (mDataInstanceList == null) {
            System.out.println("Data instance list is empty");
            return null;
        } else {
            return mDataInstanceList;
        }
    }

    /**
     * Parses the data instances in the Instance variable and stores them in an array list containing string array
     *
     * @param data Instance variable containing data
     */
    public void setDataInstanceList(Instances data) {
        if (mDataInstanceList == null) {
            mDataInstanceList = new ArrayList<String[]>();
        }
        System.out.println("Total number of Data instances : " + data.numInstances());
        if (data.numInstances() != 0) {
            for (int i = 0; i < data.numInstances(); i++) {
                String dataInstanceValue[] = new String[data.instance(i).numValues()];
                for (int j = 0; j < data.instance(i).numValues(); j++) {
                    if (data.instance(i).attribute(j).isNumeric()) {
                        dataInstanceValue[j] = data.instance(i).value(j) + "";
                        System.out.println("Adding numeric attribute value : " + dataInstanceValue[j]);
                    } else if (data.instance(i).attribute(j).isNominal()) {
                        dataInstanceValue[j] = data.instance(i).stringValue(j);
                        System.out.println("Adding nominal attribute value : " + dataInstanceValue[j]);
                    }
                }
                mDataInstanceList.add(dataInstanceValue);
                System.out.println("Number of data instances added to list: " + mDataInstanceList.size());
                //System.out.println("Instance value of attribute 1: " + data.instance(i).stringValue(1));
            }
        }
    }

    /**
     * Returns the number of data instances parsed in the ARFF input file
     *
     * @return Number of data instances
     */
    private int getNumberOfDataInstances() {
        if (mDataInstanceList == null) {
            System.out.println("Data instance list is null!");
            return -1;
        } else {
            return mDataInstanceList.size();
        }
    }

    /**
     * Returns the number of attributes in the parsed ARFF File
     *
     * @return Number of attributes
     */
    private int getNumberOfAttributes() {
        if (mID3AttributeList == null) {
            System.out.println("Attribute list is null!");
            return -1;
        } else {
            return mID3AttributeList.size();
        }
    }

    /**
     * Parses the ARFF File using the ARFFLoader class on WEKA
     */
    public void parseARFFFile() {

        ArffLoader arffLoader = new ArffLoader();
        //TODO - Change this to the argument
        File filedata = new File(mFileName);
        //File filedata = new File("/Users/kirthanaaraghuraman/Documents/CS760/Assignments/HW#1/src/com/kirthanaa/id3/trainingset/diabetes_train.arff");
        try {
            arffLoader.setFile(filedata);
            Instances data = arffLoader.getDataSet();

            setID3Attributes(data);

            setID3Class(data);

            setDataInstanceList(data);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /*public static void main(String args[]){
            ArffLoader arffloader=new ArffLoader();
            File filedata = new File("/Users/kirthanaaraghuraman/Documents/CS760/Assignments/HW#1/src/com/kirthanaa/id3/trainingset/diabetes_train.arff");
            try {
                arffloader.setFile(filedata);
                Instances data = arffloader.getDataSet();

                setID3Attributes(data);

                setID3Class(data);

                setDataInstanceList(data);

            }catch (IOException e) {
                System.out.println(e.getMessage());
            }
    }*/
}
