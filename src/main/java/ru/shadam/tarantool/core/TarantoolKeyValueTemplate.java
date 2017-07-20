package ru.shadam.tarantool.core;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueCallback;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import ru.shadam.tarantool.core.convert.TarantoolConverter;
import ru.shadam.tarantool.core.convert.TarantoolData;
import ru.shadam.tarantool.core.mapping.TarantoolMappingContext;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentProperty;
import ru.shadam.tarantool.repository.query.TarantoolQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author sala
 */
public class TarantoolKeyValueTemplate implements KeyValueOperations {
    // for exception translation
    private final TarantoolOps tarantoolOps;
    private final TarantoolMappingContext mappingContext;
    private final TarantoolConverter converter;
//    private final QueryEngine<? extends KeyValueAdapter, ?, ?> engine;

    public TarantoolKeyValueTemplate(TarantoolOps tarantoolOps, TarantoolMappingContext mappingContext, TarantoolConverter converter) {
        this.tarantoolOps = tarantoolOps;
        this.mappingContext = mappingContext;
        this.converter = converter;
    }

    @Override
    public <T> T insert(T objectToInsert) {
        Assert.notNull(objectToInsert, "Object to be inserted must not be null!");

        Class<?> objectClass = objectToInsert.getClass();
        String keyspace = resolveKeySpace(objectClass);

        TarantoolData tdo = maybeConvert(objectToInsert);

        converter.removePrimaryKey(objectToInsert, tdo);

        TarantoolData inserted = tarantoolOps.autoIncrement(keyspace, tdo);

        T converted = (T) converter.read(objectClass, inserted);

        TarantoolPersistentProperty idProperty = converter.getMappingContext().getPersistentEntity(objectClass)
                .getIdProperty();

        Object id = converter.getMappingContext().getPersistentEntity(objectClass).getPropertyAccessor(converted)
                .getProperty(idProperty);

        tdo.setId(id);
        
        if (!(objectToInsert instanceof TarantoolData)) {
            converter.getMappingContext().getPersistentEntity(objectClass).getPropertyAccessor(objectToInsert)
                    .setProperty(idProperty, id);
        }

        return converted;
    }

    @Override
    public void insert(Serializable id, Object objectToInsert) {
        Assert.notNull(id, "Id for object to be inserted must not be null!");
        Assert.notNull(objectToInsert, "Object to be inserted must not be null!");

        Class<?> objectClass = objectToInsert.getClass();
        String keyspace = resolveKeySpace(objectClass);

        TarantoolData tdo = maybeConvert(objectToInsert);

        if (tdo.getId() == null) {
            tdo.setId(id);

            if (!(objectToInsert instanceof TarantoolData)) {
                TarantoolPersistentProperty idProperty = converter.getMappingContext().getPersistentEntity(objectClass)
                        .getIdProperty();
                converter.getMappingContext().getPersistentEntity(objectClass).getPropertyAccessor(objectToInsert)
                        .setProperty(idProperty, id);
            }
        }

        tarantoolOps.replace(keyspace, tdo);
    }

    /*
         * (non-Javadoc)
         * @see org.springframework.data.keyvalue.core.KeyValueOperations#findAllOf(java.lang.Class)
         */

    @Override
    public <T> Iterable<T> findAll(final Class<T> type) {

        Assert.notNull(type, "Type to fetch must not be null!");

        String keyspace = resolveKeySpace(type);

        List<TarantoolData> data = tarantoolOps.getAllOf(keyspace);

        List<T> result = new ArrayList<>();
        for (TarantoolData value : data) {
            result.add(converter.read(type, value));
        }

        return result;
    }
    @Override
    public <T> Iterable<T> findAll(Sort sort, Class<T> type) {
        return find(new KeyValueQuery(sort), type);
    }

    @Override
    public <T> T findById(Serializable id, Class<T> type) {

        Assert.notNull(id, "Id for object to be inserted must not be null!");
        Assert.notNull(type, "Type to fetch must not be null!");

        String keyspace = resolveKeySpace(type);

        TarantoolData data = tarantoolOps.get(keyspace, id);

        return converter.read(type, data);
    }

