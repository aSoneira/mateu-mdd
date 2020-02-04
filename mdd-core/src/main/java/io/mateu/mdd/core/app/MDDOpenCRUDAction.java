package io.mateu.mdd.core.app;


import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.vaadinport.vaadin.components.oldviews.ExtraFilters;

public class MDDOpenCRUDAction extends AbstractAction {

    private final Class entityClass;
    private final String queryFilters;
    private final ExtraFilters extraFilters;

    public MDDOpenCRUDAction(Class entityClass) {
        this(Helper.capitalize(entityClass.getSimpleName()), entityClass, null, null);
    }

    public MDDOpenCRUDAction(Class entityClass, String queryFilters) {
        this(Helper.capitalize(entityClass.getSimpleName()), entityClass, queryFilters, null);
    }

    public MDDOpenCRUDAction(Class entityClass, ExtraFilters extraFilters) {
        this(Helper.capitalize(entityClass.getSimpleName()), entityClass, null, extraFilters);
    }


    public MDDOpenCRUDAction(String name, Class entityClass) {
        this(name, entityClass, null, null);
    }

    public MDDOpenCRUDAction(String name, Class entityClass, String queryFilters) {
        this(name, entityClass, queryFilters, null);
    }

    public MDDOpenCRUDAction(String name, Class entityClass, ExtraFilters extraFilters) {
        this(name, entityClass, null, extraFilters);
    }

    public MDDOpenCRUDAction(String name, Class entityClass, String queryFilters, ExtraFilters extraFilters) {
        super(name);
        this.entityClass = entityClass;
        this.queryFilters = queryFilters;
        this.extraFilters = extraFilters;
    }

    public String getQueryFilters() {
        return queryFilters;
    }

    @Override
    public void run(MDDExecutionContext context) throws Exception {
        context.openCRUD(this, entityClass, queryFilters, extraFilters, isModifierPressed());
    }
}
