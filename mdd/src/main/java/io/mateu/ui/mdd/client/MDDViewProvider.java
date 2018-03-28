package io.mateu.ui.mdd.client;

import com.google.auto.service.AutoService;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import io.mateu.ui.core.client.app.MateuUI;
import io.mateu.ui.core.client.views.AbstractCRUDView;
import io.mateu.ui.core.client.views.AbstractEditorView;
import io.mateu.ui.core.client.views.AbstractView;
import io.mateu.ui.core.client.views.CRUDListener;
import io.mateu.ui.core.shared.Data;
import io.mateu.ui.core.shared.ViewProvider;
import io.mateu.ui.mdd.server.ERPServiceImpl;

import java.lang.reflect.Constructor;
import java.util.Arrays;


@AutoService(ViewProvider.class)
public class MDDViewProvider implements ViewProvider {
    @Override
    public String getViewName(String viewAndParameters) {
        System.out.println("MDDViewProvider.getViewName(" + viewAndParameters + ")");
        String vn = viewAndParameters;
        if (vn.startsWith("area")) vn = vn.substring(vn.indexOf("..") + "..".length());
        if (vn.startsWith("pos")) vn = vn.substring(vn.indexOf("..") + "..".length());
        if (!(vn.startsWith("mdd.."))) return null;
        String p = viewAndParameters;
        if (p.contains("?")) p = p.substring(0, p.indexOf("?"));
        if (p.contains("/")) p = p.substring(0, p.indexOf("/"));
        return p;
    }

    @Override
    public AbstractView getView(String viewName) {
        System.out.println("MDDViewProvider.getView(" + viewName + ")");


        MDDJPACRUDView view =null;
        try {

            if (!Strings.isNullOrEmpty(viewName)) {

                String vn = viewName;
                if (vn.startsWith("area")) {
                    int pos = Integer.parseInt(vn.substring("area".length(), vn.indexOf("..")));
                    MateuUI.getApp().setArea(MateuUI.getApp().getAreas().get(pos));
                    vn = vn.substring(vn.indexOf("..") + "..".length());
                }
                if (vn.startsWith("pos")) {
                    String pos = vn.substring("area".length(), vn.indexOf(".."));
                    MateuUI.getApp().setPosicion(MateuUI.getApp().getMenu(pos));

                    vn = vn.substring(vn.indexOf("..") + "..".length());
                }
                if (vn.startsWith("mdd..")) vn = vn.replaceFirst("mdd\\.\\.", "");

                String[] t = vn.split("\\.\\.", -1);
                String ed = t[0];
                String vd = t[1];
                String qf = (!Strings.isNullOrEmpty(t[2]))?new String(BaseEncoding.base64().decode(t[2]), Charsets.UTF_8):null;
                String jsonDatosIniciales = (!Strings.isNullOrEmpty(t[3]))?new String(BaseEncoding.base64().decode(t[3]), Charsets.UTF_8):null;


                view = new MDDJPACRUDView(new ERPServiceImpl().getMetaData(MateuUI.getApp().getUserData(), ed, vd, qf));

                if (vn.endsWith("edit")) {
                    return view.getNewEditorView((jsonDatosIniciales != null)?new Data(jsonDatosIniciales):null);
                }

                if (view instanceof AbstractCRUDView) {
                    ((AbstractCRUDView) view).addListener(new CRUDListener() {
                        @Override
                        public void openEditor(AbstractEditorView e, boolean inNewTab) {
                            MateuUI.openView(e, inNewTab);
                        }
                    });
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }


    public static void main(String[] args) {
        System.out.println(Arrays.toString("com.quonext.quoon.platform.model.product.hotel.Hotel..com.quonext.quoon.platform.backoffice.hotel.views.HotelsView....".split("\\.\\.", -1)));
    }

}
