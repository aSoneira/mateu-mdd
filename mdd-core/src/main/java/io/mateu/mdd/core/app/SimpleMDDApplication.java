package io.mateu.mdd.core.app;

import com.google.common.base.Strings;
import com.vaadin.icons.VaadinIcons;
import io.mateu.mdd.core.annotations.*;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.Caption;
import io.mateu.mdd.core.model.authentication.Permission;
import io.mateu.mdd.core.model.authentication.User;
import io.mateu.mdd.core.reflection.ReflectionHelper;
import io.mateu.mdd.core.util.Helper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleMDDApplication extends BaseMDDApp {

    @Override
    public String getName() {
        if (getClass().isAnnotationPresent(Caption.class)) return getClass().getAnnotation(Caption.class).value();
        return Helper.capitalize(getClass().getSimpleName());
    }

    public AbstractAction getDefaultAction() {
        return null;
    }

    @Override
    public List<AbstractArea> buildAreas() {
        List<AbstractArea> l = new ArrayList<>();
        addPrivateAreas(l);
        addPublicAreas(l);
        return l;
    }

    private void addPublicAreas(List<AbstractArea> l) {
        AbstractArea a = new AbstractArea("") {
            @Override
            public List<AbstractModule> buildModules() {
                List<AbstractModule> m = Arrays.asList(new AbstractModule() {
                    @Override
                    public String getName() {
                        return "Menu";
                    }

                    @Override
                    public List<MenuEntry> buildMenu() {
                        return SimpleMDDApplication.this.buildMenu(true);
                    }
                });
                return m;
            }

            @Override
            public boolean isPublicAccess() {
                return true;
            }

            @Override
            public AbstractAction getDefaultAction() {
                return SimpleMDDApplication.this.getDefaultAction();
            }
        };
        if (a.getModules().size() > 0 && a.getModules().get(0).getMenu().size() > 0) l.add(a);
    }

    private void addPrivateAreas(List<AbstractArea> l) {
        AbstractArea a = new AbstractArea("") {
            @Override
            public List<AbstractModule> buildModules() {
                List<AbstractModule> m = Arrays.asList(new AbstractModule() {
                    @Override
                    public String getName() {
                        return "Menu";
                    }

                    @Override
                    public List<MenuEntry> buildMenu() {
                        return SimpleMDDApplication.this.buildMenu(false);
                    }
                });
                return m;
            }

            @Override
            public boolean isPublicAccess() {
                return false;
            }

            @Override
            public AbstractAction getDefaultAction() {
                return SimpleMDDApplication.this.getDefaultAction();
            }
        };
        if (a.getModules().size() > 0 && a.getModules().get(0).getMenu().size() > 0) l.add(a);
    }

    List<MenuEntry> buildMenu(boolean publicAccess) {
        return buildMenu(this, publicAccess);
    }

    List<MenuEntry> buildMenu(Object app, boolean publicAccess) {
        List<MenuEntry> l = new ArrayList<>();

        for (Method m : getAllActionMethods(app.getClass())) {

            boolean add = false;
            if (publicAccess && !m.isAnnotationPresent(Private.class) && (!SimpleMDDApplication.this.isAuthenticationNeeded() || m.isAnnotationPresent(Public.class))) {
                add = true;
            }
            if (!publicAccess && !m.isAnnotationPresent(Public.class) && (SimpleMDDApplication.this.isAuthenticationNeeded() || m.isAnnotationPresent(Private.class))) {
                Private pa = m.getAnnotation(Private.class);
                if (pa != null) {
                    User u = MDD.getCurrentUser();
                    boolean permisoOk = false;
                    if (u != null && (pa.users() == null || pa.users().length == 0) && pa.permissions() != null && pa.permissions().length > 0) {
                        for (int i = 0; i < pa.permissions().length; i++) {
                            for (Permission p : u.getPermissions()) {
                                if (p.getId() == pa.permissions()[i]) {
                                    permisoOk = true;
                                    break;
                                }
                                if (permisoOk) break;
                            }
                        }
                    } else permisoOk = true;
                    boolean usuarioOk = false;
                    if (u != null && pa.users() != null && pa.users().length > 0) {
                        for (int i = 0; i < pa.users().length; i++) {
                            if (u.getLogin().equalsIgnoreCase(pa.users()[i])) {
                                usuarioOk = true;
                                break;
                            }
                        }
                    } else usuarioOk = true;
                    if (permisoOk && usuarioOk) add = true;
                } else add = true;
            }

            if (add) {
                String caption = (m.isAnnotationPresent(SubApp.class))?m.getAnnotation(SubApp.class).value():m.getAnnotation(Action.class).value();
                if (Strings.isNullOrEmpty(caption)) caption = Helper.capitalize(m.getName());

                VaadinIcons icon = (m.isAnnotationPresent(SubApp.class))?m.getAnnotation(SubApp.class).icon():m.getAnnotation(Action.class).icon();

                if (m.isAnnotationPresent(SubApp.class)) {

                    l.add(new AbstractMenu(icon, caption) {
                        @Override
                        public List<MenuEntry> buildEntries() {
                            try {
                                return buildMenu(ReflectionHelper.invokeInjectableParametersOnly(m, app), publicAccess);
                            } catch (Throwable throwable) {
                                MDD.alert(throwable);
                            }
                            return new ArrayList<>();
                        }
                    });

                } else {

                    if (List.class.isAssignableFrom(m.getReturnType()) && MenuEntry.class.equals(ReflectionHelper.getGenericClass(m))) {

                        l.add(new AbstractMenu(icon, caption) {
                            @Override
                            public List<MenuEntry> buildEntries() {
                                List<MenuEntry> l = new ArrayList<>();
                                try {

                                    l = (List<MenuEntry>) ReflectionHelper.invokeInjectableParametersOnly(m, SimpleMDDApplication.this);

                                } catch (Throwable e) {
                                    MDD.alert(e);
                                }
                                return l;
                            }
                        });


                    } else {

                        l.add(new AbstractAction(icon, caption) {
                            @Override
                            public void run(MDDExecutionContext context) {
                                try {

                                    context.callMethod(null, m, app, null);

                                } catch (Throwable e) {
                                    MDD.alert(e);
                                }
                            }
                        });

                    }


                }
            }

        }

        return l;
    }

    List<Method> getAllActionMethods(Class c) {
        List<Method> l = new ArrayList<>();

        if (c.getSuperclass() != null && !SimpleMDDApplication.class.equals(c.getSuperclass()))
            l.addAll(getAllActionMethods(c.getSuperclass()));

        for (Method f : c.getDeclaredMethods()) if (f.isAnnotationPresent(Action.class) || f.isAnnotationPresent(SubApp.class)) l.add(f);

        l.sort((a, b) -> {
            int orderA = 0;
            if (a.isAnnotationPresent(Action.class)) orderA = a.getAnnotation(Action.class).order();
            else if (a.isAnnotationPresent(SubApp.class)) orderA = a.getAnnotation(SubApp.class).order();
            int orderB = 0;
            if (b.isAnnotationPresent(Action.class)) orderB = b.getAnnotation(Action.class).order();
            else if (b.isAnnotationPresent(SubApp.class)) orderB = b.getAnnotation(SubApp.class).order();
            return orderA - orderB;
        });

        return l;
    }

    @Override
    public boolean isAuthenticationNeeded() {
        return false;
    }
}
