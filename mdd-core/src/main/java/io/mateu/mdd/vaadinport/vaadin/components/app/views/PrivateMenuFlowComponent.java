package io.mateu.mdd.vaadinport.vaadin.components.app.views;

import com.google.common.base.Strings;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import io.mateu.mdd.core.CSS;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.app.AbstractArea;
import io.mateu.mdd.core.app.AbstractModule;
import io.mateu.mdd.shared.AppConfigLocator;
import io.mateu.mdd.shared.IAppConfig;
import io.mateu.mdd.util.Helper;
import io.mateu.mdd.util.JPAHelper;
import io.mateu.mdd.vaadinport.vaadin.MDDUI;
import io.mateu.mdd.vaadinport.vaadin.components.views.AbstractViewComponent;

public class PrivateMenuFlowComponent extends AbstractViewComponent {

    @Override
    public boolean isBarHidden() {
        return true;
    }

    @Override
    public String toString() {
        return MDD.isMobile()?MDD.getApp().getName():(MDD.getApp().getAreas().size() > 1?"Please select work area":"");
    }

    @Override
    public String getPageTitle() {
        return MDD.getApp().getName();
    }

    public PrivateMenuFlowComponent() {
        addStyleName("privatemenuflowcomponent");

        addStyleName(CSS.NOPADDING);
        if (MDD.isMobile()) {
            addStyleName("mobile");

            Label l;
            addComponent(l = new Label(MDD.getApp().getName()));
            l.addStyleName(ValoTheme.LABEL_H1);

            try {
                IAppConfig c = Helper.getImpl(AppConfigLocator.class).get();
                if (!Strings.isNullOrEmpty(c.getLogoUrl())) {
                    addComponent(new Image("", new ExternalResource(c.getLogoUrl())));
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        if (MDD.getApp().getAreas().size() == 1) {

            AbstractArea area = MDD.getApp().getAreas().get(0);

            if (area.getModules().size() == 1) {

                AbstractModule m = area.getModules().get(0);

                m.getMenu().stream().forEach(a -> {

                    Button b;
                    addComponent(b = new Button(a.getCaption()));
                    b.addClickListener(e -> MDDUI.get().getNavegador().goTo(a));
                    b.setPrimaryStyleName(ValoTheme.BUTTON_LINK);
                    b.addStyleName("submenuoption");


                });


            } else {

                area.getModules().stream().forEach(a -> {

                    Button b;
                    addComponent(b = new Button(a.getName()));
                    b.addClickListener(e -> MDDUI.get().getNavegador().goTo(a));
                    b.setPrimaryStyleName(ValoTheme.BUTTON_LINK);
                    b.addStyleName("submenuoption");

                });

            }
        }

        } else if (MDD.getApp().getAreas().size() > 1) {

            CssLayout lx;
            addComponent(lx = new CssLayout());

            MDDUI.get().getAppComponent().setSelectingArea(true);

            MDD.getApp().getAreas().stream().forEach(a -> {

                Button b;
                lx.addComponent(b = new Button(a.getName(), VaadinIcons.ADOBE_FLASH.equals(a.getIcon())?null:a.getIcon()));
                b.addClickListener(e -> MDDUI.get().getNavegador().goTo(a));
                b.setPrimaryStyleName(ValoTheme.BUTTON_HUGE);
                b.addStyleName("submenuoption");


            });
        }

        if (MDD.isMobile()) {
            Button b;
            addComponent(b = new Button("Logout"));
            b.addClickListener(e -> MDDUI.get().getNavegador().goTo("bye"));
            b.setPrimaryStyleName(ValoTheme.BUTTON_LINK);
            b.addStyleName("submenuoption");
        }

        addComponentsAndExpand(new Label(""));

        if (!MDD.isMobile() && MDD.getApp().getAreas().size() > 1) MDDUI.get().getAppComponent().setSelectingArea(true);

    }


}
