package jdplus.toolkit.base.api.util;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@lombok.Value
public class NamedObject<T> implements Comparable<NamedObject<T>> {

    @NonNull String name;
    @Nullable T object;

    @Override
    public int compareTo(NamedObject<T> o) {
        int result = name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        if (object instanceof Comparable) {
            return ((Comparable) object).compareTo(o.object);
        }
        return Objects.equals(object, o.object) ? 0 : 1;
    }

}
