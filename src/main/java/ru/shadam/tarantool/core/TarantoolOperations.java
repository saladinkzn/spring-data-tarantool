package ru.shadam.tarantool.core;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.shadam.tarantool.core.update.Operation;

import java.util.Collections;
import java.util.List;

/**
 * Basic crud operations for Tarantool
 *
 * @author sala
 * @since 0.2.0
 */
public interface TarantoolOperations<K, V> {
    /**
     * Select all entities from space {@code space} by primary index
     * @param space name of space to select tuples from
     * @return List of entities
     */
    default List<V> select(String space) {
        return select(space, new PageRequest(0, Integer.MAX_VALUE));
    }

    /**
     * Select all entities from space {@code spaceId} by index {@code indexId}
     * @param space name of space to select tuples from
     * @param index id of index to use
     * @return List of entities
     */
    default List<V> select(String space, String index) {
        return select(space, index, new PageRequest(0, Integer.MAX_VALUE));
    }

    /**
     * Select all entities from space {@code space} by primary index paginated by {@code pageable}
     * @param space name of space to select entities from
     * @param pageable PageRequest to limit/offset result
     * @return
     */
    List<V> select(String space, Pageable pageable);

    /**
     * Select all entities from space {@code spaceId} by index {@code indexId} paginated by {@code pageable}
     * @param space name of space to select entities from
     * @param index index to use
     * @param pageable PageRequest to limit/offset result
     * @return List of entities
     */
    List<V> select(String space, String index, Pageable pageable);

    default V select(String space, K key) {
        final List<V> result = select(space, key, new PageRequest(0, 1), Iterator.EQ);
        if(result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new IllegalStateException("Too many entities in result");
        }
    }

    /**
     * Select single entity from space {@code spaceId} by index {@code indexId} and key {@code key}
     * @param space name of space to select entity from
     * @param index index to use
     * @param key key to use
     * @return
     */
    default V select(String space, String index, K key) {
        final List<V> result = select(space, index, key, new PageRequest(0, 1), Iterator.EQ);
        if(result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new IllegalStateException("Too many entities in result");
        }
    }

    /**
     * Select entities from space {@code space} by primary index by key {@code key} using iterator {@code iterator}
     * paginated by {@code pageable}
     * @param space name of space to select entities from
     * @param key key to user
     * @param pageable PageRequest to limit/offset result
     * @param iterator {@link Iterator} to use
     * @return List of entities
     */
    List<V> select(String space, K key, Pageable pageable, Iterator iterator);

    /**
     * Select entities from space {@code spaceId} by index {@code indexId} by key {@code key} using iterator {@code iterator}
     * paginated by {@code pageable}
     * @param space name of space to select entities from
     * @param index id of index to use
     * @param key key to use
     * @param pageable PageRequest to limit/offset result
     * @param iterator {@link Iterator} to use
     * @return List of intities
     */
    List<V> select(String space, String index, K key, Pageable pageable, Iterator iterator);

    /**
     * Insert entity into space
     * @param space name of space to replace to
     * @param value value to replace
     * @return inserted value
     */
    V insert(String space, K key, V value);

    /**
     * Insert entity into space if key doesn't exists or replace by
     * @param space
     * @param key
     * @param value
     * @return
     */
    V replace(String space, K key, V value);

    /**
     * Apply single operation to entity selected by key
     * @param space name of space to select entities from
     * @param index name of index to use
     * @param key key to use
     * @param operation {@link Operation} to apply
     * @return result of operation
     */
    default V update(String space, String index, K key, Operation operation) {
        return update(space, index, key, Collections.singletonList(operation));
    }

    /**
     * Apply multiple operations to entity selected by key
     * @param space space to select entities
     * @param key key to use
     * @param operations {@link Operation} to apply
     * @return result of operation
     */
    V update(String space, K key, List<Operation> operations);

    /**
     * Apply multiple operations to entity selected by key
     * @param space name of space to select entities from
     * @param index name of index to use
     * @param key key to use
     * @param operation {@link Operation} to apply
     * @return result of operation
     */
    V update(String space, String index, K key, List<Operation> operation);

    /**
     * If entity associated with key exists, apply operations otherwise replace value
     * @param space name of space to select entities from
     * @param key key to use
     * @param value value to replace
     * @param operations list of {@link Operation} to apply
     */
    void upsert(String space, K key, V value, List<Operation> operations);

    /**
     * Delete entity by id
     * @param space name of space to delete entities from
     * @param key key to use
     * @return deleted entity
     */
    V delete(String space, K key);

    void deleteAll(String space);
}
