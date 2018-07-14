package io.mateu.mdd.vaadinport.vaadin.components.app.flow.views;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.app.AbstractMenu;
import io.mateu.mdd.core.app.AbstractModule;
import io.mateu.mdd.vaadinport.vaadin.components.app.flow.FlowViewComponent;

public class ModuleFlowComponent extends AbstractFlowComponent {

    private final AbstractModule module;
    private final String state;

    @Override
    public String getViewTile() {
        return module.getName();
    }

    @Override
    public String getStatePath() {
        return state;
    }


    public ModuleFlowComponent(String state, AbstractModule module) {
        this.state = state;
        this.module = module;

        addStyleName("moduleflowcomponent");

        module.getMenu().forEach(e -> addComponent(new Button(e.getName(), x ->{
            if (e instanceof AbstractMenu) MDD.open(e);
            else if (e instanceof AbstractAction) {
                String path = MDD.getApp().getMenuId(e);
                Page.getCurrent().open(((path.startsWith("/"))?"":"/") + path, Page.getCurrent().getWindowName());
            };
        })));


    }

}