    @Override
    public <T> T execute(KeyValueCallback<T> action) {
        // TODO: use execute as it's required by api
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Iterable<T> find(KeyValueQuery<?> query, Class<T> type) {
        Assert.notNull(query);
        Assert.notNull(type);

        String keyspace = resolveKeySpace(type);

        TarantoolQuery tarantoolQuery = (TarantoolQuery) query.getCritieria();

        TarantoolPersistentEntity<?> entity = this.mappingContext.getPersistentEntity(type);
        TarantoolPersistentProperty persistentProperty = entity.getPersistentProperty(tarantoolQuery.getKey());

        // todo: or switch to spel?
        String spaceIndexName = persistentProperty.getSpaceIndexName()
                .orElseThrow(() -> new IllegalArgumentException("Cannot query for property without index"));

        int offset = query.getOffset() == -1 ? 0 : query.getOffset();
        int rows = query.getRows() == -1 ? Integer.MAX_VALUE : query.getRows();

        List<TarantoolData> tuples = tarantoolOps.getByIndex(keyspace, spaceIndexName, (Serializable) tarantoolQuery.getValue(), offset, rows);

        List<T> result = new ArrayList<>();
        for (final TarantoolData tuple : tuples) {
            result.add(converter.read(type, tuple));
        }
        return result;
    }

    @Override
    public <T> Iterable<T> findInRange(int offset, int rows, Class<T> type) {
        Assert.notNull(type, "Type to fetch must not be null!");

        String keyspace = resolveKeySpace(type);

        List<TarantoolData> data = tarantoolOps.getAllOf(keyspace, offset, rows);

        List<T> result = new ArrayList<>();
        for (TarantoolData value : data) {
            result.add(converter.read(type, value));
        }

        return result;
    }

    @Override
    public <T> Iterable<T> findInRange(int offset, int rows, Sort sort, Class<T> type) {
        if (sort == null) {
            return findInRange(offset, rows, type);
        }

        List<Sort.Order> orders = new ArrayList<>();
        sort.iterator().forEachRemaining(orders::add);

        if (orders.isEmpty()) {
            return findInRange(offset, rows, type);
        }

        if (orders.size() > 1) {
            throw new IllegalArgumentException("cannot order by more than one field");
        }

        Sort.Order onlyOrder = orders.get(0);
        String onlyOrderKey = onlyOrder.getProperty();

        TarantoolPersistentEntity<?> persistentEntity = this.mappingContext.getPersistentEntity(type);
        TarantoolPersistentProperty persistentProperty = persistentEntity.getPersistentProperty(onlyOrderKey);

        String space = resolveKeySpace(type);
        ru.shadam.tarantool.core.Iterator iterator =
                onlyOrder.isAscending()
                        ? ru.shadam.tarantool.core.Iterator.GE
                        : ru.shadam.tarantool.core.Iterator.LE;

        List<TarantoolData> data;
        if (persistentProperty.isIdProperty()) {
            data = tarantoolOps.getAllOf(space, iterator, offset, rows);
        } else {
            Optional<String> indexName = persistentProperty.getSpaceIndexName();
            if(!indexName.isPresent()) {
                throw new IllegalArgumentException("cannot sort without index");
            }

            data = tarantoolOps.getByIndex(space, indexName.get(), iterator, offset, rows);
        }

        List<T> result = new ArrayList<>();

        for (TarantoolData value : data) {
            result.add(converter.read(type, value));
        }

        return result;
    }

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

    @Override
    public void update(Serializable id, Object objectToUpdate) {
        Assert.notNull(id, "Id for object to be inserted must not be null!");
        Assert.notNull(objectToUpdate, "Object to be updated must not be null!");

        final String keyspace = resolveKeySpace(objectToUpdate.getClass());

        TarantoolData tarantoolData = maybeConvert(objectToUpdate);

        tarantoolOps.replace(keyspace, tarantoolData);
    }

    @Override
    public void delete(Class<?> type) {
        Assert.notNull(type, "Type to delete must not be null!");

        final String keyspace = resolveKeySpace(type);

        tarantoolOps.deleteAll(keyspace);
    }

    @Override
    public <T> T delete(T objectToDelete) {

        Class<T> type = (Class<T>) ClassUtils.getUserClass(objectToDelete);
        PersistentEntity<?, ? extends PersistentProperty> entity = this.mappingContext.getPersistentEntity(type);

        return delete((Serializable) entity.getIdentifierAccessor(objectToDelete).getIdentifier(), type);
    }

    @Override
    public <T> T delete(Serializable id, Class<T> type) {

        Assert.notNull(id, "Id for object to be deleted must not be null!");
        Assert.notNull(type, "Type to delete must not be null!");

        final String keyspace = resolveKeySpace(type);

        TarantoolData data = tarantoolOps.delete(keyspace, id);

        return converter.read(type, data);
    }

    @Override
    public long count(Class<?> type) {
        return tarantoolOps.count(resolveKeySpace(type));
    }

    @Override
    public long count(KeyValueQuery<?> query, Class<?> type) {
        String keyspace = resolveKeySpace(type);

        TarantoolQuery tarantoolQuery = (TarantoolQuery) query.getCritieria();

        TarantoolPersistentEntity<?> entity = this.mappingContext.getPersistentEntity(type);
        TarantoolPersistentProperty persistentProperty = entity.getPersistentProperty(tarantoolQuery.getKey());

        // todo: or switch to spel?
        String spaceIndexName = persistentProperty.getSpaceIndexName()
                .orElseThrow(() -> new IllegalArgumentException("Cannot query for property without index"));

        return tarantoolOps.countByIndex(keyspace, spaceIndexName, (Serializable) tarantoolQuery.getValue());
    }

    @Override
    public MappingContext<?, ?> getMappingContext() {
        return mappingContext;
    }

    @Override
    public void destroy() throws Exception {

    }

    private TarantoolData maybeConvert(Object objectToInsert) {
        TarantoolData tdo;
        if (objectToInsert instanceof TarantoolData) {
            tdo = (TarantoolData) objectToInsert;
        } else {
            tdo = new TarantoolData();
            converter.write(objectToInsert, tdo);
        }
        return tdo;
    }

    private String resolveKeySpace(Class<?> type) {
        return this.mappingContext.getPersistentEntity(type).getSpaceName();
    }
}
