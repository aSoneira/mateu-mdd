package io.mateu.mdd.core.app;

import java.util.List;
import java.util.UUID;

/**
 * Created by miguel on 9/8/16.
 */
public abstract class AbstractMenu implements MenuEntry {

    private final String id = UUID.randomUUID().toString();

    private String name;
    private List<MenuEntry> entries;

    public AbstractMenu(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<MenuEntry> getEntries() {
        if (entries == null) synchronized (this) {
            entries = buildEntries();
        }
        return entries;
    }

    public abstract List<MenuEntry> buildEntries();

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }
}
