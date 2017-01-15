package ru.shadam.tarantool.repository.support;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

import java.io.Serializable;

/**
 * @author sala
 */
public class TarantoolRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends KeyValueRepositoryFactoryBean<T, S, ID> {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean#createRepositoryFactory(org.springframework.data.keyvalue.core.KeyValueOperations, java.lang.Class, java.lang.Class)
     */
    @Override
    protected TarantoolRepositoryFactory createRepositoryFactory(
            KeyValueOperations operations,
            Class<? extends AbstractQueryCreator<?, ?>> queryCreator,
            Class<? extends RepositoryQuery> repositoryQueryType
    ) {
        return new TarantoolRepositoryFactory(operations, queryCreator, repositoryQueryType);
    }
}
