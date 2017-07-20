package ru.shadam.tarantool.core;

import ru.shadam.tarantool.core.convert.TarantoolData;

import java.io.Serializable;
import java.util.List;

/**
 * @author sala
 */
public interface TarantoolOps {
    boolean contains(String space, Serializable id);

    long count(String space);

    long countByIndex(String keyspace, String index, Serializable id);

    TarantoolData delete(String space, Serializable id);

    void deleteAll(String space);

    TarantoolData get(String space, Serializable id);

    List<TarantoolData> getAllOf(String space);

    void replace(String space, TarantoolData tuple);

    TarantoolData autoIncrement(String space, TarantoolData tuple);

    default List<TarantoolData> getAllOf(String keyspace, int offset, int rows) {
        return getAllOf(keyspace, null, offset, rows);
    }

    List<TarantoolData> getAllOf(String keyspace, Iterator iterator, int offset, int rows);

    List<TarantoolData> getByIndex(String keyspace, String index, Serializable id, int offset, int rows);

    List<TarantoolData> getByIndex(String keyspace, String index, Iterator iterator, int offset, int rows);
}
