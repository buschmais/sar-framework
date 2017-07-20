package com.buchmais.sarf.classification.configuration;

import java.lang.reflect.Field;

/**
 * @author Stephan Pirnbaum
 */
public abstract class WeightConstants {
    public static Double DEPENDS_ON_WEIGHT = 1d; // 1

    public static Double INVOKES_WEIGHT = 3d;  // 2

    public static Double READS_WEIGHT = 2d;  // 3

    public static Double WRITES_WEIGHT = 2d;  // 4

    public static Double READS_STATIC_WEIGHT = 1d;  // 4

    public static Double WRITES_STATIC_WEIGHT = 1d; // 5

    public static Double EXTENDS_WEIGHT = 1d; // 6

    public static Double IMPLEMENTS_WEIGHT = 1d;  // 7

    public static Double PARAMETER_WEIGHT = 1d; // 8

    public static Double RETURNS_WEIGHT = 2d; // 9

    public static Double INNER_CLASSES_WEIGHT = 3d; // 10

    public static Double INVOKES_STATIC_WEIGHT = 1d; // 11

    public static Double COMPOSES_WEIGHT = 1d; // 12

    public static String stringify() {
        StringBuilder builder = new StringBuilder();
        for (Field f : WeightConstants.class.getFields()) {
            try {
                builder.append(f.getName() + ": " + f.get(null) + "\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
