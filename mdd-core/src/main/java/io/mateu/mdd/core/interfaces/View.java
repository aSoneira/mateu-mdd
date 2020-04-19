package io.mateu.mdd.core.interfaces;

import io.mateu.mdd.core.data.UserData;

import javax.persistence.EntityManager;

public interface View<T> extends ListView<T> {

    default String getFields() {
        return null;
    }

    default T newInstance(EntityManager em, UserData user) {
        return null;
    }

}
