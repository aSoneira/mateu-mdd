package io.mateu.showcase.tester.app.helloWorld;

import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.MateuMDDApp;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.app.MDDOpenCRUDAction;
import io.mateu.mdd.core.app.SimpleMDDApplication;
import io.mateu.showcase.tester.model.entities.groups.Person;

@MateuMDDApp(path = "/hello")
public class HelloWorldApp extends SimpleMDDApplication {

    @Action
    public AbstractAction people() {

        return new MDDOpenCRUDAction(Person.class);

    }

}
