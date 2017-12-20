package io.mateu.ui.mdd.server;

import io.mateu.ui.mdd.server.annotations.ValueClass;
import io.mateu.ui.mdd.server.annotations.ValueQL;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FieldInterfacedFromField implements FieldInterfaced {

    private final Field f;
    private final FieldInterfaced ff;

    @Override
    public Field getField() {
        return f;
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return (ff != null)?ff.getDeclaredAnnotationsByType(annotationClass):f.getDeclaredAnnotationsByType(annotationClass);
    }


    public FieldInterfacedFromField(FieldInterfaced f) {
        this.ff = f;
        this.f = f.getField();
    }

    public FieldInterfacedFromField(Field f) {
        this.ff = null;
        this.f = f;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return (ff != null)?ff.isAnnotationPresent(annotationClass):f.isAnnotationPresent(annotationClass);
    }

    @Override
    public Class<?> getType() {
        return (ff != null)?ff.getType():f.getType();
    }

    @Override
    public Class<?> getGenericClass() {
        if (ff != null) return ff.getGenericClass();
        else if (f.getGenericType() != null && f.getGenericType() instanceof ParameterizedType) {

            ParameterizedType genericType = (ParameterizedType) f.getGenericType();
            if (genericType != null && genericType.getActualTypeArguments().length > 0) {
                Class<?> genericClass = (Class<?>) genericType.getActualTypeArguments()[0];
                return genericClass;
            } else return null;

        } else return null;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return (ff != null)?ff.getDeclaringClass():f.getDeclaringClass();
    }

    @Override
    public Type getGenericType() {
        return (ff != null)?ff.getGenericType():f.getGenericType();
    }

    @Override
    public String getName() {
        return (ff != null)?ff.getName():f.getName();
    }

    @Override
    public String getId() {
        return (ff != null)?ff.getId():f.getName();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return (ff != null)?ff.getAnnotation(annotationClass):f.getAnnotation(annotationClass);
    }

    @Override
    public Class<?> getOptionsClass() {
        return (ff != null)?ff.getOptionsClass():((f.isAnnotationPresent(ValueClass.class))?f.getAnnotation(ValueClass.class).value():null);
    }

    @Override
    public String getOptionsQL() {
        return (ff != null)?ff.getOptionsQL():((f.isAnnotationPresent(ValueQL.class))?f.getAnnotation(ValueQL.class).value():null);
    }

    @Override
    public Object getValue(Object o) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return ERPServiceImpl.getValue(this, o);
    }


    @Override
    public void setValue(Object o, Object v) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ERPServiceImpl.setValue(this, o, v);
    }
}
