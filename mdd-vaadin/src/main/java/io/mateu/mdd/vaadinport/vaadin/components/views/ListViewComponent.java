package io.mateu.mdd.vaadinport.vaadin.components.views;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.reflection.ReflectionHelper;

import java.util.*;

public abstract class ListViewComponent extends AbstractViewComponent<ListViewComponent> {

    private ResultsComponent resultsComponent;

    private List<ListViewComponentListener> listeners = new ArrayList<>();

    private int count;
    private Label countLabel;
    private FiltersComponent filtersComponent;
    private Map<AbstractAction, ActionParametersComponent> actionComponents = new HashMap<>();

    @Override
    public ListViewComponent build() throws InstantiationException, IllegalAccessException {

        super.build();

        addStyleName("listviewcomponent");
        
        addComponent(filtersComponent = new FiltersComponent(this));

        addComponent(countLabel = new Label());

        addComponentsAndExpand(resultsComponent = buildResultsComponent());

        buildActionComponents();

        return this;
    }

    protected void buildActionComponents() {
        for (AbstractAction a : getActions()) {
            try {
                actionComponents.put(a, new ActionParametersComponent(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }


    private ResultsComponent buildResultsComponent() {
        return new ResultsComponent(this);
    }

    public abstract void buildColumns(Grid grid);

    public void search(Object filters) throws Throwable {
        resultsComponent.search(filters);
    }

    public void addListener(ListViewComponentListener listener) {
        listeners.add(listener);
    }


    public void edit(Object id) {
        for (ListViewComponentListener l : listeners) l.onEdit(id);
    };

    public abstract List findAll(Object filters, List<QuerySortOrder> sortOrders, int offset, int limit);

    public int count(Object filters) {
        count = gatherCount(filters);
        countLabel.setValue("" + count + " matches.");
        return count;
    }

    protected abstract int gatherCount(Object filters);

    public abstract Object deserializeId(String id);

    public abstract String getPathForEditor(Object id);

    public abstract String getPathForFilters();

    public Class getModelTypeForSearchFilters() {
        return this.getClass();
    }

    public Object getModelForSearchFilters() throws InstantiationException, IllegalAccessException {
        return this;
    }

    public Component getFiltersViewComponent() {
        return filtersComponent.getFiltersViewComponent();
    }


    public AbstractAction getAction(String step) {
        return null;
    }

    public Component getActionViewComponent(AbstractAction action) {
        return actionComponents.get(action);
    }

    public Set getSelection() {
        return resultsComponent.getSelection();
    }
}
