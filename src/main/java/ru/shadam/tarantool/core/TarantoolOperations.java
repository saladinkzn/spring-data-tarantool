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
     * Select all entities from space {@code spaceId} by index {@code indexId}
     * @param spaceId id of space to select tuples from
     * @param indexId id of index to use
     * @return List of entities
     */
    default List<V> select(int spaceId, int indexId) {
        return select(spaceId, indexId, new PageRequest(0, Integer.MAX_VALUE));
    }

    /**
     * Select all entities from space {@code spaceId} by index {@code indexId} paginated by {@code pageable}
     * @param spaceId id of space to select entities from
     * @param indexId id of index to use
     * @param pageable PageRequest to limit/offset result
     * @return List of entities
     */
    List<V> select(int spaceId, int indexId, Pageable pageable);

    /**
     * Select single entity from space {@code spaceId} by index {@code indexId} and key {@code key}
     * @param spaceId id of space to select entity from
     * @param indexId id of index to use
     * @param key key to use
     * @return
     */
    default V select(int spaceId, int indexId, K key) {
        final List<V> result = select(spaceId, indexId, key, new PageRequest(0, 1), Iterator.EQ);
        if(result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new IllegalStateException("Too many entities in result");
        }
    }

    /**
     * Select entities from space {@code spaceId} by index {@code indexId} by key {@code key} using iterator {@code iterator}
     * paginated by {@code pageable}
     * @param spaceId id of space to select entities from
     * @param indexId id of index to use
     * @param key key to use
     * @param pageable PageRequest to limit/offset result
     * @param iterator {@link Iterator} to use
     * @return List of intities
     */
    List<V> select(int spaceId, int indexId, K key, Pageable pageable, Iterator iterator);

    /**
     * Insert entity into space
     * @param spaceId id of space to insert to
     * @param value value to insert
     * @return inserted value
     */
    V insert(int spaceId, V value);

    /**
     * Insert entity into space if key doesn't exists or replace by
     * @param spaceId
     * @param value
     * @return
     */
    V replace(int spaceId, V value);

    /**
     * Apply single operation to entity selected by key
     * @param spaceId id of space to select entities from
     * @param indexId id of index to use
     * @param key key to use
     * @param operation {@link Operation} to apply
     * @return result of operation
     */
    default V update(int spaceId, int indexId, K key, Operation operation) {
        return update(spaceId, indexId, key, Collections.singletonList(operation));
    }

    /**
     * Apply multiple operations to entity selected by key
     * @param spaceId id of space to select entities from
     * @param indexId id of index to use
     * @param key key to use
     * @param operation {@link Operation} to apply
     * @return result of operation
     */
    V update(int spaceId, int indexId, K key, List<Operation> operation);

    /**
     * If entity associated with key exists, apply operations otherwise insert value
     * @param spaceId id of space to select entities from
     * @param key key to use
     * @param value value to insert
     * @param operations list of {@link Operation} to apply
     */
    void upsert(int spaceId, K key, V value, List<Operation> operations);

    /**
     * Delete entity by id
     * @param spaceId id of space to delete entities from
     * @param key key to use
     * @return deleted entity
     */
    V delete(int spaceId, K key);
}
