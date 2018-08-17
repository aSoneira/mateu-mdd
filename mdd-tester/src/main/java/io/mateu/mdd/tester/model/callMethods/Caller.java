package io.mateu.mdd.tester.model.callMethods;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.interfaces.PushWriter;
import io.mateu.mdd.tester.model.entities.groups.Person;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class Caller {

    public static void alert() {
        MDD.alert("Mensaje");
    }

    public static void doSomething() throws InterruptedException {
        Thread.sleep(3000);
    }

    public static int returnInt() {
        return 10;
    }

    public static String returnString(@NotEmpty String yourName) {
        return "Hello " + yourName;
    }


    public static Component returnsComponent(String yourName, int age) {
        return new Label("Hello " + yourName + "/" + age + ". This is a Vaadin Label!");
    }

    public static POJO returnsReadOnlyPojo(String name, int age) {
        return new POJO(name, age);
    }

    public static POJO returnsDefaultEditablePojo(String name, int age) {
        return new POJO(name, age);
    }

    public static Person returnsEntity(EntityManager em) {
        return (Person) em.createQuery("select x from " + Person.class.getName() + " x").getResultList().get(0);
    }

    public static List<Person> returnsList(EntityManager em) {
        return em.createQuery("select x from " + Person.class.getName() + " x").getResultList();
    }

    public static void throwsException() throws Exception {
        throw new Exception("Here comes the error message");
    }


    public static Person throwsException2(EntityManager em) {
        return (Person) em.createQuery("select x.iewhei from " + Person.class.getName() + " x").getResultList().get(0);
    }

    public static void longRunning(PushWriter pw) throws Exception {
        int maxSeconds = 20;
        for (int i = 0; i < maxSeconds; i++) {
            pw.push("Completed " + Math.round(100d * i / maxSeconds) + "%");
            Thread.sleep(1000);
        }
        pw.done("Finished");
    }

}
