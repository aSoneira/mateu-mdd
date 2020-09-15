package io.mateu.mdd.core;

import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.mateu.mdd.core.annotations.Forbidden;
import io.mateu.mdd.core.annotations.Private;
import io.mateu.mdd.core.annotations.ReadOnly;
import io.mateu.mdd.core.annotations.ReadWrite;
import io.mateu.mdd.core.app.*;
import io.mateu.mdd.core.interfaces.*;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.reflection.ReflectionHelper;
import io.mateu.mdd.core.util.Notifier;
import io.mateu.mdd.util.Helper;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.mdd.vaadinport.vaadin.components.views.*;
import io.mateu.mdd.vaadinport.vaadin.mdd.VaadinPort;
import io.mateu.mdd.vaadinport.vaadin.navigation.ComponentWrapper;
import javassist.ClassPool;

import javax.persistence.Entity;
import javax.persistence.Query;
import java.net.URL;
import java.util.Collection;

public class MDD {

    private static ClassPool classPool;
    private static AbstractApplication app;


    public static VaadinPort getPort() {
        return (MDDUI.get() != null)?MDDUI.get().getPort():null;
    }

    public static AbstractApplication getApp() {
        try {
            app = MDDUI.get() != null?MDDUI.get().getApp():MDDUI.createApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return app;
    }




    public static void setCurrentUserLogin(String userData) {
        System.out.println("setUserData(" + userData + ")");
        getPort().setCurrentUserLogin(userData);
    }
    public static String getCurrentUserLogin() {
        return (getPort() != null)?getPort().getCurrentUserLogin():null;
    }

    public static UserPrincipal getCurrentUser() {
        String login = (MDD.getPort() != null && MDD.getCurrentUserLogin() != null)?MDD.getCurrentUserLogin():null;
        try {
            return Helper.getImpl(GeneralRepository.class).findUser(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static void openPrivateAreaSelector() {
        getPort().openPrivateAreaSelector();
    }
    public static void open(AbstractArea a) {
        getPort().open(a);
    }
    public static void open(AbstractModule m) {
        getPort().open(m);
    }
    public static void open(MenuEntry m) {
        getPort().open(m);
    }
    public static void openView(MDDOpenListViewAction mddOpenListViewAction, Class listViewClass) {
        getPort().openView(mddOpenListViewAction, listViewClass);
    }
    public static void openCRUD(AbstractAction action) {
        getPort().openCRUD(action);
    }
    public static void openEditor(AbstractAction action, Class viewClass, Object object) {
        getPort().openEditor(action, viewClass, object);
    }

    public static void edit(Object o) {
        MDDUI.get().getNavegador().edit(o);
    }


    public static void notifyError(String msg) {
        getPort().notifyError(msg);
    }
    public static void notifyInfo(String msg) {
        getPort().notifyInfo(msg);
    }
    public static void notify(Throwable throwable) {
        getPort().notifyError(throwable);
    }


    public static void alert(String msg) {
        Notifier.alert(msg);
    }
    public static void alert(Throwable throwable) {
        Notifier.alert(throwable);
    }

    public static void confirm(String msg, Runnable onOk) {
        getPort().confirm(msg, onOk);
    }

    public static void saveOrDiscard(String msg, EditorViewComponent editor, Runnable afterSave) {
        getPort().saveOrDiscard(msg, editor, afterSave);
    }

    public static void info(String msg) {
        Notifier.info(msg);
    }

    public static boolean isMobile() { return getPort().isMobile(); }

    public static void openWizard(Class firstPageClass) {
        getPort().openWizard(firstPageClass);
    }

    public static void updateTitle(String title) {
        getPort().updateTitle(title);
    }

    public static boolean isViewingOfficeCurrency() {
        return getPort().isViewingOfficeCurrency();
    }

    public static boolean isViewingCentralCurrency() {
        return getPort().isViewingCentralCurrency();
    }


    public static void setClassPool(ClassPool classPool) {
        MDD.classPool = classPool;
    }

    public static ClassPool getClassPool() {
        return classPool;
    }

    public static void refreshUI() {
        MDDUI.get().refreshUI();
    }

    public static boolean isIpad() {
        return getPort().isIpad();
    }

    public static void alert(BinderValidationStatus s) {
        StringBuffer msg = new StringBuffer();
        s.getFieldValidationErrors().forEach(e -> {
            if (!"".equals(msg.toString())) msg.append("\n");
            BindingValidationStatus x = (BindingValidationStatus) e;
            if (x.getField() instanceof AbstractComponent && ((AbstractComponent)x.getField()).getCaption() != null) {
                msg.append("" + ((AbstractComponent)x.getField()).getCaption() + " ");
            }
            x.getMessage().ifPresent(m -> msg.append(m));
        });
        Notification.show(msg.toString(), Notification.Type.TRAY_NOTIFICATION);
    }

    public static void openInWindow(String title, Object o) {
        if (o == null) {
            MDD.alert("Nothing to show");
        } else {
            try {

                AbstractViewComponent v = null;
                Class c = o.getClass();
                if (o instanceof Class && ((Class)o).isAnnotationPresent(Entity.class)) v = new JPAListViewComponent((Class) o);
                else if (o instanceof Component) v = new ComponentWrapper(title, (Component) o);
                else if (int.class.equals(c)
                        || Integer.class.equals(c)
                        || long.class.equals(c)
                        || Long.class.equals(c)
                        || double.class.equals(c)
                        || Double.class.equals(c)
                        || String.class.equals(c)
                        || boolean.class.equals(c)
                        || Boolean.class.equals(c)
                        || float.class.equals(c)
                        || Float.class.equals(c)
                ) v = new ComponentWrapper(title, new Label("" + o, ContentMode.HTML));
                else if (URL.class.equals(c)) {
                    if (o.toString().endsWith("pdf")) {
                        BrowserFrame b = new BrowserFrame("Result", new ExternalResource(o.toString()));
                        b.setSizeFull();
                        v = new ComponentWrapper(title, b);
                    } else {
                        v = new ComponentWrapper(title, new Link("Click me to view the result", new ExternalResource(o.toString())));
                    }
                } else if (o instanceof Collection && ((Collection) o).size() > 0 && ((Collection) o).iterator().next() != null && ((Collection) o).iterator().next().getClass().isAnnotationPresent(Entity.class)) {
                    v = new CollectionListViewComponent((Collection) o, ((Collection) o).iterator().next().getClass());
                } else if (Collection.class.isAssignableFrom(c)) {

                    Collection col = (Collection) o;

                    if (col.size() == 0) {
                        v = new ComponentWrapper(title, new Label("Empty list", ContentMode.HTML));
                    } else {

                        if (MDD.isMobile()) {

                            VerticalLayout vl = new VerticalLayout();
                            boolean primero = true;
                            for (Object i : col) {

                                if (primero) primero = false;
                                else vl.addComponent(new Label("--------------"));

                                if (ReflectionHelper.isBasico(i)) {
                                    vl.addComponent(new Label("" + i));
                                } else {
                                    for (FieldInterfaced f : ReflectionHelper.getAllFields(i.getClass())) {
                                        Label l;
                                        vl.addComponent(l = new Label("" + ReflectionHelper.getCaption(f)));
                                        l.addStyleName(ValoTheme.LABEL_BOLD);
                                        l.addStyleName(CSS.NOPADDING);
                                        vl.addComponent(l = new Label("" + ReflectionHelper.getValue(f, i)));
                                        l.addStyleName(CSS.NOPADDING);
                                    }
                                }

                            }

                            v = new ComponentWrapper(title, vl);

                        } else {

                            Object primerElemento = col.iterator().next();

                            Grid g = new Grid();

                            ListViewComponent.buildColumns(g, ListViewComponent.getColumnFields(primerElemento.getClass()), false, false);

                            //g.setSelectionMode(Grid.SelectionMode.MULTI);

                            // añadimos columna para que no haga feo
                            if (g.getColumns().size() == 1) ((Grid.Column)g.getColumns().get(0)).setExpandRatio(1);
                            else g.addColumn((d) -> null).setWidthUndefined().setCaption("");

                            g.setWidth("100%");
                            g.setHeightByRows(col.size());

                            g.setDataProvider(new ListDataProvider((Collection) o));

                            v = new ComponentWrapper(title, g);
                        }

                    }


                } else if (o instanceof Query) {

                    try {
                        v = new ComponentWrapper(title, new PdfComponent((Query) o));
                    } catch (Throwable throwable) {
                        MDD.alert(throwable);
                    }

                } else if (o instanceof RpcView) {
                    v = new RpcListViewComponent((RpcView) o);
                } else if (o.getClass().isAnnotationPresent(Entity.class) || PersistentPOJO.class.isAssignableFrom(o.getClass())) {
                    v = new EditorViewComponent(o) {
                        @Override
                        public void goBack() {
                            // no vuelve atrás
                        }
                    };
                } else if (o instanceof Component) {
                    v = new ComponentWrapper(title, (Component) o);
                } else if (o instanceof AbstractAction) {
                    ((AbstractAction) o).run();
                } else if (o instanceof WizardPage) {
                    v = new WizardComponent((WizardPage) o);
                } else {
                    v = new EditorViewComponent(o) {
                        @Override
                        public void goBack() {
                            // no vuelve atrás
                        }
                    };
                }
                if (v != null) {
                    v.setBackable(false);
                    MDDUI.get().openInWindow(v);
                }
            } catch (Throwable throwable) {
                MDD.alert(throwable);
            }
        }
    }

    public static boolean check(Private pa) {
        return check(pa.permissions(), pa.users(), pa.userTypes());
    }

    private static boolean check(int[] permissions, String[] users, Class[] userTypes) {
        boolean add = false;
        UserPrincipal u = MDD.getCurrentUser();
        if (u == null) return false;

        boolean usuarioOk = false;
        if (u != null && users != null && users.length > 0) {
            for (int i = 0; i < users.length; i++) {
                if (u.getLogin().equalsIgnoreCase(users[i])) {
                    usuarioOk = true;
                    break;
                }
            }
        } else usuarioOk = true;

        if (!usuarioOk) return false;

        boolean permisoOk = false;
        if (u != null && (users == null || users.length == 0) && permissions != null && permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                for (Long p : u.getPermissionIds()) {
                    if (p == permissions[i]) {
                        permisoOk = true;
                        break;
                    }
                    if (permisoOk) break;
                }
            }
        } else permisoOk = true;


        boolean tipoOk = false;
        if (u != null && userTypes != null && userTypes.length > 0) {
            for (int i = 0; i < userTypes.length; i++) {
                if (userTypes[i].isAssignableFrom(u.getClass())) {
                    tipoOk = true;
                    break;
                }
            }
        } else tipoOk = true;

        if (permisoOk || usuarioOk || tipoOk) add = true;
        return add;
    }

    public static boolean check(ReadOnly a) {
        return check(a.permissions(), a.users(), a.userTypes());
    }

    public static boolean check(ReadWrite a) {
        return check(a.permissions(), a.users(), a.userTypes());
    }

    public static boolean check(Forbidden a) {
        return check(a.permissions(), a.users(), a.userTypes());
    }

    public static boolean isReadOnly(FieldInterfaced f) {
        if (f.isAnnotationPresent(ReadOnly.class)) {
            ReadOnly a = f.getAnnotation(ReadOnly.class);
            return check(a.permissions(), a.users(), a.userTypes());
        } else return false;
    }

    public static boolean isReadWrite(Class<?> type) {
        boolean r = true;
        if (type.isAnnotationPresent(Forbidden.class)) {
            Forbidden a = type.getAnnotation(Forbidden.class);
            r &= !check(a.permissions(), a.users(), a.userTypes());
        }
        if (type.isAnnotationPresent(ReadOnly.class)) {
            ReadOnly a = type.getAnnotation(ReadOnly.class);
            r &= !check(a.permissions(), a.users(), a.userTypes());
        }
        return r;
    }
}
