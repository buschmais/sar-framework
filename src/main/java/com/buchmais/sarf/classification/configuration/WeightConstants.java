package com.buchmais.sarf.classification.configuration;

/**
 * @author Stephan Pirnbaum
 */
public abstract class WeightConstants {
    public static Double DEPENDS_ON_WEIGHT = 1d; // 1

    public static Double INVOKES_WEIGHT = 0d;  // 2

    public static Double READS_WEIGHT = 0d;  // 3

    public static Double WRITES_WEIGHT = 0d;  // 4

    public static Double READS_STATIC_WEIGHT = 0d;  // 4

    public static Double WRITES_STATIC_WEIGHT = 0d; // 5

    public static Double EXTENDS_WEIGHT = 0d; // 6

    public static Double IMPLEMENTS_WEIGHT = 0d;  // 7

    public static Double PARAMETER_WEIGHT = 0d; // 8

    public static Double RETURNS_WEIGHT = 0d; // 9

    public static Double INNER_CLASSES_WEIGHT = 0d; // 10

    public static Double INVOKES_STATIC_WEIGHT = 0d; // 11

    public static Double COMPOSES_WEIGHT = 0d; // 12
}
