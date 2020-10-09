package io.mateu.mdd.vaadin.components.views;

import com.google.common.base.Strings;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.ui.MDDUIAccessor;
import io.mateu.mdd.shared.CSS;
import io.mateu.mdd.vaadin.actions.AcctionRunner;
import io.mateu.mdd.vaadin.components.ComponentWrapper;
import io.mateu.mdd.vaadin.components.app.views.firstLevel.AreaComponent;
import io.mateu.mdd.vaadin.navigation.View;
import io.mateu.mdd.vaadin.navigation.ViewStack;
import io.mateu.mdd.vaadin.util.VaadinHelper;
import io.mateu.util.notification.Notifier;

import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractViewComponent<A extends AbstractViewComponent<A>> extends VerticalLayout {

    private final String uuid = UUID.randomUUID().toString();
    private VaadinIcons icon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractViewComponent<?> that = (AbstractViewComponent<?>) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid);
    }

    private Component header;
    private Label titleLabel;
    private CssLayout kpisContainer;
    private View view;
    protected CssLayout bar;
    protected CssLayout subheader;
    protected Map<Method, AbstractAction> actionsByMethod = new HashMap<>();
    protected Map<String, Component> menuItemsById = new HashMap<>();
    protected Map<String, List<Component>> menuItemsByGroup = new HashMap<>();
    protected List<String> menuItemIdsUnseen = new ArrayList<>();
    private String title;
    private HorizontalLayout hiddens;
    private boolean backable;
    private boolean built;
    private AbstractViewComponent parentView;
    private Label iconLabel;
    private Component backButton;

    public AbstractViewComponent getParentView() {
        return parentView;
    }

    public VaadinIcons getIcon() {
        return icon;
    }

    public void setIcon(VaadinIcons icon) {
        this.icon = icon;
    }

    public void setParentView(AbstractViewComponent parentView) {
        this.parentView = parentView;
    }

    public CssLayout getKpisContainer() {
        return kpisContainer;
    }

    public AbstractViewComponent() {
        addStyleName("viewcomponent");

        addComponent(header = createHeader());

        if (!isBarHidden()) {
            bar = new CssLayout();
            bar.addStyleName("actionsbar");
            bar.addStyleName(CSS.NOPADDING);
            menuItemsById = new HashMap<>();
            addBack(bar);
            getActionsContainer().addComponent(bar);
        }

        addComponent(subheader = new CssLayout());
        subheader.addStyleName(CSS.NOPADDING);
        subheader.setWidth("100%");
        subheader.setVisible(false);

    }

    public void hideHeader() {
        header.setVisible(false);
    }

    public boolean expandOnOpen() {
        return false;
    }

    private Component createHeader() {
        HorizontalLayout l = new HorizontalLayout();

        l.addStyleName("viewHeader");

        l.addComponent(createTitleLabel());

        hiddens = new HorizontalLayout();
        hiddens.addStyleName("hidden");

        l.addComponent(hiddens);


        return l;
    }

    public void setStack(ViewStack stack) {
        boolean add = MDDUIAccessor.isMobile();
        int pos = 0;
        if (!add && !(
                this instanceof AreaComponent || this instanceof SearchInMenuComponent
        ) && stack.size() > 0) {
            add = true;
            while (!add && pos < stack.size()) {
                View v = stack.get(pos);
                Component c = v.getComponent();
                if (c instanceof ComponentWrapper) c = ((ComponentWrapper) c).getComponent(0);
                add = true;
                if (!(c instanceof AreaComponent || c instanceof SearchInMenuComponent)) add = true;
                if (!add) pos++;
            }
        }
        if (add
                //&& stack.size() > pos
                && this instanceof AbstractViewComponent) {
            ((AbstractViewComponent) this).setBackable(true);
            if (this instanceof EditorViewComponent) {
                ((EditorViewComponent) this).setKpisContainer(kpisContainer);
                ((EditorViewComponent) this).rebuildActions();
            }
        }
    }

    private Component createTitleLabel() {

        iconLabel = new Label("", ContentMode.HTML);
        iconLabel.addStyleName("viewIcon");

        titleLabel = new Label("", ContentMode.HTML);
        titleLabel.addStyleName("viewTitle");

        kpisContainer = new CssLayout();
        kpisContainer.addStyleName(CSS.NOPADDING);
        kpisContainer.addStyleName("kpisContainer");
        kpisContainer.setSizeUndefined();

        if (getIcon() != null) iconLabel.setValue(getIcon().getHtml());
        else iconLabel.setVisible(false);

        HorizontalLayout hl = new HorizontalLayout(iconLabel, titleLabel, kpisContainer);
        hl.addStyleName(CSS.NOPADDING);

        return hl;
    }


    public void updateViewTitle(String newTitle) {
        title = newTitle;
        updatePageTitle();
    }

    public void updatePageTitle() {
        if (titleLabel != null) {
            titleLabel.setValue(getTitle());
        }
        if (iconLabel != null) {
            if (getIcon() != null) {
                iconLabel.setValue(getIcon().getHtml());
                iconLabel.setVisible(true);
            } else iconLabel.setVisible(false);
        }
        //UI.getCurrent().getPage().setTitle((titleLabel.getValue() != null)?titleLabel.getValue():"No title");
    }

    public HorizontalLayout getHiddens() {
        return hiddens;
    }

    public String getTitle() {
        return title != null?title:toString();
    }

    public String getPageTitle() {
        return getTitle();
    }

    public A setTitle(String title) {
        this.title = title;
        return (A) this;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Layout getActionsContainer() {
        return this;
    }

    public A buildIfNeeded() {
        if (!built) {
            try {
                build();
            } catch (Exception e) {
                Notifier.alert(e);
            }
        }
        return (A) this;
    }

    public A build() throws Exception {
        addViewActionsMenuItems(bar);
        built = true;
        return (A) this;
    }

    private void addBack(CssLayout bar) {
            if (!isActionPresent("back")) {
                Button b;
                bar.addComponent(backButton = b = new Button("", VaadinIcons.ARROW_LEFT));
                //bar.addComponent(i = b = new Button("Back", VaadinIcons.ARROW_LEFT));
                b.addStyleName(ValoTheme.BUTTON_QUIET);
                b.addClickListener(e -> {
                    try {

                        if (AbstractViewComponent.this.beforeBack()) MDDUIAccessor.goBack();

                    } catch (Throwable throwable) {
                        Notifier.alert(throwable);
                    }
                });

                addMenuItem("back", backButton);

            } else {
                backButton = getMenuItemById("back");
            }
            if (isBackable()) backButton.setVisible(true);
    }

    public void addViewActionsMenuItems(CssLayout bar) {

        if (isActionPresent("back")) {
            getMenuItemById("back").setVisible(isBackable());
        }

        for (AbstractAction a : getActions()) {
                Component i = null;
                if (!isActionPresent(a.getId())) {
                    Button b;
                    i = b = new Button(a.getCaption(), a.getIcon());
                    b.addStyleName(ValoTheme.BUTTON_QUIET);
                    b.addClickListener(e -> {
                        try {

                            //añade validación, confirmación y updatea después de ejecutar
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        new AcctionRunner().run(a);
                                    } catch (Throwable ex) {
                                        Notifier.alert(ex);
                                        removeStyleName("refreshonback");
                                    }

                                }
                            };

                            if (!Strings.isNullOrEmpty(a.getConfirmationMessage())) {
                                VaadinHelper.confirm(a.getConfirmationMessage(), () -> {

                                    r.run();

                                    //todo: actualizar vista con los cambios en el modelo

                                });
                            } else r.run();

                        } catch (Throwable throwable) {
                            Notifier.alert(throwable);
                        }
                    });

                    if (!Strings.isNullOrEmpty(a.getGroup())) menuItemsByGroup.computeIfAbsent(a.getGroup(), k -> new ArrayList<>()).add(i);
                    addMenuItem(a.getId(), i);

                    if (!Strings.isNullOrEmpty(a.getStyle())) i.addStyleName(a.getStyle());

                    if (Strings.isNullOrEmpty(a.getGroup())) bar.addComponent(i);

                    a.addShortCut(b);

                } else {
                    i = getMenuItemById(a.getId());
                }
                if (i != null && !Strings.isNullOrEmpty(a.getStyle())) i.addStyleName(a.getStyle());
                i.setVisible(a.isVisible());
            }

        if (bar != null) if  (bar.getComponentCount() == 0 || (bar.getComponentCount() == 1 && !bar.getComponent(0).isVisible())) bar.setVisible(false);
    }

    public boolean beforeBack() {
        return true;
    }

    public void markAllAsUnseen() {
        menuItemIdsUnseen = new ArrayList<>(menuItemsById.keySet());
    }

    public List<String> getUnseenActions() {
        return menuItemIdsUnseen;
    }

    public void removeUnseen() {
        for (String id : menuItemIdsUnseen) menuItemsById.get(id).setVisible(false);
    }

    public boolean isActionPresent(String id) {
        boolean found = menuItemsById.containsKey(id);
        if (found) menuItemIdsUnseen.remove(id);
        return found;
    }

    public Component getMenuItemById(String id) {
        return menuItemsById.get(id);
    }

    public void addMenuItem(String id, Component i) {
        menuItemsById.put(id, i);
    }


    public List<AbstractAction> getActions() {
        return new ArrayList<>();
    }


    public AbstractAction getActionByMethod(Method m) {
        return actionsByMethod.get(m);
    }

    public void setAction(Method m, AbstractAction action) {
        actionsByMethod.put(m, action);
    }


    public boolean isBackable() {
        return backable;
    }

    public void setBackable(boolean backable) {
        this.backable = backable;
        if (backButton != null) backButton.setVisible(backable);
    }

    public boolean isBarHidden() {
        return false;
    }
}
