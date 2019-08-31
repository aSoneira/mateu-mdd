package io.mateu.mdd.core.interfaces;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

public interface WizardPage {

    WizardPage getPrevious();

    boolean hasNext();

    WizardPage getNext();

    default boolean isValid() {
        return true;
    }

    default void onOk() throws IOException, Throwable {

    }

    default boolean backOnOk() { return true; }

}
