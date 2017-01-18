package ru.shadam.tarantool.core;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.keyvalue.core.KeyValueCallback;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.convert.MappingTarantoolConverter;
import ru.shadam.tarantool.core.convert.TarantoolData;
import ru.shadam.tarantool.core.convert.Tuple;
import ru.shadam.tarantool.core.mapping.TarantoolMappingContext;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentProperty;
import ru.shadam.tarantool.repository.query.TarantoolQuery;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author sala
 */
public class TarantoolKeyValueTemplate implements KeyValueOperations {
    private TarantoolClientOps<Integer, Object, Object, List> ops;
    private MappingTarantoolConverter converter;
    private final TarantoolMappingContext mappingContext;
    private final IdentifierGenerator identifierGenerator;


    public TarantoolKeyValueTemplate(TarantoolClientOps<Integer, Object, Object, List> ops, MappingTarantoolConverter converter, TarantoolMappingContext mappingContext) {
        this.ops = ops;
        this.converter = converter;
        this.mappingContext = mappingContext;
        this.identifierGenerator = DefaultIdentifierGenerator.INSTANCE;

    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#insert(java.lang.Object)
     */
    @Override
    public <T> T insert(T objectToInsert) {

        PersistentEntity<?, ?> entity = this.mappingContext.getPersistentEntity(ClassUtils.getUserClass(objectToInsert));

        GeneratingIdAccessor generatingIdAccessor = new GeneratingIdAccessor(entity.getPropertyAccessor(objectToInsert),
                entity.getIdProperty(), identifierGenerator);
        Object id = generatingIdAccessor.getOrGenerateIdentifier();

        insert((Serializable) id, objectToInsert);
        return objectToInsert;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#insert(java.io.Serializable, java.lang.Object)
     */
    @Override
    public void insert(final Serializable id, final Object objectToInsert) {

        Assert.notNull(id, "Id for object to be inserted must not be null!");
        Assert.notNull(objectToInsert, "Object to be inserted must not be null!");

        final int spaceId = resolveSpaceId(objectToInsert.getClass());

        final TarantoolData rdo = objectToInsert instanceof TarantoolData ? (TarantoolData) objectToInsert : new TarantoolData();
        if (!(objectToInsert instanceof TarantoolData)) {
            converter.write(objectToInsert, rdo);
        }

        ops.insert(spaceId, rdo.getTuple().getRaw());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#update(java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void update(Object objectToUpdate) {

        PersistentEntity<?, ? extends PersistentProperty> entity = this.mappingContext.getPersistentEntity(ClassUtils
                .getUserClass(objectToUpdate));

        if (!entity.hasIdProperty()) {
            throw new InvalidDataAccessApiUsageException(String.format("Cannot determine id for type %s",
                    ClassUtils.getUserClass(objectToUpdate)));
        }

        update((Serializable) entity.getIdentifierAccessor(objectToUpdate).getIdentifier(), objectToUpdate);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#update(java.io.Serializable, java.lang.Object)
     */
    @Override
    public void update(final Serializable id, final Object objectToUpdate) {

        Assert.notNull(id, "Id for object to be inserted must not be null!");
        Assert.notNull(objectToUpdate, "Object to be updated must not be null!");

        final int spaceId = resolveSpaceId(objectToUpdate.getClass());

        final TarantoolData rdo = objectToUpdate instanceof TarantoolData ? (TarantoolData) objectToUpdate : new TarantoolData();
        if (!(objectToUpdate instanceof TarantoolData)) {
            converter.write(objectToUpdate, rdo);
        }

        // todo : check if id type is supported.
        // convert to any of supported type
        if (rdo.getId() == null) {

            rdo.setId(converter.getConversionService().convert(id, Long.class));

            if (!(objectToUpdate instanceof TarantoolData)) {
                TarantoolPersistentProperty idProperty = converter.getMappingContext().getPersistentEntity(objectToUpdate.getClass())
                        .getIdProperty();
                converter.getMappingContext().getPersistentEntity(objectToUpdate.getClass()).getPropertyAccessor(objectToUpdate)
                        .setProperty(idProperty, id);
            }
        }

        ops.replace(spaceId, rdo.getTuple().getRaw());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#findAllOf(java.lang.Class)
     */
    @Override
    public <T> Iterable<T> findAll(final Class<T> type) {
        Assert.notNull(type, "Type to fetch must not be null!");

        final int spaceId = resolveSpaceId(type);
        final int spaceIndexId = Optional.ofNullable(resolveEntity(type).getIdProperty().getSpaceIndexId()).orElse(0);

        final List listOfTuples = ops.select(spaceId, spaceIndexId, Collections.emptyList(), 0, Integer.MAX_VALUE, Iterator.ALL.getValue());
        final List<List<Object>> tuples = (List<List<Object>>) listOfTuples;

        return tuples.stream().map(
                tuple -> {
                    TarantoolData data = new TarantoolData(new Tuple(tuple));
                    data.setSpaceId(spaceId);
                    // data.setId(id);

                    return converter.read(type, data);
                }
        ).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#findById(java.io.Serializable, java.lang.Class)
     */
    @Override
    public <T> T findById(final Serializable id, final Class<T> type) {
        Assert.notNull(id, "Id for object to be inserted must not be null!");
        Assert.notNull(type, "Type to fetch must not be null!");

        final int spaceId = resolveSpaceId(type);
        final int spaceIndexId = Optional.ofNullable(resolveEntity(type).getIdProperty().getSpaceIndexId()).orElse(0);

        final List listOfTuple = ops.select(spaceId, spaceIndexId, Collections.singletonList(id), 0, 1, Iterator.EQ.getValue());

        final List<Object> tuple = (List<Object>) listOfTuple.get(0);

        TarantoolData data = new TarantoolData(new Tuple(tuple));
        data.setSpaceId(spaceId);
        data.setId(id);

        return converter.read(type, data);
    }

    @Override
    public <T> T execute(KeyValueCallback<T> action) {
        throw new UnsupportedOperationException("");
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#delete(java.lang.Class)
     */
    @Override
    public void delete(final Class<?> type) {
        final Iterable<?> result = findAll(type);

        for (final Object value : result) {
            delete(value);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#delete(java.lang.Object)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> T delete(T objectToDelete) {

        Class<T> type = (Class<T>) ClassUtils.getUserClass(objectToDelete);
        PersistentEntity<?, ? extends PersistentProperty> entity = this.mappingContext.getPersistentEntity(type);

        return delete((Serializable) entity.getIdentifierAccessor(objectToDelete).getIdentifier(), type);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#delete(java.io.Serializable, java.lang.Class)
     */
    @Override
    public <T> T delete(final Serializable id, final Class<T> type) {
        Assert.notNull(id, "Id for object to be deleted must not be null!");
        Assert.notNull(type, "Type to delete must not be null!");

        final int spaceId = resolveSpaceId(type);

        final List listOfTuple = ops.delete(spaceId, Collections.singletonList(id));

        final List<Object> tuple = (List<Object>) listOfTuple.get(0);

        TarantoolData data = new TarantoolData(new Tuple(tuple));
        data.setSpaceId(spaceId);
        data.setId(id);

        return converter.read(type, data);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#count(java.lang.Class)
     */
    @Override
    public long count(Class<?> type) {
        return StreamSupport.stream(findAll(type).spliterator(), false).count();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#find(org.springframework.data.keyvalue.core.query.KeyValueQuery, java.lang.Class)
     */
    @Override
    public <T> Iterable<T> find(final KeyValueQuery<?> query, final Class<T> type) {
        Assert.notNull(query);
        Assert.notNull(type);

        int spaceId = resolveSpaceId(type);
        //
        TarantoolQuery tarantoolQuery = (TarantoolQuery) query.getCritieria();

        TarantoolPersistentEntity<?> entity = this.mappingContext.getPersistentEntity(type);
        TarantoolPersistentProperty persistentProperty = entity.getPersistentProperty(tarantoolQuery.getKey());
        Integer spaceIndexId = persistentProperty.getSpaceIndexId();
        if(spaceIndexId == null) {
            throw new IllegalArgumentException("Cannot query for property without index");
        }
        // TODO: add support for composite index query
        int offset = query.getOffset() == -1 ? 0 : query.getOffset();
        int rows = query.getRows() == -1 ? Integer.MAX_VALUE : query.getRows();

        List listOfTuples = ops.select(spaceId, spaceIndexId, Collections.singletonList(tarantoolQuery.getValue()), offset, rows, Iterator.EQ.getValue());
        List<List<Object>> tuples = (List<List<Object>>) listOfTuples;

        return tuples.stream()
                .map(tuple -> {
                    TarantoolData data = new TarantoolData(new Tuple(tuple));
                    data.setSpaceId(spaceId);
                    // data.setId(id);

                    return converter.read(type, data);
                }).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#findAllOf(org.springframework.data.domain.Sort, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <T> Iterable<T> findAll(Sort sort, Class<T> type) {
        return find(new KeyValueQuery(sort), type);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#findInRange(int, int, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <T> Iterable<T> findInRange(int offset, int rows, Class<T> type) {
        return find(new KeyValueQuery().skip(offset).limit(rows), type);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#findInRange(int, int, org.springframework.data.domain.Sort, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <T> Iterable<T> findInRange(int offset, int rows, Sort sort, Class<T> type) {
        return find(new KeyValueQuery(sort).skip(offset).limit(rows), type);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.KeyValueOperations#count(org.springframework.data.keyvalue.core.query.KeyValueQuery, java.lang.Class)
     */
    @Override
    public long count(final KeyValueQuery<?> query, final Class<?> type) {
        return StreamSupport.stream(find(query, type).spliterator(), false).count();
    }


    @Override
    public MappingContext<?, ?> getMappingContext() {
        return mappingContext;
    }

    private int resolveSpaceId(Class<?> type) {
        return this.mappingContext.getPersistentEntity(type).getSpaceId();
    }

    private TarantoolPersistentEntity<?> resolveEntity(Class<?> type) {
        return this.mappingContext.getPersistentEntity(type);
    }

    @Override
    public void destroy() throws Exception {

    }
}
