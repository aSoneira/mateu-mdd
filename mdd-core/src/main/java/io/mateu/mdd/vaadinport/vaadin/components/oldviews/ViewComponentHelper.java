package io.mateu.mdd.vaadinport.vaadin.components.oldviews;

import com.google.common.base.Strings;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.app.AbstractAction;
import io.mateu.mdd.core.app.MDDExecutionContext;
import io.mateu.mdd.core.data.UserData;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.core.util.JPATransaction;
import io.mateu.mdd.core.views.AbstractServerSideWizard;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ViewComponentHelper {
    public static AbstractAction createAction(Method m, AbstractViewComponent viewComponent) {

        Action aa = m.getAnnotation(Action.class);

        AbstractAction action = new AbstractAction(m.getName(), (!Strings.isNullOrEmpty(aa.value())) ? aa.value() : Helper.capitalize(m.getName())) {
            @Override
            public void run(MDDExecutionContext context) {

                boolean allInjectable = true;
                boolean needsTransaction = false;
                for (Parameter p : m.getParameters()) {
                    if (EntityManager.class.equals(p.getType())) {
                        needsTransaction = true;
                    } else if (UserData.class.equals(p.getType())) {
                    } else if (Set.class.isAssignableFrom(p.getType())) {
                    } else {
                        allInjectable = false;
                    }
                }

                if (!allInjectable) { // si necesita rellenar parámetros
                    MDD.getPort().open(m);
                } else { // si no tiene parámetros o si todos son inyectables

                    try {
                        // necesita transacción?

                        Object instance = null;

                        if (viewComponent instanceof EditorViewComponent) {
                            EditorViewComponent evc = (EditorViewComponent) viewComponent;
                            instance = evc.getModel();
                        }

                        Set selection = new HashSet();
                        if (viewComponent instanceof ListViewComponent) {
                            ListViewComponent lvc = (ListViewComponent) viewComponent;
                            Helper.notransact(em -> {
                                boolean jpa = lvc.getColumnType().isAnnotationPresent(Entity.class);
                                lvc.getSelection().forEach(o -> {
                                    if (jpa && o instanceof Object[]) {
                                        selection.add(em.find(lvc.getColumnType(), lvc.deserializeId("" + lvc.toId(o))));
                                    } else {
                                        selection.add(o);
                                    }
                                });
                            });
                        }


                        if (needsTransaction) {

                            Object finalInstance = instance;
                            Set finalSelection = selection;
                            Helper.transact(new JPATransaction() {
                                @Override
                                public void run(EntityManager em) throws Throwable {

                                    invoke(m, finalInstance, finalSelection, em, null);

                                }
                            });

                        } else {

                            invoke(m, instance, selection, null, null);

                        }

                    } catch (Throwable throwable) {
                        MDD.alert(throwable);
                    }

                    if (viewComponent instanceof ListViewComponent) {
                        ListViewComponent lvc = (ListViewComponent) viewComponent;
                        try {
                            lvc.search(lvc.getModelForSearchFilters());
                        } catch (Throwable throwable) {
                            MDD.alert(throwable);
                        }
                    }
                }


            }
        }.setIcon(aa.icon()).setConfirmationMessage(aa.confirmationMessage());

        return action;

    }

    private static void invoke(Method m, Object instance, Set selection, EntityManager em, Map<String, Object> parameterValues) throws InvocationTargetException, IllegalAccessException {
        List<Object> vs = new ArrayList<>();
        for (Parameter p : m.getParameters()) {
            if (EntityManager.class.equals(p.getType())) {
                vs.add(em);
            } else if (UserData.class.equals(p.getType())) {
                vs.add(MDD.getUserData());
            } else if (Set.class.isAssignableFrom(p.getType())) {
                vs.add(selection);
            } else if (AbstractServerSideWizard.class.isAssignableFrom(p.getType())) {
                //todo: acabar
                //vs.add(fillWizard(user, em, p.getType(), parameters.get(p.getName())));
                vs.add(null);
            } else {
                vs.add((parameterValues != null)?parameterValues.get(p.getName()):null);
            }
        }
        Object[] args = vs.toArray();

        m.invoke(instance, args);
    }
}
