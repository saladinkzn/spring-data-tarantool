package ru.shadam.tarantool.core.convert;

import org.springframework.util.Assert;

import java.util.*;

/**
 * @author sala
 */
public class Path {
    private List<Integer> indexes;

    private Path(List<Integer> indexes) {
        this.indexes = Objects.requireNonNull(indexes);
    }

    public List<Integer> getIndexes() {
        return Collections.unmodifiableList(indexes);
    }

    public static Path empty() {
        return new Path(Collections.emptyList());
    }

    public static Path of(Integer index) {
        Assert.notNull(index, "index cannot be null");

        return new Path(Collections.singletonList(index));
    }

    public static Path of(Integer... indexes) {
        Assert.noNullElements(indexes);

        return new Path(Arrays.asList(indexes));
    }

    public static Path concat(Path path, Integer index) {
        Assert.notNull(index, "index cannot be null");

        if(path.isEmpty()) {
            return Path.of(index);
        }

        final List<Integer> list = new ArrayList<>();
        list.addAll(path.indexes);
        list.add(index);
        return new Path(list);
    }

    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Path path = (Path) o;

        return indexes.equals(path.indexes);
    }

    @Override
    public int hashCode() {
        return indexes.hashCode();
    }

    @Override
    public String toString() {
        return "Path{" +
                "indexes=" + indexes +
                '}';
    }
}
