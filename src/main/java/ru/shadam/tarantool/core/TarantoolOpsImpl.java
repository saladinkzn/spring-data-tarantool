package ru.shadam.tarantool.core;

import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.convert.TarantoolData;
import ru.shadam.tarantool.core.convert.Tuple;
import ru.shadam.tarantool.core.util.TarantoolSpaceUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author sala
 */
public class TarantoolOpsImpl implements TarantoolOps {
    private final TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps;

    public TarantoolOpsImpl(TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps) {
        this.syncOps = syncOps;
    }

    @Override
    public boolean contains(String space, Serializable id) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.SELECT);
        List<List<?>> listOfTuples = ((List<List<?>>) syncOps.call(spaceFunction, Collections.singletonList(id)));
        return !listOfTuples.isEmpty();
    }

    @Override
    public long count(String space) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.COUNT);
        List<List<?>> singleton = (List<List<?>>) syncOps.call(spaceFunction, Collections.emptyList());
        return ((Number) singleton.get(0).get(0)).longValue();
    }

    @Override
    public long countByIndex(String space, String index, Serializable id) {
        String indexFunction = TarantoolSpaceUtils.createSpaceIndexFunction(space, index, TarantoolIndexOperation.COUNT);
        List<List<?>> singleton = (List<List<?>>) syncOps.call(indexFunction, Collections.singletonList(id));
        return ((Number) singleton.get(0).get(0)).longValue();
    }

    @Override
    public TarantoolData delete(String space, Serializable id) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.DELETE);
        List<List<Object>> tuple = (List<List<Object>>) syncOps.call(spaceFunction, Collections.singletonList(id));
        return new TarantoolData(new Tuple(tuple.get(0)));
    }

    @Override
    public void deleteAll(String space) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.TRUNCATE);
        syncOps.call(spaceFunction, Collections.emptyList());
    }

    @Override
    public TarantoolData get(String space, Serializable id) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.SELECT);
        List<List<Object>> tuple = (List<List<Object>>) syncOps.call(spaceFunction, Collections.singletonList(id));
        List<Object> value = tuple.get(0);
        return new TarantoolData(new Tuple(value));
    }

    @Override
    public List<TarantoolData> getAllOf(String space) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.SELECT);
        List<List<Object>> tuples = (List<List<Object>>) syncOps.call(spaceFunction, Collections.emptyList());

        List<TarantoolData> result = new ArrayList<>();

        if (checkIfEmpty(tuples)) {
            return result;
        }

        for (List<Object> tuple : tuples) {
            result.add(new TarantoolData(new Tuple(tuple)));
        }
        return result;
    }

    @Override
    public void replace(String space, TarantoolData tuple) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.INSERT);
        syncOps.call(spaceFunction, tuple.getTuple().getRaw());
    }

    @Override
    public TarantoolData autoIncrement(String space, TarantoolData tuple) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.AUTO_INCREMENT);
        List<List<Object>> result = ((List<List<Object>>) syncOps.call(spaceFunction, tuple.getTuple().getRaw()));
        return new TarantoolData(new Tuple(result.get(0)));
    }

    @Override
    public List<TarantoolData> getAllOf(String space, Iterator iterator, int offset, int rows) {
        String spaceFunction = TarantoolSpaceUtils.createSpaceFunction(space, TarantoolSpaceOperation.SELECT);

        Map<String, Object> options = TarantoolPageUtils.createOptions(rows, offset, iterator);

        List<List<Object>> tuples = (List<List<Object>>) syncOps.call(spaceFunction, Collections.emptyList(), options);

        List<TarantoolData> result = new ArrayList<>();

        if (checkIfEmpty(tuples)) {
            return result;
        }

        for (List<Object> tuple : tuples) {
            result.add(new TarantoolData(new Tuple(tuple)));
        }

        return result;
    }

    @Override
    public List<TarantoolData> getByIndex(String space, String index, Serializable id, int offset, int rows) {
        String indexFunction = TarantoolSpaceUtils.createSpaceIndexFunction(space, index, TarantoolIndexOperation.SELECT);

        Map<String, Object> options = TarantoolPageUtils.createOptions(rows, offset);

        List<List<Object>> tuples = (List<List<Object>>) syncOps.call(indexFunction, Collections.singletonList(id), options);

        List<TarantoolData> result = new ArrayList<>();

        if (checkIfEmpty(tuples)) {
            return result;
        }

        for (List<Object> tuple : tuples) {
            result.add(new TarantoolData(new Tuple(tuple)));
        }
        return result;
    }

    @Override
    public List<TarantoolData> getByIndex(String space, String index, Iterator iterator, int offset, int rows) {
        String indexFunction = TarantoolSpaceUtils.createSpaceIndexFunction(space, index, TarantoolIndexOperation.SELECT);

        Map<String, Object> options = TarantoolPageUtils.createOptions(rows, offset, iterator);

        List<List<Object>> tuples = (List<List<Object>>) syncOps.call(indexFunction, Collections.emptyList(), options);

        List<TarantoolData> result = new ArrayList<>();

        if (checkIfEmpty(tuples)) {
            return result;
        }

        for (List<Object> tuple : tuples) {
            result.add(new TarantoolData(new Tuple(tuple)));
        }

        return result;
    }

    private boolean checkIfEmpty(List<List<Object>> tuples) {
        if (tuples.size() == 1 && tuples.get(0).isEmpty()) {
            // box.space.SPACE_NAME:select{} returns list with single empty tuple if nothing was found
            return true;
        }
        return false;
    }
}
