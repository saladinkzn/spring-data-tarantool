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
public class TarantoolQueryCreator extends AbstractQueryCreator<KeyValueQuery<TarantoolQueryChain>, TarantoolQueryChain> {

    public TarantoolQueryCreator(PartTree tree, ParameterAccessor parameters) {
        super(tree, parameters);
    }

    @Override
    protected TarantoolQueryChain create(Part part, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected TarantoolQueryChain and(Part part, TarantoolQueryChain base, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected TarantoolQueryChain or(TarantoolQueryChain base, TarantoolQueryChain criteria) {
        return null;
    }

    @Override
    protected KeyValueQuery<TarantoolQueryChain> complete(TarantoolQueryChain criteria, Sort sort) {
        return null;
    }
}
