package com.kirthanaa.id3.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirthanaaraghuraman on 9/27/15.
 */
public class ID3TreeNode {

    /**
     * Indicates if node is leaf node or not
     */
    public boolean mIsLeafNode;

    /**
     * Indicates if node is of nominal/numeric attribute type
     */
    public int mNodeType;

    /**
     * Indicates the attribute on which the node is going to be split
     */
    public ID3Attribute mNodeAttribute;

    /**
     * Indicates the ordinal of the attribute on which the node is split
     */
    public int mAttributeOrdinal;

    /**
     * Indicates the attribute on which the parent node was split
     */
    public ID3Attribute mParentAttribute;

    /**
     * Indicates the class label of the node
     */
    public String mLabel;

    /**
     * List of pointers to the child nodes
     */
    public ArrayList<ID3TreeNode> mChildren;

    /**
     * ArrayList containing the list of data instances at the node
     */
    public ArrayList<String[]> mInstancesAtNode;

    /**
     * Threshold value for continuous attribute
     */
    public double mContinuousAttributeThreshold;

    /**
     * Level of node
     * Level 0 denotes root
     */
    public int mNodeLevel;

}
