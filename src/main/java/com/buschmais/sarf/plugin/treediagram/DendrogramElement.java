package com.buschmais.sarf.plugin.treediagram;

import java.util.LinkedList;
import java.util.List;

/**
 * Element of a dendrogram, i.e. a tree-like diagram.
 */
public class DendrogramElement {

    /** label of the element */
    public String label;

    /** children of the element */
    public List<DendrogramElement> children = new LinkedList<>();

}
