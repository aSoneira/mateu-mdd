package io.mateu.mdd.vaadinport.vaadin.components.oldviews;

import com.google.common.base.Strings;
import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;
import io.mateu.mdd.core.CSS;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.data.MDDBinder;
import io.mateu.mdd.core.data.Pair;
import io.mateu.mdd.core.interfaces.*;
import io.mateu.mdd.core.model.common.Resource;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.reflection.ReflectionHelper;
import io.mateu.mdd.core.util.HasChangesSignature;
import io.mateu.mdd.util.Helper;
import io.mateu.mdd.util.persistence.JPATransaction;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.mdd.vaadinport.vaadin.components.ClassOption;
import io.mateu.mdd.vaadinport.vaadin.components.EditorViewStyler;
import io.mateu.mdd.vaadinport.vaadin.components.app.views.FiltersViewFlowComponent;
import io.mateu.mdd.vaadinport.vaadin.util.BindedWindow;
import io.mateu.mdd.vaadinport.vaadin.util.VaadinHelper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
public class EditorViewComponent extends AbstractViewComponent implements IEditorViewComponent {

    private boolean createSaveButton;
    private final List<FieldInterfaced> visibleFields;
    private final List<FieldInterfaced> hiddenFields;
    private ListViewComponent listViewComponent;
    private Object owner = null;
    private FieldInterfaced field = null;
    private Map<HasValue, List<Validator>> validators = new HashMap<>();
    List<Component> componentsToLookForErrors = new ArrayList<>();

    protected boolean newRecord;
    private Class modelType;

    private MDDBinder binder;

    private AbstractStylist stylist;
    private Panel panel;
    private Layout kpisContainer;
    private Layout kpis;
    private Panel panelContenido;

    private List<EditorListener> listeners = new ArrayList<>();
    private List<String> shortcutsCreated = new ArrayList<>();
    private Object modelId;

    private Map<Method, Optional<Method>> mvs = new HashMap<>();
    private Object parent;
    private Map<String, Object> initialValues;
    private CssLayout links;
    private Map<String, List<AbstractAction>> actionsPerSection = new HashMap<>();
    private VerticalLayout actionsContainer;
    private String focusedSectionId;
    private Map<String, List<AbstractAction>> actionsPerField = new HashMap<>();
    private BindedWindow creatorWindow;

    private Runnable beforeOpen;
    private TabSheet sectionTabSheet;
    private Component selectedTab;

    public Runnable getBeforeOpen() {
        return beforeOpen;
    }

    public void setBeforeOpen(Runnable beforeOpen) {
        this.beforeOpen = beforeOpen;
    }

    public BindedWindow getCreatorWindow() {
        return creatorWindow;
    }

    public String getFocusedSectionId() {
        return focusedSectionId;
    }

    public void setFocusedSectionId(String focusedSectionId) {
        this.focusedSectionId = focusedSectionId;
    }

    public void setFocusedSection(TabSheet sectionTabSheet, Component selectedTab) {
        this.sectionTabSheet = sectionTabSheet;
        this.selectedTab = selectedTab;
        setFocusedSectionId("" + selectedTab.getId());
    }


    public void setCreateSaveButton(boolean createSaveButton) {
        this.createSaveButton = createSaveButton;
    }

    public boolean isCreateSaveButton() {
        return createSaveButton;
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.EDIT;
    }

    public void setKpisContainer(Layout kpisContainer) {
        this.kpisContainer = kpisContainer;
        if (kpis != null) kpisContainer.addComponent(kpis);
    }

    @Override
    public void setParentView(AbstractViewComponent parentView) {
        super.setParentView(parentView);
        if (parentView instanceof ListViewComponent) setListViewComponent((ListViewComponent) parentView);
    }

    public ListViewComponent getListViewComponent() {
        return listViewComponent;
    }

    public void setListViewComponent(ListViewComponent listViewComponent) {
        this.listViewComponent = listViewComponent;
    }

    public boolean isModificado() {
        return isModificado(initialValues, buildSignature());
    }

    public static boolean isModificado(Map<String, Object> initialValues, Map<String, Object> currentValues) {
        boolean modificado = false;
        if (initialValues != null) {
            for (String k : initialValues.keySet()) {
                Object v0 = initialValues.get(k);
                Object v = currentValues.get(k);
                if (
                        !(v0 != null && v0 instanceof String && "".equals(v0) && v == null)
                                && !(v != null && v instanceof String && "".equals(v) && v0 == null)
                                && ((v0 == null && v != null) || (v0 != null && !v0.equals(v)))
                ) {
                    System.out.println("modificado. k=" + k + ",v0=" + v0 + ",v=" + v);
                    modificado = true;
                    break;
                }
            }
        }
        return modificado;
    }

    public boolean isNewRecord() {
        return newRecord;
    }

    public void setModelType(Class modelType) {
        this.modelType = modelType;
    }

    public void addEditorListener(EditorListener listener) {
        listeners.add(listener);
    }


    public MDDBinder getBinder() {
        return binder;
    }

    public List<Object> getMergeables() {
        return binder.getMergeables();
    }

    public List<Object> getRemovables() {
        return binder.getRemovables();
    }

    public EditorViewComponent(Class modelType) {
        this(modelType, true);
    }

    public EditorViewComponent(ListViewComponent listViewComponent, Class modelType) {
        this(listViewComponent, modelType, true);
    }

    public EditorViewComponent(ListViewComponent listViewComponent, Object owner, FieldInterfaced field, Class modelType, List<FieldInterfaced> visibleFields, List<FieldInterfaced> hiddenFields, boolean createSaveButton) {
        this.listViewComponent = listViewComponent;
        this.owner = owner;
        this.field = field;
        this.modelType = modelType;
        this.visibleFields = visibleFields;
        this.hiddenFields = hiddenFields;
        this.createSaveButton = createSaveButton;
    }

