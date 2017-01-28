package ru.shadam.tarantool.core.mapping;

import org.springframework.data.mapping.PersistentProperty;

import java.util.OptionalInt;

/**
 * @author sala
 */
public interface TarantoolPersistentProperty extends PersistentProperty<TarantoolPersistentProperty> {
    OptionalInt getTupleIndex();

    OptionalInt getSpaceIndexId();
}
