package io.mateu.mdd.vaadinport.vaadin.components.app.views;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.app.AbstractArea;
import io.mateu.mdd.core.app.AbstractModule;
import io.mateu.mdd.vaadinport.vaadin.MyUI;

public class ModuleComponent extends VerticalLayout {

    private final AbstractModule area;

    @Override
    public String toString() {
        return "Welcome to " + area.getName() + " area.";
    }

    public ModuleComponent(AbstractModule area) {

        this.area = area;

        addStyleName("methodresultflowcomponent");

        area.getMenu().stream().forEach(a -> {

            Button b;
            addComponent(b = new Button(a.getName()));
            b.addClickListener(e -> MyUI.get().getNavegador().goTo(a));
            b.addStyleName(ValoTheme.BUTTON_QUIET);

        });


        if (!MDD.isMobile()) addComponentsAndExpand(new Label(""));


    }

}
