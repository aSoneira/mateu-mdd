package io.mateu.mdd.vaadinport.vaadin.components.app.flow.views;

import com.vaadin.ui.*;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.data.UserData;
import io.mateu.mdd.vaadinport.vaadin.components.app.flow.FlowViewComponent;

public class PublicMenuFlowComponent extends VerticalLayout implements FlowViewComponent {

    private final String state;

    @Override
    public String getViewTile() {
        return MDD.getApp().getName();
    }

    @Override
    public String getStatePath() {
        return state;
    }


    public PublicMenuFlowComponent(String state) {
        this.state = state;

        MDD.getApp().getAreas().forEach(a -> addComponent(new Button(a.getName(), e -> MDD.open(a))));

    }

}
