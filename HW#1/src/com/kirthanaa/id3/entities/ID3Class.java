package com.kirthanaa.id3.entities;

/**
 * Created by kirthanaaraghuraman on 9/23/15.
 */
public class ID3Class {
    public int mNoOfClasses;

    public String[] mClassLabels;

    public ID3Class(String[] classLabels) {
        if (classLabels != null) {
            this.mNoOfClasses = classLabels.length;
            this.mClassLabels = classLabels;
        }
    }
}
