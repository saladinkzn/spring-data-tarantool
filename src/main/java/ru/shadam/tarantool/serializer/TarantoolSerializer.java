package ru.shadam.tarantool.serializer;

import java.util.List;

/**
 * @author sala
 */
public interface TarantoolSerializer<T> {

    Object serialize(T value);

    T deserialize(List value);
}
