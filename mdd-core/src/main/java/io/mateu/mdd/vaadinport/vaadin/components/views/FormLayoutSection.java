package io.mateu.mdd.vaadinport.vaadin.components.views;

import io.mateu.reflection.FieldInterfaced;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter@Setter
public class FormLayoutSection {

    private final boolean card;
    private String caption;

    private List<FieldInterfaced> kpis = new ArrayList<>();
    private List<FieldInterfaced> fields = new ArrayList<>();

    public FormLayoutSection(String caption, boolean card) {
        this.caption = caption; 
        this.card = card;
    }
}
