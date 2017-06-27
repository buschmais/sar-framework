package com.buchmais.sarf.classification.configuration;

/**
 * @author Stephan Pirnbaum
 */
public abstract class WeightConstants {
    public static final Double INVOKES_WEIGHT = 3d;

    public static final Double READS_WEIGHT = 1d;

    public static final Double WRITES_WEIGHT = 1d;

    public static final Double READS_STATIC_WEIGHT = 1d;

    public static final Double WRITES_STATIC_WEIGHT = 1d;

    public static final Double EXTENDS_WEIGHT = .5d;

    public static final Double IMPLEMENTS_WEIGHT = .5d;

    public static final Double PARAMETER_WEIGHT = 1d;

    public static final Double RETURNS_WEIGHT = 1d;

    public static final Double LOCAL_VARIABLE_WEIGHT = 1d;

    public static final Double INNER_CLASSES_WEIGHT = 1d;

    public static final Double INVOKES_STATIC_WEIGHT = 1d;

    public static final Double COMPOSES_WEIGHT = 1d;
}
