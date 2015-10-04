package com.kirthanaa.id3.entities;

/**
 * Created by kirthanaaraghuraman on 9/23/15.
 */
public class ID3Attribute {

    /**
     * Nominal attribute type definition
     */
    public static final int NOMINAL = 0;

    /**
     * Numeric attribute type definition
     */
    public static final int NUMERIC = 1;

    /**
     * Order of the attribute in the ARFF File
     */
    public int mAttributeOrdinal = -1;

    /**
     * Name of the attribute
     */
    public String mAttributeName;

    /**
     * Type of attribute - numeric/nominal
     */
    public int mAttributeType;

    /**
     * Set of values that could be taken by a nominal attribute
     * Will be null for continuous attribute
     */
    public String[] mAttributeValues;

    /**
     * Constructor for initializing ID3Attribute instance
     *
     * @param attributeOrdinal Order in which attribute appears in the ARFF File
     * @param attributeName    Name of the attribute
     * @param attributeType    Type of attribute (Nominal or numeric)
     * @param attributeValues  Possible values the attribute could take
     */
    public ID3Attribute(int attributeOrdinal, String attributeName, int attributeType, String[] attributeValues) {
        this.mAttributeOrdinal = attributeOrdinal;
        this.mAttributeName = attributeName;
        this.mAttributeType = attributeType;
        this.mAttributeValues = attributeValues;
    }

    /**
     * Returns the number of values a nominal attribute can take
     *
     * @return Number of values of nominal attribute
     */
    public int getNumberOfAttributeValues() {
        return mAttributeValues.length;
    }
}
