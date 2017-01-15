package ru.shadam.tarantool.core.mapping;

import org.springframework.data.mapping.model.MutablePersistentEntity;

/**
 * @author sala
 */
public interface TarantoolPersistentEntity<T>  extends MutablePersistentEntity<T, TarantoolPersistentProperty> {
    /**
     * @return may be null
     */
    Integer getSpaceId();
}
