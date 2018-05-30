package io.mateu.mdd.core;

import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.app.MDDAction;
import io.mateu.mdd.core.app.MDDOpenEditorAction;
import io.mateu.mdd.core.data.UserData;
import io.mateu.mdd.core.views.RPCView;

public class MDD {

    private static MDDPort port;

    public static MDDPort getPort() {
        return port;
    }

    public static void setPort(MDDPort port) {
        MDD.port = port;
    }




    public static void alert(String msg) {
        getPort().alert(msg);
    }

    public static void openCRUD(AbstractAction action, Class entityClass, String queryFilters, boolean modifierPressed) {
        getPort().openCRUD(action, entityClass, queryFilters, modifierPressed);
    }

    public static void openEditor(AbstractAction action, Class viewClass, Object object, boolean modifierPressed) {
        getPort().openEditor(action, viewClass, object, modifierPressed);
    }

    public static UserData getUserData() {
        return getPort().getUserData();
    }

    public static void openView(RPCView v, boolean modifierPressed) {
        getPort().openView(v, modifierPressed);
    }


    public static void alert(Throwable throwable) {
        getPort().alert(throwable);
    }
}
