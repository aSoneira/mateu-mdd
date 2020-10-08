package io.mateu.mdd.vaadin.controllers.secondLevel;

import com.vaadin.data.Binder;
import com.vaadin.ui.Component;
import io.mateu.mdd.core.app.MDDOpenEditorAction;
import io.mateu.mdd.core.interfaces.EntityProvider;
import io.mateu.mdd.core.ui.MDDUIAccessor;
import io.mateu.mdd.shared.annotations.UseLinkToListView;
import io.mateu.mdd.shared.reflection.FieldInterfaced;
import io.mateu.mdd.vaadin.MDDUI;
import io.mateu.mdd.vaadin.components.app.views.secondLevel.FieldEditorComponent;
import io.mateu.mdd.vaadin.components.views.*;
import io.mateu.mdd.vaadin.controllers.Controller;
import io.mateu.mdd.vaadin.controllers.thirdLevel.FieldController;
import io.mateu.mdd.vaadin.controllers.thirdLevel.MethodController;
import io.mateu.mdd.vaadin.data.MDDBinder;
import io.mateu.mdd.vaadin.navigation.MDDViewComponentCreator;
import io.mateu.mdd.vaadin.navigation.ViewStack;
import io.mateu.reflection.ReflectionHelper;
import io.mateu.util.notification.Notifier;
import io.mateu.util.persistence.JPAHelper;

import javax.persistence.*;
import java.lang.reflect.Method;
import java.util.Optional;

public class EditorController extends Controller {

    private final EditorViewComponent editorViewComponent;

    public EditorController(ViewStack stack, String path, EditorViewComponent editorViewComponent) {
        this.editorViewComponent = editorViewComponent;
        register(stack, path, editorViewComponent);
    }

    public EditorController(ViewStack stack, String path, Object bean) {
        editorViewComponent = (EditorViewComponent) MDDViewComponentCreator.createComponent(bean);
        register(stack, path, editorViewComponent);
    }

    public EditorController(ViewStack stack, String path, MDDOpenEditorAction action) throws Exception {
        editorViewComponent = (EditorViewComponent) MDDViewComponentCreator.createComponent(action);
        register(stack, path, editorViewComponent);
    }

    public EditorController(ViewStack stack, String path, ListViewComponent listViewComponent, Object bean) {
        editorViewComponent = (EditorViewComponent) MDDViewComponentCreator.createComponent(bean);
        editorViewComponent.setListViewComponent(listViewComponent);
        register(stack, path, editorViewComponent);
    }

    @Override
    public void apply(ViewStack stack, String path, String step, String cleanStep, String remaining) throws Throwable {


        if (!"".equals(step)) {

            Object r = null;
            Method method = null;
            FieldInterfaced field = null;

            if (editorViewComponent != null) {
                r = editorViewComponent.getModel();
                method = editorViewComponent.getMethod(step);
                field = editorViewComponent.getField(step);
            }
            if (r != null) {
                method = ReflectionHelper.getMethod(r.getClass(), step);
                field = ReflectionHelper.getFieldByName(r.getClass(), step.endsWith("_search") ? step.replaceAll("_search", "") : step);
            }

            if (method != null) {

                //callMethod(state, method, r, (Component) editorViewComponent);
                new MethodController(stack, path + "/" + step, method).next(stack, path, step, remaining);
            } else if (field != null) {

                if (step.endsWith("_search")) new FieldController(stack, path + "/" + step, field, editorViewComponent).next(stack, path, step, remaining);
                else new FieldController(stack, path + "/" + step, field, editorViewComponent).next(stack, path, step, remaining);

            }

        }

    }


}
