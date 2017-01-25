package ru.shadam.tarantool.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.update.Operation;
import ru.shadam.tarantool.serializer.PlainTarantoolSerializer;
import ru.shadam.tarantool.serializer.TarantoolSerializer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sala
 */
public class TarantoolTemplate<K, V> implements TarantoolOperations<K,V>, InitializingBean {
    private final static Log log = LogFactory.getLog(TarantoolTemplate.class);

    private TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps;

    private boolean initialized = false;
    private boolean enableDefaultSerializer = true;
    private TarantoolSerializer<?> defaultSerializer;

    private TarantoolSerializer keySerializer;
    private TarantoolSerializer valueSerializer;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(syncOps, "SyncOps is required");

        boolean defaultUsed = false;

        if(defaultSerializer == null) {
            this.defaultSerializer = new PlainTarantoolSerializer<>();
        }

        if (enableDefaultSerializer) {

            if (keySerializer == null) {
                keySerializer = defaultSerializer;
                defaultUsed = true;
            }
            if (valueSerializer == null) {
                valueSerializer = defaultSerializer;
                defaultUsed = true;
            }
        }

        if (enableDefaultSerializer && defaultUsed) {
            Assert.notNull(defaultSerializer, "default serializer null and not all serializers initialized");
        }

        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> select(int spaceId, int indexId, Pageable pageable) {
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.select(spaceId, indexId, Collections.emptyList(), pageable.getOffset(), pageable.getPageSize(), Iterator.ALL.getValue());
        return listOfTuples.stream()
                .map(it -> (V) valueSerializer.deserialize(it))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> select(int spaceId, int indexId, K key, Pageable pageable, Iterator iterator) {
        final Object serializedKey = keySerializer.serialize(key);
        List<?> keyTuple = coerceToTuple(serializedKey);

        final List<List<?>> tuples = (List<List<?>>) syncOps.select(spaceId, indexId, keyTuple, pageable.getOffset(), pageable.getPageSize(), iterator.getValue());

        return tuples.stream()
                .map(it -> (V) valueSerializer.deserialize(it))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V insert(int spaceId, V value) {
        final List tuple = coerceToTuple(valueSerializer.serialize(value));

        final List result = syncOps.insert(spaceId, tuple);

        // TODO: or it's a list of tuple?
        return (V) valueSerializer.deserialize(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V replace(int spaceId, V value) {
        final Object serializedValue = valueSerializer.serialize(value);
        List valueTuple = coerceToTuple(serializedValue);

        final List result = syncOps.replace(spaceId, valueTuple);

        return ((V) valueSerializer.deserialize(result));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public V update(int spaceId, int indexId, K key, List<Operation> operation) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = coerceToTuple(serializedKey);

        final List<?>[] listOfOperations = operation.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.update(spaceId, keyTuple, (Object[]) listOfOperations);
        final List<?> tuple = listOfTuples.get(0);

        return ((V) valueSerializer.deserialize(tuple));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert(int spaceId, K key, V value, List<Operation> operations) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = coerceToTuple(serializedKey);

        final Object serializedValue = valueSerializer.serialize(value);
        List valueTuple = coerceToTuple(serializedValue);

        final List[] listOfOperations = operations.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        syncOps.upsert(spaceId, keyTuple, valueTuple, listOfOperations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V delete(int spaceId, K key) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = coerceToTuple(serializedKey);

        final List tuple = syncOps.delete(spaceId, keyTuple);
        return (V) valueSerializer.deserialize(tuple);
    }

    private List coerceToTuple(Object value) {
        if(value instanceof List) {
            return ((List) value);
        }

        return Collections.singletonList(value);
    }

    public void setSyncOps(TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps) {
        this.syncOps = syncOps;
    }

    public void setEnableDefaultSerializer(boolean enableDefaultSerializer) {
        this.enableDefaultSerializer = enableDefaultSerializer;
    }

    public void setDefaultSerializer(TarantoolSerializer<?> defaultSerializer) {
        this.defaultSerializer = defaultSerializer;
    }

    public void setKeySerializer(TarantoolSerializer<?> keySerializer) {
        this.keySerializer = keySerializer;
    }

    public void setValueSerializer(TarantoolSerializer<?> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
}
