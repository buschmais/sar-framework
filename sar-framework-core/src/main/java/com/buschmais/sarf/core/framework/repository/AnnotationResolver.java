package com.buschmais.sarf.core.framework.repository;

import java.lang.annotation.Annotation;

/**
 * Resolves annotations, especially on interfaces which are managed by XOManager.
 */
public class AnnotationResolver {

    private static final String EXCEPTION_MESSAGE = "Annotation %s not found on %s or one of its interfaces.";

    /**
     * Resolve an annotation on the class itself and all of its interfaces.
     * If the annotation is present on more than one interfaces, the first occurrence is returned.
     *
     * @param searchClass     class to be searched
     * @param annotationClass class of the annotation
     * @param <A>             type of the annotation
     * @return the annotation, or raises an exception.
     */
    public static <A extends Annotation> A resolveAnnotation(Class<?> searchClass, Class<A> annotationClass) {
        A annotationOnType = searchClass.getAnnotation(annotationClass);
        if (annotationOnType != null) {
            return annotationOnType;
        }
        // Search interfaces
        for (Class<?> searchInterface : searchClass.getInterfaces()) {
            A annotationOnInterface = resolveAnnotation(searchInterface, annotationClass);
            if (annotationOnInterface != null) {
                return annotationOnInterface;
            }
        }

        throw new IllegalArgumentException(
            String.format(EXCEPTION_MESSAGE, annotationClass.getSimpleName(), searchClass.getSimpleName()));
    }

    private AnnotationResolver() {
    }

}
