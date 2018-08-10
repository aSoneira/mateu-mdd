package io.mateu.mdd.vaadinport.vaadin.components.fields;

import com.google.common.base.Strings;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.shared.ui.ErrorLevel;
import com.vaadin.ui.TextField;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.data.MDDBinder;

import java.util.List;

public class JPADoubleFieldBuilder extends JPAStringFieldBuilder {

    public boolean isSupported(FieldInterfaced field) {
        return Double.class.equals(field.getType()) || double.class.equals(field.getType());
    }


    @Override
    public void addValidators(List<Validator> validators) {
        validators.add(new Validator() {
            @Override
            public ValidationResult apply(Object o, ValueContext valueContext) {
                try {

                    String s = (String) o;

                    if (!Strings.isNullOrEmpty(s)) Double.parseDouble(s);

                    return ValidationResult.ok();
                } catch (Exception e) {
                    return ValidationResult.create("Not a number", ErrorLevel.ERROR);
                }
            }

            @Override
            public Object apply(Object o, Object o2) {
                return null;
            }
        });
    }

    @Override
    protected void bind(MDDBinder binder, TextField tf, FieldInterfaced field) {
        binder.forField(tf).withConverter(new StringToDoubleConverter("Must be an number")).bind(field.getName());
    }


    @Override
    public Object convert(String s) {
        return (!Strings.isNullOrEmpty(s))?Double.parseDouble(s):null;
    }
}
