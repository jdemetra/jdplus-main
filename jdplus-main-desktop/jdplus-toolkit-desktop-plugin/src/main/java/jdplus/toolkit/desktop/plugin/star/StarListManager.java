package jdplus.toolkit.desktop.plugin.star;

import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import nbbrd.design.swing.OnEDT;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@GlobalService
public final class StarListManager implements Iterable<DataSource> {

    @NonNull
    public static StarListManager get() {
        return LazyGlobalService.get(StarListManager.class, StarListManager::new);
    }

    private final Set<DataSource> list;

    private StarListManager() {
        list = new HashSet<>();
    }

    @OnEDT
    public void clear() {
        list.clear();
    }

    @OnEDT
    public void toggle(DataSource dataSource) {
        if (list.contains(dataSource))
            list.remove(dataSource);
        else
            list.add(dataSource);
    }

    @OnEDT
    public void add(DataSource dataSource) {
        list.add(dataSource);
    }

    @OnEDT
    public void remove(DataSource dataSource) {
        list.remove(dataSource);
    }

    @OnEDT
    @Override
    public @lombok.NonNull Iterator<DataSource> iterator() {
        return list.iterator();
    }

    @OnEDT
    public boolean isStarred(DataSource dataSource) {
        return list.contains(dataSource);
    }
}
