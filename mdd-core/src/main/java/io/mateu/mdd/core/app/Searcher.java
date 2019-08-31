package io.mateu.mdd.core.app;

import com.google.common.collect.Lists;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.UseCheckboxes;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.mdd.vaadinport.vaadin.components.oldviews.Found;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter@Setter@Slf4j
public class Searcher {

    private String menu;

    @Override
    public String toString() {
        return "Menu";
    }

    public List<Found> findByMenu(String text) {
        long t0 = System.currentTimeMillis();

        List<Found> found = new ArrayList<>();

        boolean autentico = MDD.getUserData() != null;

        List<AbstractArea> areas = new ArrayList<>();

        for (AbstractArea a : MDD.getApp().getAreas()) {
            if (autentico) {
                if (!a.isPublicAccess()) {
                    areas.add(a);
                }
            } else {
                if (!MDD.getApp().isAuthenticationNeeded() || a.isPublicAccess()) {
                    areas.add(a);
                }
            }
        }
        for (AbstractArea a : areas) {

            for (AbstractModule m : a.getModules()) {
                for (MenuEntry e : m.getMenu()) {
                    addMenuEntry(found, a, e, text);
                }
            }

        }

        log.debug("Search of " + text + " took " + (System.currentTimeMillis() - t0) + "ms.");

        return found;

    }

    private void addMenuEntry(List<Found> found, AbstractArea a, MenuEntry e, String text) {

        if (e instanceof AbstractMenu) {

            for (MenuEntry ez : ((AbstractMenu) e).getEntries()) {
                addMenuEntry(found, a, ez, text); //("".equals(text) || e.getName().toLowerCase().contains(text))?"":text);
            }

        } else if (e instanceof AbstractAction) {

            if ("".equals(text) || e.getName().toLowerCase().contains(text)) {

                found.add(new Found(MDDUI.get().getNavegador().getPath(e), e.getName(), "" + a.getName() + " -> " + e.getName()));

            }

        }

    }
}
