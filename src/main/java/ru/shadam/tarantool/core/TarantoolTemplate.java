package ru.shadam.tarantool.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.update.Operation;
import ru.shadam.tarantool.serializer.JdkTarantoolSerializer;
import ru.shadam.tarantool.serializer.PlainTarantoolSerializer;
import ru.shadam.tarantool.serializer.TarantoolSerializer;

import java.util.*;
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

    @Override
    public List<V> select(String space, Pageable pageable) {
        Map<String, Object> options = TarantoolPageUtils.createOptions(pageable, Iterator.ALL);
        List<List<?>> listOfTuples = (List<List<?>>) syncOps.call(
                createSpaceFunction(space, TarantoolSpaceOperation.SELECT), Collections.emptyList(), options
        );

        // Tarantool returns list with single empty tuple if nothing was found
        if (listOfTuples.size() == 1 && listOfTuples.get(0).isEmpty()) {
            return Collections.emptyList();
        }

        return listOfTuples.stream()
            .map(it -> (V) valueSerializer.deserialize(extractValue(it)))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> select(String space, String index, Pageable pageable) {
        Map<String, Object> options = TarantoolPageUtils.createOptions(pageable, Iterator.ALL);
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.call(
                createSpaceIndexFunction(space, index, TarantoolIndexOperation.SELECT),
                Arrays.asList(Collections.emptyList(), options)
        );
        return listOfTuples.stream()
                .map(it -> (V) valueSerializer.deserialize(extractValue(it)))
                .collect(Collectors.toList());
    }

    @Override
    public List<V> select(String space, K key, Pageable pageable, Iterator iterator) {
        List<?> keyTuple = keySerializer.serialize(key);
        Map<String, Object> options = TarantoolPageUtils.createOptions(pageable, iterator);

        List<List<?>> tuples = (List<List<?>>) syncOps.call(
                createSpaceFunction(space, TarantoolSpaceOperation.SELECT), keyTuple, options
        );

        return tuples.stream()
                .map(it -> (V) valueSerializer.deserialize(extractValue(it)))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> select(String space, String index, K key, Pageable pageable, Iterator iterator) {
        List<?> keyTuple = keySerializer.serialize(key);
        Map<String, Object> options = TarantoolPageUtils.createOptions(pageable, iterator);

        final List<List<?>> tuples = (List<List<?>>) syncOps.call(
                createSpaceIndexFunction(space, index, TarantoolIndexOperation.SELECT),
                Arrays.asList(keyTuple, options)
        );

        return tuples.stream()
                .map(it -> (V) valueSerializer.deserialize(extractValue(it)))
                .collect(Collectors.toList());
    }

//    @Override
    public List<K> selectKeys(String spaceId, String indexId) {
        Map<String, Object> options = TarantoolPageUtils.createOptions(new PageRequest(0, Integer.MAX_VALUE), Iterator.ALL);
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.call(
                createSpaceIndexFunction(spaceId, indexId, TarantoolIndexOperation.SELECT),
                Arrays.asList(Collections.emptyList(), options));
        return listOfTuples.stream()
                .map(it -> (K) keySerializer.deserialize(extractKey(it)))
                .collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V insert(String spaceId, K key, V value) {
        List tuple = convertToTuple(keySerializer.serialize(key), valueSerializer.serialize(value));

        final List<List<?>> tuples = ((List<List<?>>) syncOps.call(createSpaceFunction(spaceId, TarantoolSpaceOperation.INSERT), tuple));

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
    public V replace(String spaceId, K key, V value) {
        List valueTuple = valueSerializer.serialize(value);

        final List result = syncOps.call(createSpaceFunction(spaceId, TarantoolSpaceOperation.REPLACE), valueTuple);

        return ((V) valueSerializer.deserialize(extractValue(result)));
    }

    @Override
    public V update(String space, K key, List<Operation> operations) {
        List serializedKey = keySerializer.serialize(key);

        List[] listOperations = operations.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        List<List<?>> listOfTuples = (List<List<?>>) syncOps.call(
                createSpaceFunction(space, TarantoolSpaceOperation.UPDATE),
                serializedKey, 
                listOperations
        );
        final List<?> tuple = listOfTuples.get(0);

        return (V) valueSerializer.deserialize(extractValue(tuple));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public V update(String spaceId, String indexId, K key, List<Operation> operation) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = (List) serializedKey;

        final List<?>[] listOfOperations = operation.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        final List<List<?>> listOfTuples = (List<List<?>>) syncOps.call(
                createSpaceIndexFunction(spaceId, indexId, TarantoolIndexOperation.UPDATE),
                Arrays.asList(keyTuple, listOfOperations));
        final List<?> tuple = listOfTuples.get(0);

        return ((V) valueSerializer.deserialize(extractValue(tuple)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert(String spaceId, K key, V value, List<Operation> operations) {
        final Object serializedKey = keySerializer.serialize(key);
        List keyTuple = (List) serializedKey;

        final Object serializedValue = valueSerializer.serialize(value);
        List valueTuple = (List) serializedValue;

        List tuple = convertToTuple(keyTuple, valueTuple);

        final List[] listOfOperations = operations.stream()
                .map(Operation::toTuple)
                .toArray(List[]::new);
        syncOps.call(
                createSpaceFunction(spaceId, TarantoolSpaceOperation.UPSERT),
                tuple, (Object[]) listOfOperations
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V delete(String spaceId, K key) {
        final List serializedKey = keySerializer.serialize(key);

        final List tuple = syncOps.call(
                createSpaceFunction(spaceId,  TarantoolSpaceOperation.DELETE), serializedKey);
        return (V) valueSerializer.deserialize(extractValue(tuple));
    }

    @Override
    public void deleteAll(String space) {
        syncOps.eval(createSpaceFunctionCall(space, TarantoolSpaceOperation.TRUNCATE));
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

    private static String createSpaceFunction(String space, TarantoolSpaceOperation operation) {
        return "box.space." + space + ":" + operation.getOperationName();
    }

    private static String createSpaceFunctionCall(String space, TarantoolSpaceOperation operation) {
        return createSpaceFunction(space, operation) + "()";
    }

    private static String createSpaceIndexFunction(String space, String index, TarantoolIndexOperation operation) {
        return "box.space." + space + ".index." + index + ":" + operation.getOperationName();
    }

}
