package io.mateu.mdd.core.app;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Found {

    private String path;

    private String name;

    private String description;

    public Found() {
    }

    public Found(String path, String name, String description) {
        this.path = path;
        this.name = name;
        this.description = description;
    }
}
