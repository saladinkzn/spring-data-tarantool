package ru.shadam.tarantool.core;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.shadam.tarantool.core.update.Operation;

import java.util.Collections;
import java.util.List;

/**
 * @author sala
 */
public interface TarantoolOperations<K, V> {

    default List<V> select(int spaceId, int indexId) {
        return select(spaceId, indexId, new PageRequest(0, Integer.MAX_VALUE));
    }

    List<V> select(int spaceId, int indexId, Pageable pageable);

    default List<V> select(int spaceId, int indexId, K key) {
        return select(spaceId, indexId, key, new PageRequest(0, 1), Iterator.EQ);
    }

    List<V> select(int spaceId, int indexId, K key, Pageable pageable, Iterator iterator);

    V insert(int spaceId, V value);

    V replace(int spaceId, V value);

    default V update(int spaceId, int indexId, K key, Operation operation) {
        return update(spaceId, indexId, key, Collections.singletonList(operation));
    }

    V update(int spaceId, int indexId, K key, List<Operation> operation);

    void upsert(int spaceId, K key, V value, List<Operation> operations);

    V delete(int spaceId, K key);
}
