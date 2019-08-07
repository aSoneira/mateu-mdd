package io.mateu.mdd.vaadinport.vaadin.components.fieldBuilders;

import com.google.common.base.Strings;
import com.vaadin.data.*;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import io.mateu.mdd.core.annotations.Help;
import io.mateu.mdd.core.data.MDDBinder;
import io.mateu.mdd.core.interfaces.AbstractStylist;
import io.mateu.mdd.core.annotations.Help;
import io.mateu.mdd.core.data.MDDBinder;
import io.mateu.mdd.core.interfaces.AbstractStylist;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.reflection.ReflectionHelper;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JPADoubleFieldBuilder extends JPAStringFieldBuilder {

    public boolean isSupported(FieldInterfaced field) {
        return Double.class.equals(field.getType()) || double.class.equals(field.getType());
    }

    @Override
    public void build(FieldInterfaced field, Object object, Layout container, MDDBinder binder, Map<HasValue, List<Validator>> validators, AbstractStylist stylist, Map<FieldInterfaced, Component> allFieldContainers, boolean forSearchFilter) {
        if (field.isAnnotationPresent(DecimalMin.class) && field.isAnnotationPresent(DecimalMax.class)) {
            Slider tf;
            container.addComponent(tf = new Slider(new Double(field.getAnnotation(DecimalMin.class).value()), new Double(field.getAnnotation(DecimalMax.class).value()), 1));

            if (allFieldContainers != null && allFieldContainers.size() == 0) tf.focus();

            if (allFieldContainers != null) allFieldContainers.put(field, tf);

            if (container.getComponentCount() > 0) tf.setCaption(ReflectionHelper.getCaption(field));

            if (!forSearchFilter) {

                tf.setRequiredIndicatorVisible(field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotEmpty.class));

            }

            //if (field.isAnnotationPresent(Help.class) && !Strings.isNullOrEmpty(field.getAnnotation(Help.class).value())) tf.setDescription(field.getAnnotation(Help.class).value());

            bind(binder, tf, field, forSearchFilter);
        } else super.build(field, object, container, binder, validators, stylist, allFieldContainers, forSearchFilter);
    }

    protected void bind(MDDBinder binder, Slider tf, FieldInterfaced field, boolean forSearchFilter) {
        Binder.BindingBuilder aux = binder.forField(tf);
        if (!forSearchFilter && field.getDeclaringClass() != null) aux.withValidator(new BeanValidator(field.getDeclaringClass(), field.getName()));
        aux.bind(field.getName());
    }

    @Override
    protected void bind(MDDBinder binder, TextField tf, FieldInterfaced field, boolean forSearchFilter) {
        Binder.BindingBuilder aux = binder.forField(tf).withConverter(new StringToDoubleConverter((double.class.equals(field.getType()))?0d:null,"Must be a number") {
            @Override
            public String convertToPresentation(Double value, ValueContext context) {
                if (value == null) return "";
                else {
                    //String s = super.convertToPresentation(value, context);
                    String s = "" + value;
                    if (s.endsWith(".0")) s = s.replaceAll("\\.0", "");
                    System.out.println("------------------>JPADoubleFieldBuilder.convertToPresentation()=" + s);
                    return s;
                }
            }

            @Override
            public Result<Double> convertToModel(String value, ValueContext context) {
                System.out.println("------------------>JPADoubleFieldBuilder.convertToModel(" + value + ")");
                if (value != null) {
                    if (value.contains(".") && value.contains(",")) value = value.replaceAll("\\.", "");
                    value = value.replaceAll("\\.", ",");
                }
                Result<Double> r = null;
                if (!Strings.isNullOrEmpty(value)) {
                    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                    Number number = null;
                    try {
                        number = format.parse(value.trim());
                        double d = number.doubleValue();
                        r = Result.ok(d);
                    } catch (ParseException e) {
                    }
                }
                return r;
            }
        });
        if (!forSearchFilter && field.getDeclaringClass() != null) aux.withValidator(new BeanValidator(field.getDeclaringClass(), field.getName()));
        aux.bind(field.getName());
    }

}
