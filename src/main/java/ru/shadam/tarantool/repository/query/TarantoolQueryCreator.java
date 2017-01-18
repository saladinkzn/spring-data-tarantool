package ru.shadam.tarantool.repository.query;

import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;

/**
 * @author sala
 */
public class TarantoolQueryCreator extends AbstractQueryCreator<KeyValueQuery<TarantoolQuery>, TarantoolQuery> {

    public TarantoolQueryCreator(PartTree tree, ParameterAccessor parameters) {
        super(tree, parameters);
    }

    @Override
    protected TarantoolQuery create(Part part, Iterator<Object> iterator) {
        switch (part.getType()) {
            case SIMPLE_PROPERTY:
                return new TarantoolQuery(part.getProperty().toDotPath(), iterator.next());
            default:
                throw new IllegalArgumentException(part.getType() + "is not supported for redis query derivation");
        }
    }

    @Override
    protected TarantoolQuery and(Part part, TarantoolQuery base, Iterator<Object> iterator) {
        throw new UnsupportedOperationException("and is not supported");
    }

    @Override
    protected TarantoolQuery or(TarantoolQuery base, TarantoolQuery criteria) {
        throw new UnsupportedOperationException("or is not supported");
    }

    @Override
    protected KeyValueQuery<TarantoolQuery> complete(TarantoolQuery criteria, Sort sort) {
        final KeyValueQuery<TarantoolQuery> tarantoolQueryKeyValueQuery = new KeyValueQuery<>(criteria);
        tarantoolQueryKeyValueQuery.setSort(sort);
        return tarantoolQueryKeyValueQuery;
    }
}
