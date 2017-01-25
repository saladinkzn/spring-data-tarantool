package ru.shadam.tarantool.repository.core;

import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.repository.core.support.PersistentEntityInformation;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;

import java.io.Serializable;

/**
 * @author sala
 */
public class MappingTarantoolEntityInformation<T> extends PersistentEntityInformation<T, Serializable> {

    public MappingTarantoolEntityInformation(TarantoolPersistentEntity<T> entity) {
        super(entity);

        if(!entity.hasIdProperty()) {

            throw new MappingException(
                    String.format("Entity %s requires to have an explicit id field. Did you forget to provide one using @Id?",
                            entity.getName())
            );
        }
    }
}
