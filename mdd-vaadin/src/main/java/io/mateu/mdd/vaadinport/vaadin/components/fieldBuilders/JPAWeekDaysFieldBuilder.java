package io.mateu.mdd.vaadinport.vaadin.components.fieldBuilders;

import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import io.mateu.mdd.core.annotations.WeekDays;
import io.mateu.mdd.core.data.MDDBinder;
import io.mateu.mdd.core.interfaces.AbstractStylist;
import io.mateu.mdd.core.model.multilanguage.Literal;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.vaadinport.vaadin.components.fieldBuilders.components.LiteralComponent;
import io.mateu.mdd.vaadinport.vaadin.components.fieldBuilders.components.WeekDaysComponent;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class JPAWeekDaysFieldBuilder extends JPAFieldBuilder {


    private Literal literal;

    public boolean isSupported(FieldInterfaced field) {
        return boolean[].class.equals(field.getType()) && field.isAnnotationPresent(WeekDays.class);
    }

    public void build(FieldInterfaced field, Object object, Layout container, MDDBinder binder, Map<HasValue, List<Validator>> validators, AbstractStylist stylist, Map<FieldInterfaced, Component> allFieldContainers, boolean forSearchFilter) {

        WeekDaysComponent c;
        container.addComponent(c = new WeekDaysComponent(field, binder));

        if (allFieldContainers.size() == 0) c.focus();

        allFieldContainers.put(field, c);

        c.setCaption(Helper.capitalize(field.getName()));

        if (!forSearchFilter) {

            c.setRequiredIndicatorVisible(field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotEmpty.class));

        }

        bind(binder, c, field, forSearchFilter);
    }


    protected void bind(MDDBinder binder, WeekDaysComponent c, FieldInterfaced field, boolean forSearchFilter) {
        Binder.BindingBuilder aux = binder.forField(c);
        //if (!forSearchFilter && field.getDeclaringClass() != null) aux.withValidator(new BeanValidator(field.getDeclaringClass(), field.getName()));
        aux.bind(field.getName());
    }
}
