package io.mateu.mdd.vaadin.components.fieldBuilders;

import com.google.common.base.Strings;
import com.vaadin.data.*;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.interfaces.AbstractStylist;
import io.mateu.mdd.shared.reflection.FieldInterfaced;
import io.mateu.mdd.vaadin.data.MDDBinder;
import io.mateu.reflection.ReflectionHelper;
import io.mateu.util.notification.Notifier;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class JPAURLFieldBuilder extends AbstractFieldBuilder {


    public boolean isSupported(FieldInterfaced field) {
        return URL.class.equals(field.getType());
    }

    @Override
    public Component build(FieldInterfaced field, Object object, Layout container, MDDBinder binder, Map<HasValue, List<Validator>> validators, AbstractStylist stylist, Map<FieldInterfaced, Component> allFieldContainers, boolean forSearchFilter, Map<String, List<AbstractAction>> attachedActions) {

        TextField tf;
        container.addComponent(tf = new TextField());
        tf.setValueChangeMode(ValueChangeMode.BLUR);

        addErrorHandler(field, tf);


        Button b = new Button("Go", e -> {
            if (!Strings.isNullOrEmpty(tf.getValue())) Page.getCurrent().open(tf.getValue(), "_blank");
        });


        HorizontalLayout hl;
        container.addComponent(hl = new HorizontalLayout(tf, b));


        if (allFieldContainers != null && allFieldContainers.size() == 0) tf.focus();

        if (allFieldContainers != null) allFieldContainers.put(field, tf);

        if (container.getComponentCount() > 0) hl.setCaption(ReflectionHelper.getCaption(field));

        if (!forSearchFilter) {

            tf.setRequiredIndicatorVisible(field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotEmpty.class));

        }

        //if (field.isAnnotationPresent(Help.class) && !Strings.isNullOrEmpty(field.getAnnotation(Help.class).value())) tf.setDescription(field.getAnnotation(Help.class).value());


        bind(binder, tf, field, forSearchFilter);

        return tf;
    }


    protected void bind(MDDBinder binder, TextField tf, FieldInterfaced field, boolean forSearchFilter) {
        Binder.BindingBuilder aux = binder.forField(tf).withConverter(new Converter() {
            @Override
            public Result convertToModel(Object o, ValueContext valueContext) {
                URL u = null;
                try {
                    if (!Strings.isNullOrEmpty((String) o)) u = new URL((String) o);
                } catch (Exception e) {
                    Notifier.alert(e);
                }
                return Result.ok(u);
            }

            @Override
            public Object convertToPresentation(Object o, ValueContext valueContext) {
                return (o != null) ? o.toString() : "";
            }
        });
        if (!forSearchFilter && field.getDeclaringClass() != null) aux.withValidator(new BeanValidator(field.getDeclaringClass(), field.getName()));
        aux.bind(field.getName());
    }
}
