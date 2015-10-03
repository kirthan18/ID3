package com.kirthanaa.id3.entities;

/**
 * Created by kirthanaaraghuraman on 9/27/15.
 */
public class ID3ContinuousInstance implements Comparable<ID3ContinuousInstance> {

    public int mInstanceOrdinal = -1;

    public double mInstanceValue = 0.0;

    public String mInstanceLabel = "";

    public ID3ContinuousInstance(int instanceOrdinal, double instanceValue, String classLabel) {
        this.mInstanceOrdinal = instanceOrdinal;
        this.mInstanceValue = instanceValue;
        this.mInstanceLabel = classLabel;
    }

    @Override
    public int compareTo(ID3ContinuousInstance continuousInstance) {
        if (this.mInstanceValue == continuousInstance.mInstanceValue) {
            return 0;
        } else if (this.mInstanceValue > continuousInstance.mInstanceValue) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object id3ContinuousInstance){
        return this.mInstanceValue == ((ID3ContinuousInstance)id3ContinuousInstance).mInstanceValue;
    }
}
