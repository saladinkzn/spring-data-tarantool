package ru.shadam.tarantool.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.update.Operation;
import ru.shadam.tarantool.serializer.JdkTarantoolSerializer;
import ru.shadam.tarantool.serializer.PlainTarantoolSerializer;
import ru.shadam.tarantool.serializer.TarantoolSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author sala
 */
public class TarantoolTemplate<K, V> implements TarantoolOperations<K,V>, InitializingBean {
    private final static Log log = LogFactory.getLog(TarantoolTemplate.class);

    private int keySize = 1;

    private TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps;

    private boolean initialized = false;
    private boolean enableDefaultSerializer = true;

    private Class<K> keyClass;
    private TarantoolSerializer<K> keySerializer;
    private TarantoolSerializer<V> valueSerializer;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(syncOps, "SyncOps is required");

        if (enableDefaultSerializer) {

            if (keySerializer == null) {
                Assert.notNull(keyClass, "KeyClass is required if keySerializer is not provided");
                keySerializer = new PlainTarantoolSerializer<>(keyClass);
            }
            if (valueSerializer == null) {
                valueSerializer = (TarantoolSerializer<V>) new JdkTarantoolSerializer();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> select(int spaceId, int indexId, Pageable pageable) {
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.select(spaceId, indexId, Collections.emptyList(), pageable.getOffset(), pageable.getPageSize(), Iterator.ALL.getValue());
        return listOfTuples.stream()
                .map(it -> (V) valueSerializer.deserialize(extractValue(it)))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> select(int spaceId, int indexId, K key, Pageable pageable, Iterator iterator) {
        List<?> keyTuple = keySerializer.serialize(key);

        final List<List<?>> tuples = (List<List<?>>) syncOps.select(spaceId, indexId, keyTuple, pageable.getOffset(), pageable.getPageSize(), iterator.getValue());

        return tuples.stream()
                .map(it -> (V) valueSerializer.deserialize(extractValue(it)))
                .collect(Collectors.toList());
    }

    @Override
    public List<K> selectKeys(int spaceId, int indexId) {
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.select(spaceId, indexId, Collections.emptyList(), 0, Integer.MAX_VALUE, Iterator.ALL.getValue());
        return listOfTuples.stream()
                .map(it -> (K) keySerializer.deserialize(extractKey(it)))
                .collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V insert(int spaceId, K key, V value) {
        List tuple = convertToTuple(keySerializer.serialize(key), valueSerializer.serialize(value));

        final List<List<?>> tuples = ((List<List<?>>) syncOps.insert(spaceId, tuple));

        if (tuples.isEmpty()) {
            return null;
        }

        List<?> result = tuples.get(0);

        // TODO: or it's a list of tuple?
        return (V) valueSerializer.deserialize(extractValue(result));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V replace(int spaceId, K key, V value) {
        final List serializedValue = valueSerializer.serialize(value);
        List valueTuple = serializedValue;

        final List result = syncOps.replace(spaceId, valueTuple);

        return ((V) valueSerializer.deserialize(extractValue(result)));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public V update(int spaceId, int indexId, K key, List<Operation> operation) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = (List) serializedKey;

        final List<?>[] listOfOperations = operation.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.update(spaceId, keyTuple, (Object[]) listOfOperations);
        final List<?> tuple = listOfTuples.get(0);

        return ((V) valueSerializer.deserialize(extractValue(tuple)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert(int spaceId, K key, V value, List<Operation> operations) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = (List) serializedKey;

        final Object serializedValue = valueSerializer.serialize(value);
        List valueTuple = (List) serializedValue;

        final List[] listOfOperations = operations.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        syncOps.upsert(spaceId, keyTuple, valueTuple, (Object[]) listOfOperations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V delete(int spaceId, K key) {
        final List serializedKey = keySerializer.serialize(key);

        final List tuple = syncOps.delete(spaceId, serializedKey);
        return (V) valueSerializer.deserialize(extractValue(tuple));
    }

    private List convertToTuple(List key, List value) {
        List<Object> result = new ArrayList<>();
        result.addAll(Objects.requireNonNull(key));
        result.addAll(Objects.requireNonNull(value));
        return result;
    }

    private List extractKey(List<?> it) {
        if(it.isEmpty()) {
            return Collections.emptyList();
        }
        return it.subList(0, keySize);
    }

    private List extractValue(List tuple) {
        if(tuple.isEmpty()) {
            return Collections.emptyList();
        }

        return tuple.subList(keySize, tuple.size());
    }

    public void setSyncOps(TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps) {
        this.syncOps = syncOps;
    }

    public void setEnableDefaultSerializer(boolean enableDefaultSerializer) {
        this.enableDefaultSerializer = enableDefaultSerializer;
    }

    public void setKeySerializer(TarantoolSerializer<K> keySerializer) {
        this.keySerializer = keySerializer;
    }

    public void setValueSerializer(TarantoolSerializer<V> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public void setKeyClass(Class<K> keyClass) {
        this.keyClass = keyClass;
    }
}
