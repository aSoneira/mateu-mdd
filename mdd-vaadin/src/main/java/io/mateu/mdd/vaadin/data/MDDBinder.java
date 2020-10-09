package io.mateu.mdd.vaadin.data;

import com.vaadin.data.*;
import com.vaadin.server.Setter;
import io.mateu.mdd.shared.interfaces.IBinder;
import io.mateu.mdd.shared.reflection.FieldInterfaced;
import io.mateu.mdd.vaadin.components.views.AbstractViewComponent;
import io.mateu.mdd.vaadin.components.views.EditorViewComponent;
import io.mateu.reflection.FieldInterfacedFromField;
import io.mateu.reflection.ReflectionHelper;
import io.mateu.util.notification.Notifier;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MDDBinder extends Binder implements IBinder {

    private AbstractViewComponent viewComponent;
    private final Class beanType;
    private boolean updating;

    public MDDBinder(List<FieldInterfaced> fields) {
        super(new PropertySet() {

            private Map<String, PropertyDefinition> definitions = createDefinitions(this, fields);

            @Override
            public Stream<PropertyDefinition> getProperties() {
                return definitions.values().stream();
            }

            @Override
            public Optional<PropertyDefinition> getProperty(String s) {
                return Optional.of(definitions.get(s));
            }
        });
        this.beanType = new HashMap<String, Object>().getClass();
    }


    public static Map<String,PropertyDefinition> createDefinitions(PropertySet propertySet, List<FieldInterfaced> fields) {
        Map<String,PropertyDefinition> defs = new HashMap<>();

        fields.forEach(f -> defs.put(f.getName(), new PropertyDefinition() {
            private ValueProvider valueProvider = new ValueProvider() {
                @Override
                public Object apply(Object o) {
                    try {
                        return ReflectionHelper.getValue(f, o);
                    } catch (Exception e) {
                        Notifier.alert(e);
                        return null;
                    }
                }
            };

            @Override
            public ValueProvider getGetter() {
                return valueProvider;
            }

            @Override
            public Optional<Setter> getSetter() {
                return Optional.of(new Setter() {
                    @Override
                    public void accept(Object o, Object v) {
                        try {
                            ReflectionHelper.setValue(f, o, v);
                        } catch (Exception e) {
                            Notifier.alert(e);
                        }
                    }
                });
            }

            @Override
            public Class getType() {
                if (int.class.equals(f.getType())) return Integer.class;
                if (long.class.equals(f.getType())) return Long.class;
                if (float.class.equals(f.getType())) return Float.class;
                if (double.class.equals(f.getType())) return Double.class;
                if (boolean.class.equals(f.getType())) return Boolean.class;
                else return f.getType();
            }

            @Override
            public Class<?> getPropertyHolderType() {
                return f.getDeclaringClass();
            }

            @Override
            public String getName() {
                return f.getName();
            }

            @Override
            public String getCaption() {
                return ReflectionHelper.getCaption(f);
            }

            @Override
            public PropertySet getPropertySet() {
                return propertySet;
            }
        }));

        return defs;
    }

    public MDDBinder(Class beanType) {
        super(beanType);
        this.beanType = beanType;
    }


    public MDDBinder(Class beanType, EditorViewComponent component) {
        super(beanType);
        this.beanType = beanType;
        this.viewComponent = component;
    }

    public AbstractViewComponent getViewComponent() {
        return viewComponent;
    }

    public void setViewComponent(AbstractViewComponent viewComponent) {
        this.viewComponent = viewComponent;
    }


    @Override
    public void setBean(Object bean) {
        setBean(bean, true);
    }

    public void setBean(Object bean, boolean reset) {
        super.setBean(bean);
    }

    public Class getBeanType() {
        return beanType;
    }

    public void refresh() {
        setBean(getBean(), false);
    }

    public void update(Object model) {
        if (!updating) {
            updating = true;
            if (model != null) {
                getBindings().forEach(b -> {
                    ((Binding) b).read(model);
                });
                Field v;
                if ((v = ReflectionHelper.getVersionField(model.getClass())) != null) {
                    try {
                        v.setAccessible(true);
                        v.set(getBean(), v.get(model));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (FieldInterfaced f : ReflectionHelper.getAllFields(model.getClass())) if (f instanceof FieldInterfacedFromField) {
                    try {
                        f.getField().setAccessible(true);
                        f.getField().set(getBean(), f.getField().get(model));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                getFields().forEach(f -> {
                    HasValue hv = (HasValue) f;
                    hv.setValue(null);
                });
            }
            updating = false;
        }
    }

    public Optional<Binding> getFieldBinding(HasValue hv) {
        return getBindings().stream().filter(b -> ((Binding)b).getField() == hv).findFirst();
    }
}
