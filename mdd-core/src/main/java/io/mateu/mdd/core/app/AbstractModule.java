package io.mateu.mdd.core.app;

import io.mateu.mdd.shared.interfaces.IArea;
import io.mateu.mdd.shared.interfaces.IModule;
import io.mateu.mdd.shared.interfaces.MenuEntry;

import java.util.List;

/**
 * Created by miguel on 8/8/16.
 */
public abstract class AbstractModule implements IModule {

    private List<MenuEntry> menu;

    public abstract String getName();

    public List<MenuEntry> getMenu() {
        if (menu == null) synchronized (this) {
            menu = buildMenu();
        }
        return menu;
    }

    public abstract List<MenuEntry> buildMenu();

    public abstract IArea getArea();

}
