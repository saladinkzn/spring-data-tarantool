package ru.shadam.tarantool.repository.support;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;
import ru.shadam.tarantool.repository.core.MappingTarantoolEntityInformation;
import ru.shadam.tarantool.repository.query.TarantoolQueryCreator;

import java.io.Serializable;

/**
 * @author sala
 */
public class TarantoolRepositoryFactory extends KeyValueRepositoryFactory {
    private final KeyValueOperations keyValueOperations;

    /**
     * @param keyValueOperations
     * @see KeyValueRepositoryFactory#KeyValueRepositoryFactory(KeyValueOperations)
     */
    public TarantoolRepositoryFactory(KeyValueOperations keyValueOperations) {
        this(keyValueOperations, TarantoolQueryCreator.class);
    }

    public TarantoolRepositoryFactory(
            KeyValueOperations keyValueOperations,
            Class<? extends AbstractQueryCreator<?, ?>> queryCreator
    ) {
        this(keyValueOperations, queryCreator, KeyValuePartTreeQuery.class);
    }

    public TarantoolRepositoryFactory(
            KeyValueOperations keyValueOperations,
            Class<? extends AbstractQueryCreator<?, ?>> queryCreator,
            Class<? extends RepositoryQuery> repositoryQueryType
    ) {
        super(keyValueOperations, queryCreator, repositoryQueryType);
        this.keyValueOperations = keyValueOperations;
    }

    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        TarantoolPersistentEntity<T> entity = ((TarantoolPersistentEntity<T>) keyValueOperations
                .getMappingContext()
                .getPersistentEntity(domainClass));
        EntityInformation<T, ID> entityInformation = (EntityInformation<T, ID>) new MappingTarantoolEntityInformation<T>(entity);
        return entityInformation;
    }
}
