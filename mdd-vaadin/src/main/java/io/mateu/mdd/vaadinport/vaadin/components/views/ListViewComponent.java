package io.mateu.mdd.vaadinport.vaadin.components.views;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.List;

public abstract class ListViewComponent extends AbstractViewComponent<ListViewComponent> {

    private ResultsComponent resultsComponent;

    private List<ListViewComponentListener> listeners = new ArrayList<>();

    private int count;
    private Label countLabel;

    @Override
    public ListViewComponent build() {

        super.build();

        addStyleName("listviewcomponent");
        
        
        addComponent(new FiltersComponent(this));

        addComponent(countLabel = new Label());


        addComponentsAndExpand(resultsComponent = buildResultsComponent());

        return this;
    }


    private ResultsComponent buildResultsComponent() {
        return new ResultsComponent(this);
    }

    public abstract void buildColumns(Grid grid);

    public void search() throws Throwable {
        resultsComponent.search();
    }

    public void addListener(ListViewComponentListener listener) {
        listeners.add(listener);
    }


    public void edit(Object id) {
        for (ListViewComponentListener l : listeners) l.onEdit(id);
    };

    public abstract List findAll(List<QuerySortOrder> sortOrders, int offset, int limit);
    public int count() {
        count = gatherCount();
        countLabel.setValue("" + count + " matches.");
        return count;
    }

    protected abstract int gatherCount();

    public abstract Object deserializeId(String id);

    public abstract String getPathForEditor(Object id);
}
