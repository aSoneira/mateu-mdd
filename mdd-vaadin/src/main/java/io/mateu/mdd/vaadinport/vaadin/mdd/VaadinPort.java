package io.mateu.mdd.vaadinport.vaadin.mdd;

import com.vaadin.ui.*;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.MDDPort;
import io.mateu.mdd.core.app.*;
import io.mateu.mdd.core.data.UserData;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.vaadinport.vaadin.MyUI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

public class VaadinPort implements MDDPort {

    @Override
    public void alert(String msg) {
        Notification.show("Alert",
                msg,
                Notification.Type.HUMANIZED_MESSAGE);
    }

    @Override
    public void openCRUD(AbstractAction action, Class viewClass, String queryFilters, boolean modifierPressed) {
        System.out.println("open crud");

        MyUI.get().getNavegador().goTo(action, viewClass);
    }

    @Override
    public void openEditor(AbstractAction action, Class viewClass, Object id, boolean modifierPressed) {
        System.out.println("open editor");

        MyUI.get().getNavegador().goTo(action, viewClass, id);
    }

    @Override
    public UserData getUserData() {
        return (UserData) UI.getCurrent().getSession().getAttribute("_userdata");
    }

    @Override
    public void openView(AbstractAction action, Class listViewClass, boolean modifierPressed) {
        MyUI.get().getNavegador().goTo(action, listViewClass);
    }

    @Override
    public void alert(Throwable throwable) {
        throwable.printStackTrace();

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        Notification.show("Error",
                sw.toString(),
                Notification.Type.ERROR_MESSAGE);
    }

    @Override
    public void setUserData(UserData userData) {
        MDD.getApp().setUserData(userData);
        UI.getCurrent().getSession().setAttribute("_userdata", userData);
        MyUI.get().getAppComponent().updateSession();
    }

    @Override
    public void openPrivateAreaSelector() {
        MyUI.get().getNavegador().goTo("private");
    }


    @Override
    public void open(AbstractArea a) {
        MyUI.get().getNavegador().goTo(a);
    }

    @Override
    public void open(MenuEntry m) {
        MyUI.get().getNavegador().goTo(m);
    }

    @Override
    public void open(AbstractModule m) {
        MyUI.get().getNavegador().goTo(m);
    }

    @Override
    public void open(Method m) {
        MyUI.get().getNavegador().goTo(m);
    }

    @Override
    public void open(FieldInterfaced f) {
        MyUI.get().getNavegador().goTo(f);
    }

    @Override
    public void confirm(String msg, Runnable onOk) {

        Window w = new Window("Please confirm action");

        VerticalLayout l = new VerticalLayout();

        l.addComponent(new Label(msg));

        Button b;
        l.addComponent(b = new Button("Yes, do it", e -> {
            try {
                onOk.run();
            } catch (Throwable t) {
                MDD.alert(t);
            }
            w.close();
        }));

        w.setContent(l);

        w.center();
        w.setModal(true);

        UI.getCurrent().addWindow(w);

    }

    @Override
    public AbstractApplication getApp() {
        return MyUI.get().getApp();
    }

    @Override
    public void setApp(AbstractApplication app) {
        MyUI.get().setApp(app);
    }
}
