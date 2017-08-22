package ru.shadam.tarantool.core.convert;

import org.springframework.data.convert.EntityConverter;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentProperty;

/**
 * @author sala
 */
public interface TarantoolConverter
        extends EntityConverter<TarantoolPersistentEntity<?>, TarantoolPersistentProperty, Object, TarantoolData> {

    void removePrimaryKey(Object source, TarantoolData sink);
}
