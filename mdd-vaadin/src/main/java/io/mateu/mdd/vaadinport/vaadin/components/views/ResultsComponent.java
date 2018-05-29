package io.mateu.mdd.vaadinport.vaadin.components.views;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.GridSortOrder;
import com.vaadin.event.SortEvent;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.ItemClickListener;
import io.mateu.mdd.core.MDD;
import org.vaadin.patrik.FastNavigation;

public class ResultsComponent extends VerticalLayout {

    private final ListViewComponent listViewComponent;
    private Grid grid;

    public ResultsComponent(ListViewComponent listViewComponent) {

        this.listViewComponent = listViewComponent;

        addStyleName("resultscomponent");

        build();

    }

    private void build() {

        grid = new Grid<>();

        listViewComponent.buildColumns(grid);

        // añadimos columna para que no haga feo
        grid.addColumn((d) -> null).setWidthUndefined().setCaption("");

        grid.addSortListener(new SortEvent.SortListener<GridSortOrder<Object>>() {
            @Override
            public void sort(SortEvent<GridSortOrder<Object>> sortEvent) {
                System.out.println("sort listener received " + sortEvent);
            }
        });


        grid.setSizeFull();


        DataProvider p = DataProvider.fromFilteringCallbacks(query -> listViewComponent.findAll(query.getSortOrders(), query.getOffset(), query.getLimit()).stream(), query -> listViewComponent.count());

        grid.setDataProvider(p);
        grid.setColumnReorderingAllowed(true);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        /*

        FastNavigation nav = new FastNavigation(grid, false, true);
        nav.setChangeColumnAfterLastRow(true);
        nav.setOpenEditorWithSingleClick(false);
        nav.setRowValidation(true);


        nav.addRowFocusListener(event -> {
            if (event.getRow() >= 0) {
                grid.select(event.getItem());
            }
            else grid.deselectAll();
            System.out.println("Focus moved to row " + event.getRow());
        });

*/



        /*
        nav.addEditorOpenListener(new FastNavigation.EditorOpenListener() {
            @Override
            public void onEvent(EditorOpenEvent<?> editorOpenEvent) {
                Object i = editorOpenEvent.getItem();
                if (i != null && i instanceof Object[]) edit(((Object[])i)[0]);
            }
        });
        */


        grid.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChange(SelectionEvent selectionEvent) {
                System.out.println("selected " + selectionEvent.getFirstSelectedItem());
            }
        });

        grid.addItemClickListener(new ItemClickListener<Object>() {
            @Override
            public void itemClick(Grid.ItemClick<Object> itemClick) {
                if (itemClick.getMouseEventDetails().isDoubleClick()) {
                    Object i = itemClick.getItem();
                    if (i != null && i instanceof Object[]) edit(((Object[])i)[0]);
                }
            }
        });



        addComponentsAndExpand(grid);

    }

    private void edit(Object id) {
        try {
            listViewComponent.edit(id);
        } catch (Throwable throwable) {
            MDD.alert(throwable);
        }
    }

    public void search() throws Throwable {
        grid.getDataProvider().refreshAll();
    }
}
