package ru.shadam.tarantool.repository.support;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import ru.shadam.tarantool.repository.query.TarantoolQueryCreator;

/**
 * @author sala
 */
public class TarantoolRepositoryFactory extends KeyValueRepositoryFactory {
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
    }
}
