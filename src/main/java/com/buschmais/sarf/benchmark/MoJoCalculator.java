package com.buschmais.sarf.benchmark;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.metamodel.ComponentDescriptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MoJoCalculator {

    private BufferedReader br_s, br_t;

    public static Set<ComponentDescriptor> reference = null;

    public MoJoCalculator(BufferedReader br_s, BufferedReader br_t) {
        this.br_s = br_s;
        this.br_t = br_t;
    }

    public MoJoCalculator(Map<Long, Set<Long>> decomposition, boolean isFrom) {
        StringBuilder builder1 = new StringBuilder();
        for (Map.Entry<Long, Set<Long>> dec : decomposition.entrySet()) {
            for (Long id : dec.getValue()) {
                TypeDescriptor typeDescriptor = DatabaseHelper.xoManager.findById(TypeDescriptor.class, id);
                builder1.append("contain " + dec.getKey() + " " + typeDescriptor.getName() + "\n");
            }
        }
        StringBuilder builder2 = new StringBuilder();
        for (ComponentDescriptor componentDescriptor : reference) {
            for (TypeDescriptor typeDescriptor : componentDescriptor.getContainedTypes()) {
                builder2.append("contain " + componentDescriptor.getName() + " " + typeDescriptor.getName() + "\n");
            }
        }
        if (isFrom) {
            this.br_s = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(builder1.toString().getBytes())));
            this.br_t = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(builder2.toString().getBytes())));
        } else {
            this.br_s = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(builder2.toString().getBytes())));
            this.br_t = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(builder1.toString().getBytes())));
        }
    }

    public MoJoCalculator(Set<ComponentDescriptor> from, Set<ComponentDescriptor> to) {
        StringBuilder fromRsf = new StringBuilder();
        for (ComponentDescriptor componentDescriptor : from) {
            for (TypeDescriptor typeDescriptor : componentDescriptor.getContainedTypes()) {
                fromRsf.append("contain " + componentDescriptor.getName() + " " + typeDescriptor.getName() + "\n");
            }
        }
        StringBuilder toRsf = new StringBuilder();
        for (ComponentDescriptor componentDescriptor : to) {
            for (TypeDescriptor typeDescriptor : componentDescriptor.getContainedTypes()) {
                toRsf.append("contain " + componentDescriptor.getName() + " " + typeDescriptor.getName() + "\n");
            }
        }
        this.br_s = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fromRsf.toString().getBytes())));
        this.br_t = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(toRsf.toString().getBytes())));
    }

    /* The mapping between objects and clusters in B */
    private Map<String, String> mapObjectClusterInB = new Hashtable<String, String>();

    /* The mappings of clusters to tags in both A and B */
    private Map<String, Integer> mapClusterTagA = new Hashtable<String, Integer>();

    private Map<String, Integer> mapClusterTagB = new Hashtable<String, Integer>();

    /* Mapping between edges and their edgecost */
    private Hashtable<String, Double> tableR = new Hashtable<String, Double>();

    /* use for store the name of each items */
    private Vector<String> clusterNamesInA = new Vector<String>();

    // Stores the number of objects in each cluster in partition B
    // Used in calculating the max distance from partition B
    private Vector<Integer> cardinalitiesInB = new Vector<Integer>();

    /* This vector contains a vector for each cluster in A */
    private Vector<Vector<String>> partitionA = new Vector<Vector<String>>();

    private int l = 0; /* number of clusters in A */

    private int m = 0; /* number of clusters in B */

    private long numberOfObjectsInA;

    private Cluster A[] = null;

    private boolean verbose = true;

    /*
     * record the capacity of each group, if the group is empty ,the count is
     * zero, otherwise >= 1
     */
    private int groupscount[] = null;

    /*
     * after join operations, each group will have only one cluster left, we use
     * grouptags[i] to indicate the remain cluster in group i
     */
    private Cluster grouptags[] = null; /*
                                         * every none empty group have a tag
                                         * point to a cluster in A
                                         */

    public long mojoplus() {

        commonPrep();

        /* tag assigment */
        tagAssignment("MoJoPlus");

        /* draw graph and matching */
        maxbipartiteMatching();

        /* Calculate total cost */
        return calculateCost();
    }

    public double mojofm() {

        commonPrep();

        /* tag assigment */
        tagAssignment("MoJo");

        /* draw graph and matching */
        maxbipartiteMatching();

        /* Calculate MoJoFM value */
        return mojofmValue(cardinalitiesInB, numberOfObjectsInA, calculateCost());
    }

    public void setVerbose(boolean v) {
        verbose = v;
    }

    public long mojo() {

        commonPrep();

        /* tag assigment */
        tagAssignment("MoJo");

        /* draw graph and matching */
        maxbipartiteMatching();

        /* Calculate total cost */
        return calculateCost();
    }

    private void commonPrep() {

        numberOfObjectsInA = 0;

        /* Read target file first to update mapObjectClusterInB */
        readTargetRSFFile();

        /* Read source file */
        readSourceRSFfile();

        l = mapClusterTagA.size(); /* number of clusters in A */
        m = mapClusterTagB.size(); /* number of clusters in B */

        A = new Cluster[l]; /* create A */
        groupscount = new int[m]; /* the count of each group, 0 if empty */
        grouptags = new Cluster[m]; /*
                                     * the first cluster in each group, null if
                                     * empty
                                     */

        /* init group tags */
        for (int j = 0; j < m; j++)
        {
            grouptags[j] = null;
        }

        /* create each cluster in A */
        for (int i = 0; i < l; i++)
        {
            A[i] = new Cluster(i, l, m);
        }
    }

    private void maxbipartiteMatching() {
        
        /* Create the graph and add all the edges */
        BipartiteGraph bgraph = new BipartiteGraph(l + m, l, m);

        for (int i = 0; i < l; i++)
        {
            for (int j = 0; j < A[i].groupList.size(); j++)
            {
                bgraph.addedge(i, l + A[i].groupList.elementAt(j).intValue());
            }
        }

        /* Use maximum bipartite matching to calculate the groups */
        bgraph.matching();
        
        /*
         * Assign group after matching, for each Ai in matching, assign the
         * corresponding group, for other cluster in A, just leave them alone
         */
        for (int i = l; i < l + m; i++)
        {
            if (bgraph.vertex[i].matched)
            {
                int index = bgraph.adjacentList.elementAt(i).elementAt(0).intValue();
                A[index].setGroup(i - l);
            }
        }

    }

    /*
     * Calculates the MoJoFM value, using the formula MoJoFM(M) = 1 - mno(A,B)/
     * max(mno(any_A,B)) * 100%
     */
    private double mojofmValue(Vector number_of_B, long obj_number, long totalCost) {
        long maxDis = maxDistanceTo(number_of_B, obj_number);
        return Math.rint((1 - (double) totalCost / (double) maxDis) * 10000) / 100;
    }

    /* calculate the max(mno(B, any_A)), which is also the max(mno(any_A, B)) */
    private long maxDistanceTo(Vector number_of_B, long obj_number) {
        int group_number = 0;
        int[] B = new int[number_of_B.size()];

        for (int i = 0; i < B.length; i++)
        {
            B[i] = ((Integer) number_of_B.elementAt(i)).intValue();
        }
        /* sort the array in ascending order */
        Arrays.sort(B);

        for (int i = 0; i < B.length; i++)
        {
            /* calculate the minimum maximum possible groups for partition B */
            /*
             * after sort the B_i in ascending order B_i: 1, 2, 3, 4, 5, 6, 7,
             * 8, 10, 10, 10, 15 we can calculate g in this way g: 1, 2, 3, 4,
             * 5, 6, 7, 8, 9, 10, 10, 11
             */
            if (group_number < B[i]) group_number++;
        }
        /* return n - l + l - g = n - g */
        return obj_number - group_number;

    }

    private long calculateCost() {
        int moves = 0; /* total number of move operations */
        int no_of_nonempty_group = 0; /* number of total noneempty groups */
        long totalCost = 0; /* total cost of MoJo */

        /* find none empty groups and find total number of moves */
        for (int i = 0; i < l; i++)
        {
            /* caculate the count of nonempty groups */
            /*
             * when we found that a group was set to empty but in fact is not
             * empty, we increase the number of noneempty group by 1
             */
            if (groupscount[A[i].getGroup()] == 0)
            {
                no_of_nonempty_group += 1;
            }
            /* assign group tags */
            /* if this group has no tag, then we assign A[i] to its tag */
            if (grouptags[A[i].getGroup()] == null)
            {
                grouptags[A[i].getGroup()] = A[i];
            }
            /* assign the group count */
            groupscount[A[i].getGroup()] += 1;
            /* calculate the number of move opts for each cluster */
            moves += A[i].gettotalTags() - A[i].getMaxtag();
        }
        totalCost = moves + l - no_of_nonempty_group;
        return totalCost;
    }

    private void tagAssignment(String mode) {
        for (int i = 0; i < l; i++)
        {
            int tag = -1;
            String clusterName = "";
            for (int j = 0; j < partitionA.elementAt(i).size(); j++)
            {
                String objName = partitionA.elementAt(i).elementAt(j);
                clusterName = mapObjectClusterInB.get(objName);
                tag = mapClusterTagB.get(clusterName).intValue();
                A[i].addobject(tag, objName, mode);
            }
        }
    }

    private void readSourceRSFfile() {
        long extraInA = 0;
        try
        {
            for (String line = br_s.readLine(); line != null; line = br_s.readLine())
            {
                StringTokenizer st = new StringTokenizer(line);
                if (st.countTokens() != 3)
                {
                    String message = "Incorrect RSF format in source in the following line:\n" + line;
                    throw new RuntimeException(message);
                }
                // Ignore lines that do not start with contain
                if (!st.nextToken().toLowerCase().equals("contain")) continue;

                int index = -1;
                String clusterName = st.nextToken();
                String objectName = st.nextToken();

                if (mapObjectClusterInB.keySet().contains(objectName))
                {
                    numberOfObjectsInA++;
                    Integer objectIndex = mapClusterTagA.get(clusterName);
                    if (objectIndex == null)
                    {
                        index = mapClusterTagA.size();
                        clusterNamesInA.addElement(clusterName);
                        mapClusterTagA.put(clusterName, new Integer(index));
                        partitionA.addElement(new Vector<String>());
                    }
                    else
                    {
                        index = objectIndex.intValue();
                    }
                    partitionA.elementAt(index).addElement(objectName);
                }
                else extraInA++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not read from source");
        }
        try
        {
            br_s.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not close source");
        }
        if (extraInA > 0) put("Warning: " + extraInA + " objects in source were not found in target. They will be ignored.");
        long extraInB = mapObjectClusterInB.keySet().size() - numberOfObjectsInA;
        if (extraInB > 0) put("Warning: " + extraInB + " objects in target were not found in source. They will be ignored.");
    }

    private void readTargetRSFFile() {
        try
        {
            for (String line = br_t.readLine(); line != null; line = br_t.readLine())
            {
                StringTokenizer st = new StringTokenizer(line);
                if (st.countTokens() != 3)
                {
                    String message = "Incorrect RSF format in target in the following line:\n" + line;
                    throw new RuntimeException(message);
                }
                // Ignore lines that do not start with contain
                if (!st.nextToken().toLowerCase().equals("contain")) continue;

                String clusterName = st.nextToken();
                /* Remove quotes from the token */
                int first_quote_index = clusterName.indexOf("\"");
                if (first_quote_index == 0 && clusterName.indexOf("\"", first_quote_index + 1) == clusterName.length() - 1)
                    clusterName = clusterName.substring(first_quote_index + 1, clusterName.length() - 1);

                String objectName = st.nextToken();
                int index = -1;

                /* Search for the cluster name in mapClusterTagB */
                Integer objectIndex = mapClusterTagB.get(clusterName);

                if (objectIndex == null)
                {
                    // This cluster is not in mapClusterTagB yet
                    index = mapClusterTagB.size();
                    // Since it is a new cluster, it currently contains 1 object
                    cardinalitiesInB.addElement(new Integer(1));
                    mapClusterTagB.put(clusterName, new Integer(index));
                }
                else
                {
                    index = objectIndex.intValue();
                    // Increase the cluster's cardinality in vector
                    // cardinalitiesInB
                    int newCardinality = 1 + cardinalitiesInB.elementAt(index).intValue();
                    cardinalitiesInB.setElementAt(new Integer(newCardinality), index);
                }
                mapObjectClusterInB.put(objectName, clusterName);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not read from target");
        }
        try
        {
            br_t.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not close target");
        }
    }

    private void put(String message) {
        if (verbose) System.out.println(message);
    }
}