    public EditorViewComponent(Object owner, FieldInterfaced field, Class modelType, boolean createSaveButton) {
        this(null, owner, field, modelType, new ArrayList<>(), new ArrayList<>(), createSaveButton);
    }

    public EditorViewComponent(Class modelType, boolean createSaveButton) {
        this(null, null, modelType, createSaveButton);
    }

    public EditorViewComponent(ListViewComponent listViewComponent, Class modelType, boolean createSaveButton) {
        this(null, null, null, modelType, new ArrayList<>(), new ArrayList<>(), createSaveButton);
    }

    public EditorViewComponent(Object model) {
        this(model, true);
    }

    public EditorViewComponent(Object model, Component lastViewComponent) {
        this(lastViewComponent instanceof ListViewComponent? (ListViewComponent) lastViewComponent :null, model, null, null, true);
    }

    public EditorViewComponent(Object model, List<FieldInterfaced> visibleFields, List<FieldInterfaced> hiddenFields) {
        this(model, visibleFields, hiddenFields, true);
    }

    public EditorViewComponent(ListViewComponent listViewComponent, Object model, List<FieldInterfaced> visibleFields, List<FieldInterfaced> hiddenFields) {
        this(listViewComponent, model, visibleFields, hiddenFields, true);
    }

    public EditorViewComponent(Object model, boolean createSaveButton) {
        this(model, new ArrayList<>(), new ArrayList<>(), createSaveButton);
    }

    public EditorViewComponent(Object model, List<FieldInterfaced> visibleFields, List<FieldInterfaced> hiddenFields, boolean createSaveButton) {
        this(null, model, visibleFields, hiddenFields, createSaveButton);
    }

    public EditorViewComponent(ListViewComponent listViewComponent, Object model, List<FieldInterfaced> visibleFields, List<FieldInterfaced> hiddenFields, boolean createSaveButton) {
        this.listViewComponent = listViewComponent;
        this.modelType = model.getClass();
        this.visibleFields = visibleFields;
        this.hiddenFields = hiddenFields;
        this.createSaveButton = !(this instanceof OwnedCollectionComponent) && createSaveButton;
        setModel(model);
    }


    public Object getModel() {
        return (binder != null)?binder.getBean():null;
    }

    public void setModel(Object model) {
        setModel(model, true);
    }

    public void setModel(Object model, boolean updateChangesSignature) {
        if (model != null) {
            modelType = model.getClass();
        } else {
            try {
                model = modelType.newInstance();
            } catch (Exception e) {
                MDD.alert(e);
            }
        }

        Object old = getModel();
        if (old != null) listeners.remove(old);
        if (model instanceof EditorListener) addEditorListener((EditorListener) model);

        binder = new MDDBinder(model.getClass(), this);

        if (model != null && model.getClass().isAnnotationPresent(Entity.class)) {
            modelId = ReflectionHelper.getId(model);
        }

        binder.setBean(model);

        String oldFocusedSectionId = focusedSectionId;

        build(model);

        if (!Strings.isNullOrEmpty(oldFocusedSectionId) && sectionTabSheet != null) {
            sectionTabSheet.setSelectedTab(Integer.parseInt(oldFocusedSectionId));
            focusedSectionId = oldFocusedSectionId;
        }

        rebuildActions();

        /*
        for (Optional<Method> omv : mvs.values()) {
            if (omv.isPresent()) {

                Method mv = omv.get();

                if (mv.isAnnotationPresent(DependsOn.class)) {
                    String fns = mv.getAnnotation(DependsOn.class).value();

                    if (fns != null) {
                        for (String fn : fns.split(",")) {
                            fn = fn.trim();
                            getBinder().getBinding(fn).ifPresent(b -> ((Binder.Binding)b).getField().addValueChangeListener(e -> {

                                if (getBinder().getBean() != null) { // puede pasar que llamemos a este método cuando todavía no hayamos bindeado nada

                                    rebuildActions();

                                }

                            }));
                        }
                    }
                }


            }

        }
         */

        if (updateChangesSignature) initialValues = buildSignature();
    }

    public Map<String,Object> buildSignature() {
        return buildSignature(getModel());
    }

