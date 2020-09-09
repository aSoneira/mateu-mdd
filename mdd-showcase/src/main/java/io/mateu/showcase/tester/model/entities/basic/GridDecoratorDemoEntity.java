package io.mateu.showcase.tester.model.entities.basic;

import com.vaadin.ui.Grid;
import com.vaadin.ui.StyleGenerator;
import io.mateu.mdd.core.annotations.SearchFilter;
import io.mateu.mdd.core.interfaces.GridDecorator;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import lombok.MateuMDDEntity;
import javax.persistence.Id;

@MateuMDDEntity
public class GridDecoratorDemoEntity implements GridDecorator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @SearchFilter
    private String stringField;

    private int intField;


    @Override
    public void decorateGrid(Grid grid) {

        System.out.println(getIntField());

        grid.getColumn("stringField").setStyleGenerator(new StyleGenerator() {
            @Override
            public String apply(Object o) {
                return (((Integer)((Object[])o)[3]) > 20)?"green":"";
            }
        });
    }

    public static void main(String[] args) {
        System.out.println(new GridDecoratorDemoEntity().getIntField());
    }
}
