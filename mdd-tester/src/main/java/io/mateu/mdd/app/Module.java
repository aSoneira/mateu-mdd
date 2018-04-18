package io.mateu.mdd.app;

import io.mateu.mdd.model.authentication.AuthToken;
import io.mateu.mdd.model.authentication.User;
import io.mateu.mdd.model.config.AppConfig;
import io.mateu.mdd.model.finnancials.Actor;
import io.mateu.mdd.model.finnancials.Currency;
import io.mateu.mdd.model.finnancials.Invoice;
import io.mateu.mdd.model.hotelcontract.HotelContract;
import io.mateu.mdd.model.rpc.Hotel;
import io.mateu.mdd.model.rpc.Room;
import io.mateu.mdd.model.rpc.StopSalesListView;
import io.mateu.mdd.model.rpc.StopSalesView;
import io.mateu.mdd.model.tests.basic.Basico;
import io.mateu.mdd.model.tests.herencia.Abstracta;
import io.mateu.mdd.model.tests.multiidioma.Traducido;
import io.mateu.mdd.model.tests.onetomany.Uno;
import io.mateu.mdd.model.tests.onetomanyowned.UnoOwned;
import io.mateu.mdd.model.tests.onetoone.UnoAUnoMapped;
import io.mateu.mdd.model.tests.onetoone.UnoAUnoMapper;
import io.mateu.mdd.model.tests.owned.Propietario;
import io.mateu.mdd.model.tests.showmethodresult.ConMetodos;
import io.mateu.mdd.model.tests.stereotype.Estereotipado;
import io.mateu.mdd.model.tests.tabs.Tabs;
import io.mateu.mdd.model.tests.usaridparaseleccionar.Referenciador;
import io.mateu.mdd.model.tests.view.*;
import io.mateu.ui.core.client.app.*;
import io.mateu.ui.core.shared.Data;
import io.mateu.ui.mdd.client.*;
import io.mateu.ui.mdd.shared.ERPService;

import java.util.ArrayList;
import java.util.List;

public class Module extends AbstractModule {
    @Override
    public String getName() {
        return "Module";
    }

    @Override
    public List<MenuEntry> buildMenu() {
        List<MenuEntry> m = new ArrayList<>();

        m.add(new MDDOpenEditorAction("AppConfig", AppConfig.class, 1l));

        m.add(new MDDAction("Users", User.class));

        m.add(new MDDAction("Auth tokens", AuthToken.class));

        m.add(new MDDAction("Actors", Actor.class));

        m.add(new MDDAction("Currencies", Currency.class));

        m.add(new MDDAction("Invoices", Invoice.class));

        m.add(new MDDAction("Tabs", Tabs.class));

        m.add(new MDDAction("Methods as fields", ConMetodos.class));

        m.add(new MDDAction("Basic", Basico.class));

        m.add(new MDDAction("UseIdToSelect", Referenciador.class));

        m.add(new MDDAction("OneToMany", Uno.class));

        m.add(new MDDAction("OneToManyOwned", UnoOwned.class));

        m.add(new MDDAction("Owned", Propietario.class));

        m.add(new MDDAction("OneToOne mapped", UnoAUnoMapped.class));

        m.add(new MDDAction("OneToOne mapper", UnoAUnoMapper.class));


        m.add(new MDDAction("Contracts", HotelContract.class));

        m.add(new MDDAction("Agencies", Agency.class));

        m.add(new MDDAction("Booking", Booking.class));

        m.add(new MDDAction("View 1", View1.class));

        m.add(new MDDAction("View 2", View2.class));

        m.add(new MDDAction("View 3", View3.class));

        m.add(new MDDAction("View 4", View4.class));

        m.add(new MDDAction("Estereotipado", Estereotipado.class));

        m.add(new MDDAction("Hotels", Hotel.class));

        m.add(new MDDAction("Rooms", Room.class));

        m.add(new MDDAction("Stop sales", StopSalesView.class));


        m.add(new MDDAction("Stop sales from hotel", StopSalesListView.class));


        m.add(new MDDAction("Abstracta", Abstracta.class));

        m.add(new MDDAction("Multiidioma", Traducido.class));

        m.add(new MDDAction("Bookings restricted", Booking.class) {
            @Override
            public String getQueryFilters() {
                return "x.agency.id = " + MateuUI.getApp().getUserData().get("agencyId");
            }
        });

        return m;
    }
}
