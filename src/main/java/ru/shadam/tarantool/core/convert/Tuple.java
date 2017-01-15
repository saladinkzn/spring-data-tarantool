package ru.shadam.tarantool.core.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author sala
 */
public class Tuple {
    private final List<Object> inner;

    public Tuple() {
        this.inner = new ArrayList<>();
    }

    public Tuple(List<Object> inner) {
        this.inner = inner;
    }

    public Object get(Path path) {
        return getInternal(inner, path.getIndexes());
    }

    @SuppressWarnings("unchecked")
    private static Object getInternal(List<Object> inner, List<Integer> indexes) {
        if(indexes.isEmpty()) {
            return inner;
        }
        final Integer headIndex = indexes.get(0);
        if(indexes.size() == 1) {
            if(inner.size() <= headIndex) {
                return null;
            }
            return inner.get(headIndex);
        }
        final List<Integer> tailIndexes = indexes.subList(1, indexes.size());
        final List<Object> tuple = (List<Object>) inner.get(headIndex);
        return getInternal(tuple, tailIndexes);
    }

    public void set(Path path, Object value) {
        setInternal(inner, path.getIndexes(), value);
    }

    @SuppressWarnings("unchecked")
    private static void setInternal(List<Object> inner, List<Integer> indexes, Object value) {
        if(indexes.isEmpty()) {
            throw new IllegalArgumentException("indexes should not be empty");
        }
        final Integer index = indexes.get(0);
        ensureCapacity(inner, index);
        if(indexes.size() == 1) {
            inner.set(index, value);
            return;
        }
        final List<Object> tuple;
        if(inner.get(index) == null) {
            tuple = new ArrayList<>();
            inner.set(index, tuple);
        } else {
            tuple = (List<Object>) inner.get(index);
        }
        setInternal(tuple, indexes.subList(1, indexes.size()), value);
    }

    private static void ensureCapacity(List<Object> inner, int size) {
        int startSize = inner.size();
        for(int i = startSize; i <= size; i++) {
            inner.add(null);
        }
    }

    public int size() {
        return inner.size();
    }

    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public Tuple extract(Path currentPath) {
        final Object value = get(currentPath);
        final Tuple tuple = new Tuple();
        tuple.set(currentPath, value);
        return tuple;
    }

    public List<Object> getRaw() {
        return inner;
    }

    public List<Path> extractAllKeysFor(Path path) {
        final List<Object> internal = (List<Object>) get(path);
        if(internal == null) {
            return Collections.emptyList();
        }
        return IntStream.range(0, internal.size())
                .mapToObj(i -> Path.concat(path, i))
                .collect(Collectors.toList());
    }


    @Override
    public String toString() {
        return "Tuple{" +
                "inner=" + inner +
                '}';
    }
}
