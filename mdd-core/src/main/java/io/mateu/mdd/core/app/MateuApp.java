package io.mateu.mdd.core.app;

import io.mateu.i18n.Translator;
import io.mateu.mdd.core.annotations.MateuUI;
import io.mateu.mdd.shared.annotations.*;
import io.mateu.mdd.shared.reflection.FieldInterfaced;
import io.mateu.reflection.ReflectionHelper;
import io.mateu.security.Private;
import io.mateu.util.Helper;

import java.lang.reflect.Method;
import java.util.List;

public class MateuApp extends BaseMDDApp {

    private final Object ui;
    private List<AbstractArea> _areas;
    private boolean _authenticationNeeded;
    private Class uiclass;
    private boolean _hasRegistrationForm;

    public MateuApp() throws Exception {
        this(null);
    }

    public MateuApp(Class uiclass) throws Exception {
        this.uiclass = uiclass != null?uiclass:getClass();
        ui = uiclass != null?ReflectionHelper.newInstance(uiclass):this;
        init();
    }

    @Override
    public String getName() {
        if (_areas == null) init();
        if (uiclass.isAnnotationPresent(Caption.class)) return Translator.translate(((Caption)uiclass.getAnnotation(Caption.class)).value());
        return Translator.translate(Helper.capitalize(uiclass.getSimpleName()));
    }

    @Override
    public List<AbstractArea> buildAreas() {
        if (_areas == null) init();
        return _areas;
    }

    private void init() {

        if (uiclass.isAnnotationPresent(MateuUI.class)) setLogo(((MateuUI)uiclass.getAnnotation(MateuUI.class)).logo());

        _areas = new AreaBuilder(ui).buildAreas(uiclass);

        _authenticationNeeded = uiclass.isAnnotationPresent(Private.class);
        if (!_authenticationNeeded) {
            _areas.forEach(a -> _authenticationNeeded |= !a.isPublicAccess());
        }

        for (FieldInterfaced f : ReflectionHelper.getAllFields(uiclass)) {
            if (f.isAnnotationPresent(RegistrationForm.class)) {
                _hasRegistrationForm = true;
                break;
            }
        }
        if (!_hasRegistrationForm) for (Method f : ReflectionHelper.getAllMethods(uiclass)) {
            if (f.isAnnotationPresent(RegistrationForm.class)) {
                _hasRegistrationForm = true;
                break;
            }
        }
    }

    @Override
    public boolean isAuthenticationNeeded() {
        if (_areas == null) init();
        return _authenticationNeeded;
    }

    @Override
    public AbstractArea getDefaultPublicArea() {
        if (_areas == null) init();
        return _areas.stream().filter(a -> a.isPublicAccess()).findFirst().orElse(null);
    }

    @Override
    public boolean hasRegistrationForm() {
        return _hasRegistrationForm;
    }

    @Override
    public boolean isForm() {
        boolean hasMenus = false;
        boolean hasHomes = false;

        for (FieldInterfaced f : ReflectionHelper.getAllFields(uiclass)) {
            if (f.isAnnotationPresent(Home.class) || f.isAnnotationPresent(PrivateHome.class) || f.isAnnotationPresent(PublicHome.class)) {
                hasHomes = true;
                break;
            }
            if (f.isAnnotationPresent(Submenu.class) || f.isAnnotationPresent(MenuOption.class)) {
                hasMenus = true;
                break;
            }
        }

        if (!hasMenus && !hasHomes) for (Method f : ReflectionHelper.getAllMethods(uiclass)) {
            if (f.isAnnotationPresent(Home.class) || f.isAnnotationPresent(PrivateHome.class) || f.isAnnotationPresent(PublicHome.class)) {
                hasHomes = true;
                break;
            }
            if (f.isAnnotationPresent(Submenu.class) || f.isAnnotationPresent(MenuOption.class)) {
                hasMenus = true;
                break;
            }
        }

        return !hasMenus && !hasHomes;
    }

    public Object getBean() {
        return ui;
    }
}
