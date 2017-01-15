package ru.shadam.tarantool.test.util;

import ru.shadam.tarantool.core.convert.Path;
import ru.shadam.tarantool.core.convert.Tuple;

import java.util.List;
import java.util.Map;

/**
 * @author sala
 */
public class Tuples {
    public static Tuple of(Path key, Object value) {
        final Tuple tuple = new Tuple();
        tuple.set(key, value);
        return tuple;
    }

    public static Tuple of(Path key, Object value, Path key2, Object value2) {
        final Tuple tuple = new Tuple();
        tuple.set(key, value);
        tuple.set(key2, value2);
        return tuple;
    }

    public static Tuple of(List<Map.Entry<Path, Object>> tuples) {
        final Tuple tuple = new Tuple();
        tuples.forEach(t -> tuple.set(t.getKey(), t.getValue()));
        return tuple;
    }
}
