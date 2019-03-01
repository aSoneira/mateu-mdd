package io.mateu.mdd.vaadinport.vaadin.components.fieldBuilders;

import com.google.common.base.Strings;
import com.vaadin.data.HasValue;
import com.vaadin.data.Validator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.mateu.mdd.core.CSS;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.data.MDDBinder;
import io.mateu.mdd.core.annotations.Help;
import io.mateu.mdd.core.data.MDDBinder;
import io.mateu.mdd.core.dataProviders.JPQLListDataProvider;
import io.mateu.mdd.core.interfaces.AbstractStylist;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.reflection.ProxyClass;
import io.mateu.mdd.core.reflection.ReflectionHelper;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.mdd.vaadinport.vaadin.components.oldviews.ListViewComponent;
import org.javamoney.moneta.FastMoney;

import javax.money.MonetaryAmount;
import javax.persistence.Entity;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JPAOutputFieldBuilder extends AbstractFieldBuilder {


    public boolean isSupported(FieldInterfaced field) {
        return String.class.equals(field.getType());
    }

    public void build(FieldInterfaced field, Object object, Layout container, MDDBinder binder, Map<HasValue, List<Validator>> validators, AbstractStylist stylist, Map<FieldInterfaced, Component> allFieldContainers, boolean forSearchFilter) {

        if (!forSearchFilter) {

            if (Collection.class.isAssignableFrom(field.getType())) {


                Grid g = new Grid();

                List<FieldInterfaced> colFields = JPAOneToManyFieldBuilder.getColumnFields(field);

                Class targetClass = field.getGenericClass();

                boolean inline = false;

                ListViewComponent.buildColumns(g, colFields, false, inline, binder, field);

                g.setSelectionMode(Grid.SelectionMode.MULTI);

                g.setCaption(ReflectionHelper.getCaption(field));


                int ancho = 0;
                for (Grid.Column col : (List<Grid.Column>) g.getColumns()) ancho += col.getWidth();
                if (ancho <= 0) ancho = 500;


                boolean anchoCompleto = field.isAnnotationPresent(FullWidth.class) || ancho > 900;
                anchoCompleto = false;

                if (anchoCompleto) g.setWidth("100%");
                else {
                    g.setWidth("" + (ancho + 60) + "px");
                }

                // añadimos columna para que no haga feo
                if (anchoCompleto) {
                    if (g.getColumns().size() == 1) ((Grid.Column) g.getColumns().get(0)).setExpandRatio(1);
                    else g.addColumn((d) -> null).setWidthUndefined().setCaption("");
                }

                g.setHeightMode(HeightMode.UNDEFINED);


                DataProvider dpa = (field.isAnnotationPresent(DataProvider.class)) ? field.getAnnotation(DataProvider.class) : null;

                if (dpa == null) {

                    Method mdp = ReflectionHelper.getMethod(field.getDeclaringClass(), ReflectionHelper.getGetter(field.getName()) + "DataProvider");

                }


                JPAOneToManyFieldBuilder.bind(binder, g, field, targetClass, null);

                container.addComponent(g);

                if (allFieldContainers != null) if (allFieldContainers != null) allFieldContainers.put(field, g);
            } else {

                Button botonLink = null;
                Label tf = new Label();
                tf.setContentMode(ContentMode.HTML);
                tf.addStyleName("outputlabel");

                if (Integer.class.equals(field.getType()) || int.class.equals(field.getType())
                        || Long.class.equals(field.getType()) || long.class.equals(field.getType())
                        || Double.class.equals(field.getType()) || double.class.equals(field.getType())
                        || FastMoney.class.equals(field.getType()) || MonetaryAmount.class.equals(field.getType())) {
                    tf.addStyleName("alinearderecha");
                }


                if (field.getType().isAnnotationPresent(Entity.class)) {

                    HorizontalLayout hl = new HorizontalLayout();
                    hl.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
                    container.addComponent(hl);

                    hl.addComponent(tf);

                    if (allFieldContainers != null) allFieldContainers.put(field, hl);

                    if (container.getComponentCount() > 0) hl.setCaption(ReflectionHelper.getCaption(field));

                    //if (field.isAnnotationPresent(Help.class) && !Strings.isNullOrEmpty(field.getAnnotation(Help.class).value())) hl.setDescription(field.getAnnotation(Help.class).value());

                    botonLink = new Button(null, VaadinIcons.EDIT);
                    botonLink.addStyleName(ValoTheme.BUTTON_QUIET);
                    botonLink.addStyleName(CSS.NOPADDING);
                    botonLink.addClickListener(e -> MDDUI.get().getNavegador().go(field.getName()));
                    hl.addComponent(botonLink);
                    hl.setComponentAlignment(tf, Alignment.MIDDLE_LEFT);
                    hl.setComponentAlignment(botonLink, Alignment.MIDDLE_LEFT);

                } else {

                    container.addComponent(tf);

                    if (allFieldContainers != null) if (allFieldContainers != null) allFieldContainers.put(field, tf);

                    if (container.getComponentCount() > 0) tf.setCaption(ReflectionHelper.getCaption(field));

                    //if (field.isAnnotationPresent(Help.class) && !Strings.isNullOrEmpty(field.getAnnotation(Help.class).value())) tf.setDescription(field.getAnnotation(Help.class).value());

                }



                bind(binder, tf, botonLink, field);

            }



        }

    }

    protected void bind(MDDBinder binder, Label tf, Button botonLink, FieldInterfaced field) {
        binder.forField(new HasValue() {

            Object v;

            @Override
            public void setValue(Object o) {
                v = o;
                tf.setValue((o != null)?objectToString(o):"");
                if (botonLink != null) botonLink.setVisible(o != null);
            }

            @Override
            public Object getValue() {
                return v;
            }

            @Override
            public Registration addValueChangeListener(ValueChangeListener valueChangeListener) {
                return null;
            }

            @Override
            public void setRequiredIndicatorVisible(boolean b) {

            }

            @Override
            public boolean isRequiredIndicatorVisible() {
                return false;
            }

            @Override
            public void setReadOnly(boolean b) {

            }

            @Override
            public boolean isReadOnly() {
                return false;
            }
        }).bind(field.getName());
    }

    private String objectToString(Object o) {
        if (o instanceof Collection) {
            String s = "";
            for (Object x : (Collection) o) {
                if (!"".equals(s)) s += "<br/>";
                s += x.toString();
            }
            if ("".equals(s)) s += "Empty list";
            return s;
        } else {
            return o.toString();
        }
    }
}
