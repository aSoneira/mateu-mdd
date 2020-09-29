package io.mateu.mdd.core;

import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;
import io.mateu.mdd.core.app.AbstractApplication;
import io.mateu.mdd.shared.annotations.Forbidden;
import io.mateu.mdd.shared.annotations.ReadOnly;
import io.mateu.mdd.shared.annotations.ReadWrite;
import io.mateu.mdd.shared.interfaces.UserPrincipal;
import io.mateu.mdd.shared.reflection.FieldInterfaced;
import io.mateu.security.Private;
import javassist.ClassPool;

public class MDD {

    private static ClassPool classPool;
    private static AbstractApplication app;

/*
    public static AbstractApplication getApp() {
        try {
            app = MDDUI.get() != null? MDDUIAccessor.getApp(): MDDUI.createApp();
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

*/

    public static void setClassPool(ClassPool classPool) {
        MDD.classPool = classPool;
    }

    public static ClassPool getClassPool() {
        return classPool;
    }
/*
    public static void refreshUI() {
        MDDUI.get().refreshUI();
    }

    public static boolean isIpad() {
        return getPort().isIpad();
    }

 */

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

    /*
    public static void openInWindow(String title, Object o) {
        if (o == null) {
            Notifier.alert("Nothing to show");
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

                        if (MDDUIAccessor.isMobile()) {

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
                        Notifier.alert(throwable);
                    }

                } else if (o instanceof RpcView) {
                    v = new RpcListViewComponent((RpcView) o);
                } else if (o.getClass().isAnnotationPresent(Entity.class) || PersistentPojo.class.isAssignableFrom(o.getClass())) {
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
                Notifier.alert(throwable);
            }
        }
    }
*/

    public static boolean check(Private pa) {
        return check(pa.roles(), pa.users());
    }

    private static boolean check(String[] permissions, String[] users) {
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
                    if (permissions[i].equals("" + p)) {
                        permisoOk = true;
                        break;
                    }
                    if (permisoOk) break;
                }
            }
        } else permisoOk = true;


        if (permisoOk || usuarioOk) add = true;
        return add;
    }

    private static UserPrincipal getCurrentUser() {
        return null;
    }

    public static boolean check(ReadOnly a) {
        return check(a.roles(), a.users());
    }

    public static boolean check(ReadWrite a) {
        return check(a.roles(), a.users());
    }

    public static boolean check(Forbidden a) {
        return check(a.roles(), a.users());
    }

    public static boolean isReadOnly(FieldInterfaced f) {
        if (f.isAnnotationPresent(ReadOnly.class)) {
            ReadOnly a = f.getAnnotation(ReadOnly.class);
            return check(a.roles(), a.users());
        } else return false;
    }

    public static boolean isReadWrite(Class<?> type) {
        boolean r = true;
        if (type.isAnnotationPresent(Forbidden.class)) {
            Forbidden a = type.getAnnotation(Forbidden.class);
            r &= !check(a.roles(), a.users());
        }
        if (type.isAnnotationPresent(ReadOnly.class)) {
            ReadOnly a = type.getAnnotation(ReadOnly.class);
            r &= !check(a.roles(), a.users());
        }
        return r;
    }
}
