package io.mateu.mdd.vaadin.controllers.secondLevel;

import io.mateu.mdd.core.app.MDDOpenListViewAction;
import io.mateu.mdd.core.interfaces.ListView;
import io.mateu.mdd.vaadin.components.views.ListViewComponent;
import io.mateu.mdd.vaadin.controllers.secondLevel.ListViewComponentController;
import io.mateu.mdd.vaadin.navigation.MDDViewComponentCreator;
import io.mateu.mdd.vaadin.navigation.ViewStack;

public class ListViewController extends ListViewComponentController {

    public ListViewController(ViewStack stack, String path, ListViewComponent listViewComponent) {
        super(stack, path, listViewComponent);
    }

    public ListViewController(ViewStack stack, String path, MDDOpenListViewAction action) throws Exception {
        super(stack, path, (ListViewComponent) MDDViewComponentCreator.createComponent(action.listViewClass));
    }

}
