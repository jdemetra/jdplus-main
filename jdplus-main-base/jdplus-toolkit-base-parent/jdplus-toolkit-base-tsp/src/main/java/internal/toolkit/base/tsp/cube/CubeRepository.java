package internal.toolkit.base.tsp.cube;

import jdplus.toolkit.base.api.util.Validatable;
import jdplus.toolkit.base.api.util.Validations;
import jdplus.toolkit.base.tsp.cube.*;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@lombok.Value
@lombok.Builder(buildMethodName = "buildWithoutValidation")
public class CubeRepository implements Validatable<CubeRepository> {

    @NonNull
    CubeId root;

    @lombok.Singular
    List<CubeSeriesWithData> items;

    @NonNull
    String name;

    @Override
    public @NonNull CubeRepository validate() throws IllegalArgumentException {
        Validations.notBlank(name, "name");
        allMatch(items, o -> root.isAncestorOf(o.getId()), "items");
        return this;
    }

    public @NonNull CubeConnection asConnection() {
        return new CubeRepositoryConnection();
    }

    public static final class Builder implements Validatable.Builder<CubeRepository> {
    }

    private final class CubeRepositoryConnection implements CubeConnection {

        @Override
        public @NonNull Optional<IOException> testConnection() {
            return Optional.empty();
        }

        @Override
        public @NonNull CubeId getRoot() {
            return root;
        }

        @Override
        public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId id) {
            checkNode(id);
            return items.stream().filter(ts -> id.isAncestorOf(ts.getId())).map(CubeSeriesWithData::withoutData);
        }

        @Override
        public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId id) {
            checkNode(id);
            return items.stream().filter(ts -> id.isAncestorOf(ts.getId()));
        }

        @Override
        public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) {
            checkLeaf(id);
            return items.stream().filter(ts -> id.equals(ts.getId())).map(CubeSeriesWithData::withoutData).findFirst();
        }

        @Override
        public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId id) {
            checkLeaf(id);
            return items.stream().filter(ts -> id.equals(ts.getId())).findFirst();
        }

        @Override
        public @NonNull Stream<CubeId> getChildren(@NonNull CubeId id) {
            checkNode(id);
            return items.stream()
                    .map(HasCubeId::getId)
                    .filter(id::isAncestorOf)
                    .map(child -> child.getDimensionValue(id.getLevel()))
                    .distinct()
                    .sorted()
                    .map(id::child);
        }

        @Override
        public @NonNull String getDisplayName() {
            return name;
        }

        @Override
        public @NonNull String getDisplayName(@NonNull CubeId id) {
            return TableAsCubeUtil.getDisplayName(id, joining(", "));
        }

        @Override
        public @NonNull String getDisplayNodeName(@NonNull CubeId id) {
            return TableAsCubeUtil.getDisplayNodeName(id);
        }

        @Override
        public void close() {
        }
    }

    @MightBePromoted
    private static <T, C extends Collection<T>> C allMatch(C actual, @NonNull Predicate<T> expected, @NonNull String message) throws IllegalArgumentException {
        return Validations.on(actual, collection -> collection.stream().allMatch(expected), o -> message);
    }

    public static CubeId checkNode(CubeId id) {
        if (id.isSeries() || id.isVoid()) {
            throw new IllegalArgumentException(id.toString());
        }
        return id;
    }

    public static CubeId checkLeaf(CubeId id) {
        if (!id.isSeries() && !id.isVoid()) {
            throw new IllegalArgumentException(id.toString());
        }
        return id;
    }
}
