package io.mateu.mdd.core.annotations;

import com.vaadin.icons.VaadinIcons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by miguel on 18/1/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD}) //can use in method only.
public @interface PrivateHome {

    String value() default "";

    VaadinIcons icon() default VaadinIcons.ADOBE_FLASH;

    int order() default 0;

}