package io.mateu.mdd.vaadinport.vaadin.components.oldviews;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Grid;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.reflection.ReflectionHelper;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.mdd.vaadinport.vaadin.components.ClassOption;
import io.mateu.mdd.vaadinport.vaadin.util.VaadinHelper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.ManyToOne;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public class JPACollectionFieldListViewComponent extends JPAListViewComponent {

    private final FieldInterfaced field;
    private final IEditorViewComponent evfc;
    private Collection list;
    private Object model;

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.LIST_SELECT;
    }

    @Override
    public String getFieldsFilter() {
        return getColumns(field);
    }

    public JPACollectionFieldListViewComponent(Class entityClass, FieldInterfaced field, IEditorViewComponent evfc) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        super(entityClass, getColumns(field));
        this.field = field;
        this.evfc = evfc;
        this.model = evfc.getModel();
        this.list = (Collection) ReflectionHelper.getValue(field, model);

    }

    private static String getColumns(FieldInterfaced field) {
        String fields = "";
        if (field.getAnnotation(UseLinkToListView.class) != null) fields = field.getAnnotation(UseLinkToListView.class).fields();
        if (Strings.isNullOrEmpty(fields) && field.isAnnotationPresent(FieldsFilter.class)) fields = field.getAnnotation(FieldsFilter.class).value();
        return fields;
    }

    @Override
    public String getTitle() {
        return "" + (model != null?model.toString():"--") + ": " + super.getTitle();
    }

    public FieldInterfaced getField() {
        return field;
    }

    public IEditorViewComponent getEvfc() {
        return evfc;
    }

    @Override
    public boolean isAddEnabled() {
        return false;
    }

    @Override
    public boolean isDeleteEnabled() {
        return false;
    }

    public boolean canAdd() {
        return ReflectionHelper.isOwnedCollection(field) && (!field.isAnnotationPresent(UseLinkToListView.class) || field.getAnnotation(UseLinkToListView.class).addEnabled());
    }

    public boolean canDelete() {
        return ReflectionHelper.isOwnedCollection(field) && (!field.isAnnotationPresent(UseLinkToListView.class) || field.getAnnotation(UseLinkToListView.class).deleteEnabled());
    }

    @Override
    public List<AbstractAction> getActions() {
        List<AbstractAction> l = super.getActions();

            if (canAdd()) l.add(new AbstractAction("Add") {
                @Override
                public void run() {

                    Set<Class> subClasses = ReflectionHelper.getSubclasses(getModelType());

                    if (subClasses.size() > 1) {

                        Set<ClassOption> subClassesOptions = new LinkedHashSet<>();
                        subClasses.forEach(c -> subClassesOptions.add(new ClassOption(c)));

                        VaadinHelper.choose("Please choose type", subClassesOptions, c -> {
                            try {
                                MDDUI.get().getNavegador().setPendingResult(ReflectionHelper.newInstance(((ClassOption)c).get_class(), model));
                                MDDUI.get().getNavegador().go("add");
                            } catch (Exception e) {
                                MDD.alert(e);
                            }
                        }, () -> MDDUI.get().getNavegador().goBack());
                    } else if (subClasses.size() == 1) {
                        try {
                            MDDUI.get().getNavegador().setPendingResult(ReflectionHelper.newInstance(subClasses.iterator().next(), model));

                            MDDUI.get().getNavegador().go("add");

                        } catch (Exception e) {
                            MDD.alert(e);
                        }
                    } else {
                        MDDUI.get().getNavegador().go("add");
                    }

                }
            });
            if (canDelete()) l.add(new AbstractAction("Remove selected items") {
                @Override
                public void run() {

                    if (resultsComponent.getSelection().size() == 0) MDD.alert("No item selected");
                    else MDD.confirm("Are you sure?", new Runnable() {
                        @Override
                        public void run() {

                            try {

                                Set borrar = resultsComponent.getSelection();

                                Collection col = ((Collection) ReflectionHelper.getValue(field, model));

                                col.removeAll(borrar);

                                ReflectionHelper.setValue(field, model, col);

                                evfc.getBinder().setBean(evfc.getModel(), false);

                                evfc.getBinder().getRemovables().addAll(borrar);

                                evfc.save(false);


                                MDDUI.get().getNavegador().goBack();

                            } catch (Throwable e) {
                                MDD.alert(e);
                            }

                        }
                    });


                }
            });



        Object bean = (evfc != null)?evfc.getModel():null;

        boolean isEditingNewRecord = MDDUI.get().isEditingNewRecord();

        List<Method> ms = new ArrayList<>();
        for (Method m : ReflectionHelper.getAllMethods(bean != null?bean.getClass():field.getDeclaringClass())) {
            if (!(
                    Modifier.isStatic(m.getModifiers())
                            || (m.isAnnotationPresent(NotWhenCreating.class) && isEditingNewRecord)
                            || (m.isAnnotationPresent(NotWhenEditing.class) && !isEditingNewRecord))
                    && (m.isAnnotationPresent(Action.class) && field.getName().equals(m.getAnnotation(Action.class).attachToField()))
            ) {
                ms.add(m);
            }
        }

        ms.sort((a, b) -> {
            return a.getAnnotation(Action.class).order() - b.getAnnotation(Action.class).order();
        });

        ms.forEach(m -> {
            AbstractAction a;
            l.add(a = ViewComponentHelper.createAction(m, this));
        });

        String filter = field.isAnnotationPresent(UseLinkToListView.class)?field.getAnnotation(UseLinkToListView.class).actions():"";
        if (!Strings.isNullOrEmpty(filter)) {
            List<String> filteredActionIds = List.of(filter.split(","));
            l.removeIf(a -> !filteredActionIds.contains(a.getId()));
        }

        return l;
    }

    @Override
    public Collection findAll(Object filters, List<QuerySortOrder> sortOrders, int offset, int limit) {
        return list;
    }

    @Override
    public int count(Object filters) throws Throwable {
        return list.size();
    }

    @Override
    public void edit(Object id) {
        MDDUI.get().getNavegador().go("" + id);
    }


    public Object addNew() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object o = getModelType().newInstance();
        //ReflectionHelper.reverseMap(evfc.getBinder(), field, evfc.getModel(), o);
        /// lo hemos movido al onsave
        return o;
    }

    public Object getItem(String sid) {
        Object id = deserializeId(sid);
        Object f = null;
        for (Object o : list) if (id.equals(toId(o))) {
            f = o;
            break;
        }
        return f;
    }

    public void saved(Object o) throws Throwable {
        //list.add(o);
        Object bean = evfc.getModel();
        ReflectionHelper.setValue(field, bean, ReflectionHelper.getValue(field, bean));
        evfc.getBinder().setBean(bean, false);
        //evfc.save(false, false);
        list = (Collection) ReflectionHelper.getValue(field, model);
        search(this);
    }


    @Override
    public void decorateGrid(Grid grid) {
        String fields = "";
        if (field.getAnnotation(UseLinkToListView.class) != null) fields = field.getAnnotation(UseLinkToListView.class).fields();
        if (Strings.isNullOrEmpty(fields) && field.isAnnotationPresent(FieldsFilter.class)) fields = field.getAnnotation(FieldsFilter.class).value();
        if (!Strings.isNullOrEmpty(fields)) {
            List<String> fns = Lists.newArrayList(fields.replaceAll(" ", "").split(","));
            for (Grid.Column col : (List<Grid.Column>) grid.getColumns()) {
                if (col.getId() != null && !fns.contains(col.getId()) && (getFieldsByColId().get(col.getId()) == null || !fns.contains(getFieldsByColId().get(col.getId()).getName()))) col.setHidden(true);
            }
        } else {
            super.decorateGrid(grid);
        }
    }

    public void preSave(Object model) throws Throwable {

        log.debug("******PRESAVE******");

        Object parent = this.evfc.getModel();

        log.debug("******MODEL=" + model);
        log.debug("******PARENT=" + parent);

        Collection col = (Collection) ReflectionHelper.getValue(field, parent);
        log.debug("******RESULT=" + col.size());

        ReflectionHelper.addToCollection(evfc.getBinder(), field, parent, model);

        col = (Collection) ReflectionHelper.getValue(field, parent);
        log.debug("******RESULT=" + col.size());


        Class targetType = model.getClass();

        List<FieldInterfaced> parentFields = ReflectionHelper.getAllFields(parent.getClass());

        for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(targetType)) {

            for (FieldInterfaced pf : parentFields) {
                if (pf.isAnnotationPresent(ManyToOne.class) && f.isAnnotationPresent(ManyToOne.class) && pf.getType().equals(f.getType()) && pf.getName().equals(f.getName())) {
                    ReflectionHelper.setValue(f, model, ReflectionHelper.getValue(pf, parent));
                }
            }

        }

    }


    @Override
    public Method getMethod(String methodName) {
        Method a = super.getMethod(methodName);

        if (a == null) {
            Object bean = (evfc != null)?evfc.getModel():null;

            for (Method m : ReflectionHelper.getAllMethods(bean != null?bean.getClass():field.getDeclaringClass())) {
                if (!Modifier.isStatic(m.getModifiers())
                        && m.getName().equals(methodName)
                        && (m.isAnnotationPresent(Action.class) &&  field.getName().equals(m.getAnnotation(Action.class).attachToField()))
                ) {
                    a = m;
                    break;
                }
            }

        }

        return a;
    }
}
