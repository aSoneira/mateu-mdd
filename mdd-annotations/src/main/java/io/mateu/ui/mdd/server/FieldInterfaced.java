package io.mateu.ui.mdd.server;

import io.mateu.ui.mdd.server.annotations.Ignored;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by miguel on 22/2/17.
 */
public interface FieldInterfaced {
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

    Class<?> getType();

    Class<?> getGenericClass();

    Class<?> getDeclaringClass();

    public Type getGenericType();

    String getName();

    String getId();

    <T extends Annotation> T getAnnotation(Class<T> annotationClass);


    Class<?> getOptionsClass();

    String getOptionsQL();

    Object getValue(Object o);
}
