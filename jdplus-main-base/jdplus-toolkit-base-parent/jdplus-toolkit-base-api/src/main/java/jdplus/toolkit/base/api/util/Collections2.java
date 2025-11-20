package jdplus.toolkit.base.api.util;

import jdplus.toolkit.base.api.data.Seq;
import lombok.NonNull;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@lombok.experimental.UtilityClass
public class Collections2 {

    public <T> @NonNull Stream<T> streamOf(@NonNull Iterable<T> iterable) {
        if (iterable instanceof Collection) return ((Collection<T>) iterable).stream();
        if (iterable instanceof Seq) return ((Seq<T>) iterable).stream();
        return StreamSupport.stream(iterable.spliterator(), false);
    }
    
    public boolean isNullOrEmpty(Collection<?> coll){
        return coll == null || coll.isEmpty();
    }
}
