package com.kirthanaa.id3.entities;

/**
 * Created by kirthanaaraghuraman on 9/23/15.
 */
public class ID3Class {
    /**
     * Number of classes data should be identified as
     */
    public int mNoOfClasses;

    /**
     * Label values of the class
     */
    public String[] mClassLabels;

    /**
     * Constructor for initializing ID3Class instance
     *
     * @param classLabels List of string values of the class labels
     */
    public ID3Class(String[] classLabels) {
        if (classLabels != null) {
            this.mNoOfClasses = classLabels.length;
            this.mClassLabels = classLabels;
        }
    }
}