    public static Map<String, Object> buildSignature(Object m) {
        Map<String, Object> s = new HashMap<>();
        if (m != null && m instanceof HasChangesSignature) {
            s.put("signature", ((HasChangesSignature) m).getChangesSignature());
        } else if (m != null) {
            List<Object> seen = new ArrayList<>();
            seen.add(m);
            for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(m.getClass())) if (!Modifier.isTransient(f.getModifiers())) {
                try {
                    Object v = ReflectionHelper.getValue(f, m);
                    if (v != null) {
                        if (v instanceof Collection) {
                            Collection col = (Collection) v;
                            String cols = "";
                            for (Object o : col) {
                                if (!"".equals(cols)) cols += ",";
                                cols += "" + serialize(seen, o);
                            }
                            s.put(f.getName(), cols);
                            continue;
                        }
                    } else v = "";
                    s.put(f.getName(), "" + v);
                } catch (Exception e) {
                    s.put(f.getName(), null);
                }
            }
        }
        return s;
    }

    public static String serialize(List<Object> seen, Object o) {
        String s = "";
        if (o != null) {
            if (seen.contains(o)) return "" + o;
            seen.add(o);
            for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(o.getClass())) {
                if (!"".equals(s)) s += "&";
                try {
                    s += f.getName() + "=";
                    Object v = ReflectionHelper.getValue(f, o);
                    if (v != null) {
                        if (v instanceof Collection) {
                            String z = "";
                            for (Object i : (Collection) v) {
                                if (!"".equals(z)) z += "|";
                                z += serialize(seen, i);
                            }
                            v = z;
                        } else if (v instanceof Map) {
                            String z = "";
                            for (Object i : ((Map) v).keySet()) {
                                if (!"".equals(z)) z += "|";
                                z += serialize(seen, i) + "->" + serialize(seen, ((Map) v).get(i));
                            }
                            v = z;
                        }
                    } else v = "";
                    s += v;
                } catch (Exception e) {
                    s += f.getName() + "=exception";
                }
            }
        } else s = "null";
        return s;
    }

    private void build(Object model) {

        if (isResource(model)) {
            model = new ResourceModel((Resource) model);
        }

        long t0 = System.currentTimeMillis();

        if (model != null && model.getClass().isAnnotationPresent(Entity.class)) {
            Object id = ReflectionHelper.getId(model);
            boolean esNuevo = false;
            if (id instanceof Long) {
                esNuevo = ((Long) id) == 0;
            } else if (id instanceof Integer) {
                esNuevo = ((Integer)id) == 0;
            } else {
                esNuevo = id == null;
            }
            newRecord = esNuevo;
        }

        clear();

        componentsToLookForErrors = new ArrayList<>();

        MDDUI.get().getNavegador().getViewProvider().setCurrentEditor(this);

        try {
            if (panel == null) build();

            if (links != null) links.removeAllComponents();


            getActions();

            updateSubheader(model);

            panelContenido = new Panel();
            panelContenido.addStyleName(ValoTheme.PANEL_BORDERLESS);
            panelContenido.addStyleName(CSS.NOPADDING);
            panelContenido.addStyleName("panelContenido");
            panelContenido.setSizeFull();

            if (kpis == null) {
                kpis = new CssLayout();
                kpis.addStyleName(CSS.NOPADDING);
            }

            if (kpisContainer != null) {
                kpisContainer.removeAllComponents();
                kpisContainer.addComponent(kpis);
            }


            if (!(this instanceof FiltersViewFlowComponent)) {
                List<FieldInterfaced> kpiFields = ReflectionHelper.getKpiFields(model.getClass());
                if (kpiFields.size() > 0) {
                    if (kpis != null) {
                        kpis.removeAllComponents();

                        for (FieldInterfaced kpi : kpiFields) {
                            kpis.addComponent(createKpi(binder, kpi));
                        }
                    }
                }
            }


            panel.setContent(panelContenido);


            List<FieldInterfaced> fields = ReflectionHelper.getAllEditableFields(model.getClass(), (owner != null) ? owner.getClass() : null, false, field);

            removeUneditableFields(fields);

            if (visibleFields != null && visibleFields.size() > 0) {
                fields.removeIf(f -> !visibleFields.contains(f));
            }

            if (hiddenFields != null && hiddenFields.size() > 0) {
                fields.removeIf(f -> hiddenFields.contains(f));
            }

            FormLayoutBuilderParameters.FormLayoutBuilderParametersBuilder params = FormLayoutBuilderParameters.builder().validators(validators).allFields(fields).links(links).actionsPerSection(actionsPerSection);
            if (this instanceof FiltersViewFlowComponent) params = params.forSearchFilters(true).forSearchFiltersExtended(true).createSections(false).createTabs(false);
            Pair<Component, AbstractStylist> r = FormLayoutBuilder.get().build(this, binder, model.getClass(), model, componentsToLookForErrors, params.build(), actionsPerField);

            stylist = r.getValue();

            panelContenido.setContent(r.getKey());


            AbstractStylist finalStylist = stylist;
            /*
            binder.addValueChangeListener(e -> {
                MDDUI.get().access(new Runnable() {
                    @Override
                    public void run() {
                        //rebuildActions();
                        binder.setBean(binder.getBean(), false); // para campos calculados
                    }
                });
            });
             */

            if (getView() != null) getView().updateViewTitle(toString());

            if (links != null) {
                if (links.getComponentCount() == 0) links.setVisible(false);
                else links.setVisible(true);
            }

            focusFirstField(panelContenido.getContent());

        } catch (Exception e) {
            MDD.alert(e);
        }

        rebuildActions();

        actionsByMethod.keySet().forEach(x -> {
            List<DependsOn> as = new ArrayList<>();
            Method m = (Method) x;
            if (m.isAnnotationPresent(DependsOn.class)) as.add(m.getAnnotation(DependsOn.class));
            Method mv = ReflectionHelper.getMethod(modelType, ReflectionHelper.getGetter(m.getName()).replaceFirst("get", "is") + "Visible");
            if (mv != null && mv.isAnnotationPresent(DependsOn.class)) as.add(mv.getAnnotation(DependsOn.class));

            for (DependsOn a : as) {
                for (String fn : a.value().split(",")) {
                    fn = fn.trim();
                    String finalFn = fn;
                    binder.getBinding(fn).ifPresentOrElse(b -> ((Binder.Binding)b).getField().addValueChangeListener(e -> {

                        //((AbstractAction)actionsByMethod.get(m)).run(new AbstractMDDExecutionContext());

                        rebuildActions();

                    }), () -> {
                        binder.forField(new AbstractField() {
                            Object v = null;
                            @Override
                            protected void doSetValue(Object o) {
                                v = o;
                            }

                            @Override
                            public Object getValue() {
                                return v;
                            }
                        }).bind(finalFn);
                    });
                }
            }
        });

        log.debug("editor component built in " + (System.currentTimeMillis() - t0) + " ms.");

    }

    private void updateSubheader(Object model) {
        if (subheader != null) {
            subheader.removeAllComponents();
            if (model != null && model instanceof HasHeader) {
                Component h = createSubheader(((HasHeader) model).getHeader());
                if (h != null) {
                    subheader.addComponent(h);
                    subheader.setVisible(true);
                } else {
                    subheader.setVisible(false);
                }

            }
        }
    }

    private Component createSubheader(Header header) {
        if (header == null) return null;
        Label l = new Label(header.getMessage());
        l.addStyleName("subheader");
        String s = null;
        switch (header.getType()) {
            case Info: s = "v-label-info"; break;
            case Success: s = ValoTheme.LABEL_SUCCESS; break;
            case Warning: s = "v-label-warning"; break;
            case Error: s = ValoTheme.LABEL_FAILURE; break;
        }
        l.addStyleName(s);
        return l;
    }

    private boolean isResource(Object model) {
        boolean r = false;
        if (model instanceof Resource) r = true;
        else if (model instanceof List && Resource.class.equals(ReflectionHelper.getGenericClass(model.getClass()))) r = true;
        return r;
    }

    protected void removeUneditableFields(List<FieldInterfaced> fields) {
    }

    private boolean focusFirstField(Component c) {
        if (c instanceof AbstractField) {
            if (c instanceof TextField) ((TextField)c).selectAll();
            ((AbstractField) c).focus();
            return true;
        } else if (c instanceof ComboBox) {
            ((ComboBox) c).focus();
            return true;
        } else if (c instanceof HasComponents) {
            HasComponents l = (HasComponents) c;
            for (Component ch : l) {
                if (focusFirstField(ch)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Component createKpi(MDDBinder binder, FieldInterfaced kpi) {

        VerticalLayout vl = new VerticalLayout();
        vl.addStyleName("kpi");
        vl.setWidthUndefined();

        vl.addComponent(new Label(ReflectionHelper.getCaption(kpi)));

        Label l;
        vl.addComponent(l = new Label());
        l.addStyleName("valor");
        l.setContentMode(ContentMode.HTML);
        Binder.Binding binding = binder.forField(new HasValue() {

            Object v = null;

            @Override
            public void setValue(Object o) {
                v = o;

                String s = "";

                if (double.class.equals(kpi.getType()) && (kpi.isAnnotationPresent(Money.class) || kpi.isAnnotationPresent(Balance.class))) {
                    DecimalFormat df = new DecimalFormat("##,###,###,###,##0.00");
                    s = df.format(v != null?v:0);
                    if (kpi.isAnnotationPresent(Balance.class)) {
                        if (v != null && ((double)v) < 0) {
                            l.addStyleName("negativo");
                            l.removeStyleName("positivo");
                        } else {
                            l.addStyleName("positivo");
                            l.removeStyleName("negativo");
                        }
                    }
                } else {
                    if (v == null) s = "";
                    else {
                        if (v instanceof Boolean) {
                            if ((Boolean) v && !kpi.getAnnotation(KPI.class).reversed()) {
                                s = VaadinIcons.CHECK.getHtml();
                                l.addStyleName(ValoTheme.BUTTON_FRIENDLY);
                            } else {
                                s = VaadinIcons.CLOSE.getHtml();
                                l.addStyleName(ValoTheme.BUTTON_DANGER);
                            }
                            l.addStyleName("centered");
                        } else {
                            s = "" + v;
                        }
                    }
                }
                l.setValue(s);
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
        }).bind(kpi.getName());

        return vl;

    }

    public void updateModel(Object model) {
        if (binder != null) {
            binder.update(model);
            updateSubheader(model);
        }
    }

    @Override
    public void updatePageTitle() {
        super.updatePageTitle();
        if (getModel() != null && getModel() instanceof EditorViewStyler) ((EditorViewStyler) getModel()).apply(this);
    }

    @Override
    public String toString() {
        String t = (stylist != null)?stylist.getViewTitle(newRecord, getModel()):"Not yet";
        return t;
    }

    public Class getModelType() {
        return modelType;
    }

    /*
    @Override
    public Layout getActionsContainer() {
        return actionsContainer;
    }
     */

    @Override
    public EditorViewComponent build() throws Exception {
        actionsContainer = new VerticalLayout();
        actionsContainer.addStyleName("actionscontainer");
        actionsContainer.addStyleName(CSS.NOPADDING);
        actionsContainer.setSizeUndefined();

        super.build();

        log.debug("*******BUILD***************");

        addStyleName("editorviewcomponent");

        if (false) {
            links = new CssLayout();
            links.addStyleName(CSS.NOPADDING);
            addComponent(links);
        }

        panel = new Panel();
        panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        panel.addStyleName(CSS.NOPADDING);
        panel.addStyleName("panelContenedor");
        addComponentsAndExpand(panel);

        return this;
    }

    private void updateActions() {
        if (stylist != null) {
            Object model = binder.getBean();

            for (String k : (Set<String>) menuItemsById.keySet()) {
                Object c = menuItemsById.get(k);
                if (c instanceof MenuBar.MenuItem) ((MenuBar.MenuItem)c).setEnabled(stylist.isActionEnabled(k, model));
                else if (c instanceof Button) ((Button) c).setEnabled(stylist.isActionEnabled(k, model));
            }

        }
        if (bar.getComponentCount() == 0) bar.setVisible(false);
        else bar.setVisible(true);
    }


    @Override
    public void addViewActionsMenuItems(CssLayout bar) {

        boolean isEditingNewRecord = newRecord;
        boolean readOnly = getModel() != null && getModel() instanceof ReadOnly && ((ReadOnly) getModel()).isReadOnly();

        if (modelType.isAnnotationPresent(Entity.class) || PersistentPOJO.class.isAssignableFrom(modelType)) {
            if (field == null && !(this instanceof OwnedCollectionComponent) && !isActionPresent("refresh")) {

                Button i;
                bar.addComponent(i = new Button("", VaadinIcons.REFRESH));
                //bar.addComponent(i = new Button("Refresh", VaadinIcons.REFRESH));
                i.addStyleName(ValoTheme.BUTTON_QUIET);
                i.addClickListener(e -> {
                    try {

                        load(modelId);

                    } catch (Throwable throwable) {
                        MDD.alert(throwable);
                    }
                });

                addMenuItem("refresh", i);

                i.setClickShortcut(ShortcutAction.KeyCode.R, ShortcutAction.ModifierKey.CTRL);

                shortcutsCreated.add("refresh");
            }


            if (true || (!(this instanceof FiltersViewFlowComponent) && !isNewRecord() && listViewComponent != null)) {
                if (!isActionPresent("prev")) {

                    Button i;
                    bar.addComponent(i = new Button("", VaadinIcons.ARROW_UP));
                    //bar.addComponent(i = new Button("Prev", VaadinIcons.ARROW_UP));
                    i.addStyleName(ValoTheme.BUTTON_QUIET);
                    i.addClickListener(e -> {
                        try {

                            Object xid = listViewComponent.getPrevious(modelId);

                            if (xid != null) {
                                MDDUI.get().getNavegador().goSibling(xid);
                            }


                        } catch (Throwable throwable) {
                            MDD.notify(throwable);
                        }
                    });

                    addMenuItem("prev", i);

                    i.setClickShortcut(ShortcutAction.KeyCode.ARROW_UP, ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.ALT);

                    shortcutsCreated.add("prev");

                }

                if (!isActionPresent("next")) {

                    Button i;
                    bar.addComponent(i = new Button("", VaadinIcons.ARROW_DOWN));
                    //bar.addComponent(i = new Button("Next", VaadinIcons.ARROW_DOWN));
                    i.addStyleName(ValoTheme.BUTTON_QUIET);
                    i.addClickListener(e -> {
                        try {

                            Object xid = listViewComponent.getNext(modelId);

                            if (xid != null) {
                                MDDUI.get().getNavegador().goSibling(xid);
                            }

                        } catch (Throwable throwable) {
                            MDD.notify(throwable);
                        }
                    });
                    addMenuItem("next", i);
                    i.setClickShortcut(ShortcutAction.KeyCode.ARROW_DOWN, ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.ALT);

                    shortcutsCreated.add("next");

                }
            }
        }

        if (!readOnly && createSaveButton && (modelType.isAnnotationPresent(Entity.class) || PersistentPOJO.class.isAssignableFrom(modelType)) && (isNewRecord() || !modelType.isAnnotationPresent(Unmodifiable.class))) {

            if (!isActionPresent("save")) {

                Button i;
                bar.addComponent(i = new Button("", VaadinIcons.DOWNLOAD));
                //bar.addComponent(i = new Button("Save", VaadinIcons.DOWNLOAD));
                i.addStyleName(ValoTheme.BUTTON_QUIET);
                i.addClickListener(e -> {
                    try {

                        BinderValidationStatus v = binder.validate();

                        if (v.isOk()) {

                            preSave();

                            save();

                        } else {
                            for (Component c : componentsToLookForErrors) {
                                if (c instanceof AbstractComponent && ((AbstractComponent) c).getComponentError() != null) {
                                    if (c instanceof Focusable) ((Focusable) c).focus();
                                    break;
                                }
                            }
                            MDD.alert(v);
                        }


                    } catch (Throwable throwable) {
                        MDD.alert(throwable);
                    }
                });
                addMenuItem("save", i);
                i.setClickShortcut(ShortcutAction.KeyCode.S, ShortcutAction.ModifierKey.CTRL);

                if (!shortcutsCreated.contains("saveandstay")) {

                    Button b;
                    getHiddens().addComponent(b = new Button());
                    b.addClickListener(e -> {
                        BinderValidationStatus v = binder.validate();

                        if (v.isOk()) {
                            try {

                                preSave();

                                save(false);

                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }


                        } else MDD.alert(v);
                    });
                    b.setClickShortcut(ShortcutAction.KeyCode.S, ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.ALT);

                    shortcutsCreated.add("saveandstay");
                }

                if (!shortcutsCreated.contains("new")) {

                    Button b;
                    getHiddens().addComponent(b = new Button());
                    b.addClickListener(e -> {
                        BinderValidationStatus v = binder.validate();

                        if (v.isOk()) {
                            try {

                                preSave();

                                save(false);

                                Object old = getBinder().getBean();

                                //load(null);

                                Object current = ReflectionHelper.newInstance(modelType, parent);
                                newRecord = true;
                                for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(old.getClass())) {
                                    if (f.isAnnotationPresent(Keep.class)) {
                                        ReflectionHelper.setValue(f, current, ReflectionHelper.getValue(f, old));
                                    }
                                }
                                getBinder().setBean(current, false);

                                MDD.updateTitle(getTitle());

                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }


                        } else MDD.alert(v);
                    });
                    b.setClickShortcut(ShortcutAction.KeyCode.N, ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.ALT);

                    shortcutsCreated.add("new");
                }

                if ((modelType.isAnnotationPresent(Entity.class) || PersistentPOJO.class.isAssignableFrom(modelType)) && !modelType.isAnnotationPresent(NonDuplicable.class) && !isActionPresent("duplicate") && !isNewRecord()) {

                    if (!shortcutsCreated.contains("duplicate")) {

                        Button b;
                        getHiddens().addComponent(b = new Button());
                        b.addClickListener(e -> {
                            try {

                                BinderValidationStatus v = binder.validate();

                                if (v.isOk()) {
                                    try {

                                        preSave();

                                        save(false);

                                        Object old = getBinder().getBean();

                                        //load(null);

                                        Object current = ReflectionHelper.newInstance(modelType, parent);
                                        newRecord = true;
                                        for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(old.getClass())) {
                                            if (Collection.class.isAssignableFrom(f.getType())) {
                                                //todo: clonar colecciones y mapas
                                            } else ReflectionHelper.setValue(f, current, ReflectionHelper.getValue(f, old));
                                        }
                                        getBinder().setBean(current, false);
                                        setModel(current);
                                        modelId = null;
                                        MDD.updateTitle(getTitle());

                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }


                                } else MDD.alert(v);


                            } catch (Throwable throwable) {
                                MDD.alert(throwable);
                            }
                        });
                        b.setClickShortcut(ShortcutAction.KeyCode.D, ShortcutAction.ModifierKey.CTRL);

                        shortcutsCreated.add("duplicate");
                    }

                }

            }

            if (modelType == null || (!modelType.isAnnotationPresent(ModifyValuesOnly.class) && !modelType.isAnnotationPresent(NewNotAllowed.class))) if (field == null || ReflectionHelper.puedeAnadir(field)) {
                    if (!isActionPresent("add")) {
                        Button i;
                        bar.addComponent(i = new Button("", VaadinIcons.PLUS));
                        //bar.addComponent(i = new Button("Add", VaadinIcons.PLUS));
                        i.addStyleName(ValoTheme.BUTTON_QUIET);
                        i.addClickListener(e -> {
                            try {

                                BinderValidationStatus v = binder.validate();

                                if (v.isOk()) {
                                    try {

                                        preSave();

                                        save(false);

                                        Object old = getBinder().getBean();

                                        //load(null);

                                        load(null);
                                        Object current = getModel();
                                        for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(old.getClass())) {
                                            if (f.isAnnotationPresent(Keep.class)) {
                                                ReflectionHelper.setValue(f, current, ReflectionHelper.getValue(f, old));
                                            }
                                        }
                                        getBinder().setBean(current, false);
                                        setModel(current);
                                        modelId = null;
                                        MDD.updateTitle(getTitle());

                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }


                                } else MDD.alert(v);


                            } catch (Throwable throwable) {
                                MDD.alert(throwable);
                            }
                        });
                        addMenuItem("add", i);
                    }
            }

            if (modelType == null || (!modelType.isAnnotationPresent(ModifyValuesOnly.class) && !modelType.isAnnotationPresent(Indelible.class))) if (field == null || ReflectionHelper.puedeBorrar(field)) {
                if (!isActionPresent("remove")) {
                    Button i;
                    bar.addComponent(i = new Button("", VaadinIcons.MINUS));
                    //bar.addComponent(i = new Button("Remove", VaadinIcons.MINUS));
                    i.addStyleName(ValoTheme.BUTTON_QUIET);
                    i.addClickListener(e -> {

                        MDD.confirm("Are you sure you want to delete this item?", () -> {

                            try {

                                Object xid = null;
                                try {
                                    xid = listViewComponent.getNext(modelId);
                                } catch (Throwable throwable) {
                                    try {
                                        xid = listViewComponent.getPrevious(modelId);
                                    } catch (Throwable throwable2) {
                                    }
                                }

                                if (!isEditingNewRecord) {
                                    Helper.transact(em -> {
                                        ReflectionHelper.delete(em, em.find(getModelType(), ReflectionHelper.getId(getModel())));
                                    });
                                }

                                if (xid != null) {
                                    MDDUI.get().getNavegador().goSibling(xid);
                                } else {
                                    goBack();
                                }

                            } catch (Throwable ex) {
                                MDD.alert(ex);
                            }
                        });

                    });
                    addMenuItem("remove", i);
                };
            }


        }

        if (getMenuItemById("prev") != null) getMenuItemById("prev").setVisible(!isNewRecord() && listViewComponent != null);
        if (getMenuItemById("next") != null) getMenuItemById("next").setVisible(!isNewRecord() && listViewComponent != null);
        if (getMenuItemById("duplicate") != null) getMenuItemById("duplicate").setVisible(!isNewRecord());
        if (getMenuItemById("add") != null && (getView() == null || getView().getWindowContainer() != null || this instanceof OwnedCollectionComponent)) getMenuItemById("add").setVisible(false);
        if (getMenuItemById("remove") != null && (getView() == null || getView().getWindowContainer() != null || this instanceof OwnedCollectionComponent)) getMenuItemById("remove").setVisible(false);
        if (getMenuItemById("save") != null && this instanceof OwnedCollectionComponent) getMenuItemById("save").setVisible(false);

        super.addViewActionsMenuItems(bar);
    }


    public AbstractStylist getStylist() {
        return stylist;
    }

    public void setStylist(AbstractStylist stylist) {
        this.stylist = stylist;
    }


    @Override
    public List<AbstractAction> getActions() {

        Object bean = (binder != null)?binder.getBean():null;


        boolean isEditingNewRecord = MDDUI.get().isEditingNewRecord();


        List<Method> ms = new ArrayList<>();
        List<Method> mvf = new ArrayList<>();

        for (Method m : ReflectionHelper.getAllMethods(modelType)) {
            if (!(
                    Modifier.isStatic(m.getModifiers())
                            || (m.isAnnotationPresent(NotWhenCreating.class) && isEditingNewRecord)
                            || (m.isAnnotationPresent(NotWhenEditing.class) && !isEditingNewRecord))
                    && m.isAnnotationPresent(Action.class) && (isCreateSaveButton() || (!m.getAnnotation(Action.class).saveAfter() && !m.getAnnotation(Action.class).saveBefore()))
            ) {

                Optional<Method> omv = mvs.get(m);

                if (omv == null) {

                    Method mv = ReflectionHelper.getMethod(modelType, ReflectionHelper.getGetter(m.getName()).replaceFirst("get", "is") + "Visible");

                    omv = mv != null?Optional.of(mv):Optional.empty();

                    mvs.put(m, omv);
                }

                Method mv = null;
                if (omv.isPresent()) mv = omv.get();
                try {

                    if (mv == null || bean == null || (Boolean) mv.invoke(bean)) {
                        (Strings.isNullOrEmpty(m.getAnnotation(Action.class).attachToField())?ms:mvf).add(m);
                    }

                } catch (Exception e) {
                    MDD.alert(e);
                }
            }
        }

        ms.sort((a, b) -> {
            return a.getAnnotation(Action.class).order() - b.getAnnotation(Action.class).order();
        });

        mvf.sort((a, b) -> {
            return a.getAnnotation(Action.class).order() - b.getAnnotation(Action.class).order();
        });

        actionsPerSection = new HashMap<>();
        ms.forEach(m -> {
            Action aa = m.getAnnotation(Action.class);
            actionsPerSection.putIfAbsent(aa.section(), new ArrayList<>());
            List<AbstractAction> l = actionsPerSection.get(aa.section());
            AbstractAction a;
            l.add(a = ViewComponentHelper.createAction(m, this));
        });

        actionsPerField = new HashMap<>();
        mvf.forEach(m -> {
            Action aa = m.getAnnotation(Action.class);
            actionsPerField.putIfAbsent(aa.attachToField(), new ArrayList<>());
            List<AbstractAction> l = actionsPerField.get(aa.attachToField());
            AbstractAction a;
            l.add(a = ViewComponentHelper.createAction(m, this));
        });

        return actionsPerSection.getOrDefault("", new ArrayList<>());
    }


    public Method getMethod(String methodName) {

        Method a = null;

        for (Method m : ReflectionHelper.getAllMethods(getModelType())) {
            if (!Modifier.isStatic(m.getModifiers()) && m.getName().equals(methodName)) {
                a = m;
                break;
            }
        }

        return a;

    }


    public FieldInterfaced getField(String fieldName) {

        FieldInterfaced a = null;

        for (FieldInterfaced m : ReflectionHelper.getAllFields(getModelType())) {
            if (m.getName().equals(fieldName) || (m.getName() + "_search").equals(fieldName)) {
                a = m;
                break;
            }
        }

        return a;
    }

    public void save() throws Throwable {
        save(true);
    }

    public void save(boolean goBack) throws Throwable {
        save(goBack, true);
    }

    public void preSave() throws Throwable {
        for (EditorListener l : listeners) l.preSave(getModel());
    }

    public void save(boolean goBack, boolean notify) throws Throwable {
        save(goBack, notify, false);
    }

    public void save(boolean goBack, boolean notify, boolean copyEditableValues) throws Throwable {

        if (validate()) {

            try {

                if (modelType.isAnnotationPresent(Entity.class)) {

                    Object m = getModel();
                    Object[] merged = new Object[1];

                    MDDUI ui = MDDUI.get();

                    Helper.transact(new JPATransaction() {
                        @Override
                        public void run(EntityManager em) throws Throwable {

                            if (copyEditableValues) {
                                Object d = transferirValores(em, m, new ArrayList<>());
                                setModel(d);
                                merged[0] = d;
                            } else {
                                for (Object o : getRemovables()) {
                                    if (getMergeables().contains(o)) getMergeables().remove(o);

                                    if (em.contains(o)) em.remove(o);
                                    else if (em.find(o.getClass(), ReflectionHelper.getId(o)) != null) {
                                        em.remove(em.merge(o));
                                    }
                                }
                                for (Object o : getMergeables()) em.merge(o);

                                ReflectionHelper.auditar(m);

                                merged[0] = em.merge(m);
                            }

                        }
                    }, () -> {
                        System.out.println("entramos en el callback");
                        if (!goBack) {
                            System.out.println("actualizamos el ui");
                            ui.access(() -> {
                                try {
                                    Helper.notransact(em -> {
                                        if (modelId != null) {
                                            binder.update(em.find(merged[0].getClass(), modelId));
                                        }
                                        initialValues = buildSignature();
                                    });
                                    MDD.updateTitle(getTitle());
                                } catch (Throwable throwable) {
                                    MDD.alert(throwable);
                                }
                            });
                        }
                    });

                    modelId = ReflectionHelper.getId(merged[0]);

                    Helper.notransact(em -> {
                        binder.update(em.find(merged[0].getClass(), modelId));
                        initialValues = buildSignature();
                    });

                    if (goBack) goBack();

                } else if (PersistentPOJO.class.isAssignableFrom(modelType)) {

                    PersistentPOJO ppojo = (PersistentPOJO) getModel();

                    ppojo.save();

                }

                if (notify) listeners.forEach(l -> l.onSave(getModel()));

                if (!goBack && (modelType.isAnnotationPresent(Entity.class)) || PersistentPOJO.class.isAssignableFrom(modelType)) {
                    load(modelId);
                }

                if (getView() != null) getView().updateViewTitle(toString());

            } catch (OptimisticLockException ole) {
                MDD.confirm("Some objects have been modified by someone else. You should refresh and recover any modification you have done. Do you want to go ahead and overwrite instead?", () -> {
                    try {
                        save(goBack, notify, true);
                    } catch (Throwable throwable) {
                        MDD.alert(throwable);
                    }
                });
            }

        }

    }

    public boolean validate() {
        return validate(false);
    }

    public boolean validate(boolean silent) {
        boolean noerror = true;
        BinderValidationStatus status = binder.validate();
        if (false && status.hasErrors()) {
            noerror = false;
        }
        for (Component c : componentsToLookForErrors) if (c.isVisible() && c.getParent().isVisible()) {
            if (c instanceof AbstractComponent && ((AbstractComponent) c).getComponentError() != null) {
                noerror = false;
                if (c instanceof Focusable) ((Focusable) c).focus();
                break;
            }
        }
        if (!noerror && !silent) MDD.notifyError("Please solve errors for all fields");
        return noerror;
    }

    private Object transferirValores(EntityManager em, Object m, List<Object> transferidos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object d = em.find(m.getClass(), ReflectionHelper.getId(m));
        if (d != null) {
            for (FieldInterfaced f : ReflectionHelper.getAllEditableFields(m.getClass()))
                if (!f.isAnnotationPresent(Output.class) && !f.isAnnotationPresent(KPI.class) && !f.isAnnotationPresent(KPIInline.class)) {
                    Object v = ReflectionHelper.getValue(f, m);
                    boolean transferir = true;
                    if (v instanceof Collection) {
                        if (ReflectionHelper.isOwnedCollection(f)) {
                            Collection aux = new ArrayList();
                            if (v instanceof Collection) {
                                if (v instanceof Set) aux = new HashSet();
                            }
                            Collection finalAux = aux;
                            for (Object x : ((Collection)v)) {
                                finalAux.add(x.getClass().isAnnotationPresent(Entity.class) && !transferidos.contains(x)? transferirValores(em, x, transferidos) : x);
                            };
                            v = aux;
                        } else transferir = false;
                    }
                    if (transferir) ReflectionHelper.setValue(f, d, v);
                }
        } else {
            d = m;
            em.persist(d);
        }

        ReflectionHelper.auditar(d);
        return d;
    }


    public void load(Object id, Object parent, FieldInterfaced field) throws Throwable {
        this.modelId = id;
        this.parent = parent;
        if (id == null) {
            newRecord = true;

            Set<Class> subClasses = ReflectionHelper.getSubclasses(modelType);

            if (subClasses.size() > 1) {

                Set<ClassOption> subClassesOptions = new LinkedHashSet<>();
                subClasses.forEach(c -> subClassesOptions.add(new ClassOption(c)));

                VaadinHelper.choose("Please choose type", subClassesOptions, c -> {
                    try {
                        create(((ClassOption)c).get_class(), parent);
                    } catch (Throwable e) {
                        MDD.alert(e);
                    }
                }, () -> {
                    MDDUI.get().getNavegador().goBack();
                });
            } else if (subClasses.size() == 1) {
                create(subClasses.iterator().next(), parent);
            } else {
                create(modelType, parent);
            }

        } else {
            newRecord = false;

            if (modelType.isAnnotationPresent(Entity.class)) {

                Helper.notransact(new JPATransaction() {
                    @Override
                    public void run(EntityManager em) throws Throwable {
                        setModel(em.find(modelType, id));
                    }
                });

            } else if (PersistentPOJO.class.isAssignableFrom(modelType)) {
                PersistentPOJO ppojo = null;

                if (id != null && modelType.equals(id.getClass())) ppojo = (PersistentPOJO) id;
                else ppojo = (PersistentPOJO) modelType.newInstance();

                ppojo.load(id);


                if (parent != null) {
                    for (FieldInterfaced f : ReflectionHelper.getAllFields(ppojo.getClass())) if (f.getType().equals(parent.getClass()) && f.isAnnotationPresent(NotNull.class)) {
                        ReflectionHelper.setValue(f, ppojo, parent);
                        break;
                    }
                }

                setModel(ppojo);

            } else {

                if (parent != null) {
                    for (FieldInterfaced f : ReflectionHelper.getAllFields(id.getClass())) if (f.getType().equals(parent.getClass()) && f.isAnnotationPresent(NotNull.class)) {
                        ReflectionHelper.setValue(f, id, parent);
                        break;
                    }
                }


                setModel(id);
            }

        }

    }

    private void create(Class type, Object parent) throws Throwable {
        Constructor con = parent != null?ReflectionHelper.getConstructor(type, parent.getClass()):ReflectionHelper.getConstructor(type);
        if (con != null && con.getParameterCount() > 0) {
            VaadinHelper.fill(this, "I need some data", con, i -> {
                try {
                    setModel(i);
                } catch (Throwable e) {
                    MDD.alert(e);
                }
                MDDUI.closeWindow(false);
            }, () -> MDDUI.get().getNavegador().goBack());
        } else setModel(ReflectionHelper.newInstance(type, parent));
    }



    public void load(Object id) throws Throwable {
        load(id, null, null);
    }

    public void rebuildActions() {
        markAllAsUnseen();
        addViewActionsMenuItems(bar);
        removeUnseen();
        updateActions();
    }

    public void clear() {
        log.debug("*********CLEAR PANEL***********");
        if (panel != null) panel.setContent(null);
    }

    public void onGoBack() {
        if (binder != null) for (EditorListener l : listeners) l.onGoBack(binder.getBean());
    }

    public void goBack() {
        MDDUI.get().getNavegador().goBack();
    }

    public void setCreatorWindow(BindedWindow creatorWindow) {
        this.creatorWindow = creatorWindow;
    }

}
