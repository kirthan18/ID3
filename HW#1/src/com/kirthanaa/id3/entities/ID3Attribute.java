package com.kirthanaa.id3.entities;

/**
 * Created by kirthanaaraghuraman on 9/23/15.
 */
public class ID3Attribute {

    public static final int NOMINAL = 0;

    public static final int NUMERIC = 1;

    public int mAttributeOrdinal = -1;

    public String mAttributeName;

    public int mAttributeType;

    public String[] mAttributeValues;

    public ID3Attribute(int attributeOrdinal, String attributeName, int attributeType, String[] attributeValues) {
        this.mAttributeOrdinal = attributeOrdinal;
        this.mAttributeName = attributeName;
        this.mAttributeType = attributeType;
        this.mAttributeValues = attributeValues;
    }

    public int getNumberOfAttributeValues() {
        return mAttributeValues.length;
    }
}
