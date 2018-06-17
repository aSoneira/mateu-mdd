package io.mateu.mdd.vaadinport.vaadin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.vaadin.annotations.*;
import com.vaadin.navigator.*;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.app.*;
import io.mateu.mdd.core.interfaces.App;
import io.mateu.mdd.vaadinport.vaadin.components.app.AppComponent;
import io.mateu.mdd.vaadinport.vaadin.components.app.flow.FlowComponent;
import io.mateu.mdd.vaadinport.vaadin.components.app.flow.views.LoginFlowComponent;
import io.mateu.mdd.vaadinport.vaadin.components.views.EditorViewComponent;
import io.mateu.mdd.vaadinport.vaadin.components.views.JPAListViewComponent;
import io.mateu.mdd.vaadinport.vaadin.components.views.ViewComponent;
import io.mateu.mdd.vaadinport.vaadin.mdd.VaadinPort;
import io.mateu.mdd.vaadinport.vaadin.navigation.MDDNavigator;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")

//<link href="//cdn.muicss.com/mui-0.9.39/css/mui.min.css" rel="stylesheet" type="text/css" />
//<script src="//cdn.muicss.com/mui-0.9.39/js/mui.min.js"></script>

@StyleSheet("//cdn.muicss.com/mui-0.9.39/css/mui.min.css")
@JavaScript("//cdn.muicss.com/mui-0.9.39/js/mui.min.js")

//@StyleSheet("https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.1/css/bulma.min.css")
@Viewport("width=device-width, initial-scale=1")
@PushStateNavigation // para urls sin #!
@PreserveOnRefresh
public class MyUI extends UI {

    private MDDNavigator navegador;
    private Navigator navigator;
    private FlowComponent flowComponent;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        Iterator<App> apps = ServiceLoader.load(App.class).iterator();
        AbstractApplication app = null;
        while (apps.hasNext()) {
            app = (AbstractApplication) apps.next();
            System.out.println("app " + app.getName() + " loaded");
            app.buildAreaAndMenuIds();
            break;
        }

        MDD.setPort(new VaadinPort());

        MDD.setApp((BaseMDDApp) app);

        flowComponent = new FlowComponent();

        addNavigator();


        setContent(flowComponent);

    }


    @Override
    protected void refresh(VaadinRequest request) {
        super.refresh(request);
        String state = getPage().getLocation().getPath();
        if (state.startsWith("/")) state = state.substring(1);
        System.out.println("MyUI.refresh: new state = " + state);
        navigator.navigateTo(state);
    }



    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {

    }







    private void addNavigator() {


        navigator = new Navigator(this, new Panel());

        VoidView voidView = new VoidView();
        navigator.addProvider(new ViewProvider() {
            @Override
            public String getViewName(String s) {
                return s;
            }

            @Override
            public View getView(String s) {
                return voidView;
            }
        });

        navegador = new MDDNavigator(flowComponent, navigator);

        navigator.addViewChangeListener(navegador);
    }

    public String getPath(AbstractAction action, Class viewClass, Object id) {
        return navegador.getPath(action, viewClass, id);
    }

    public String getPath(AbstractAction action, Class viewClass) {
        return navegador.getPath(action, viewClass);
    }

    public void goTo(String path) {
        navegador.goTo(path);
    }

    public void go(String relativePath) { navegador.go(relativePath); }

    public void goTo(AbstractAction action, Class viewClass) {
        navegador.goTo(navegador.getPath(action, viewClass));
    }

    public void goTo(AbstractAction action, Class viewClass, Object id) {
        navegador.goTo(navegador.getPath(action, viewClass, id));
    }

    public void goTo(AbstractModule m) {
        navegador.goTo(navegador.getPath(m));
    }


    public void goTo(MenuEntry e) {
        navegador.goTo(navegador.getPath(e));
    }

    public void goTo(AbstractArea area) {
        navegador.goTo(navegador.getPath(area));
    }


}
