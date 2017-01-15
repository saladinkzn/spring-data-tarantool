package ru.shadam.tarantool.core.mapping;

import org.springframework.data.mapping.PersistentProperty;

/**
 * @author sala
 */
public interface TarantoolPersistentProperty extends PersistentProperty<TarantoolPersistentProperty> {
    int getTupleIndex();
}
