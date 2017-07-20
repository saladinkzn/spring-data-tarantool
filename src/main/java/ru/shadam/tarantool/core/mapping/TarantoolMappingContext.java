package ru.shadam.tarantool.core.mapping;

import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author sala
 */
public class TarantoolMappingContext extends AbstractMappingContext<TarantoolPersistentEntity<?>, TarantoolPersistentProperty> {
    private KeySpaceResolver fallbackKeySpaceResolver;

    public void setFallbackKeySpaceResolver(KeySpaceResolver fallbackKeySpaceResolver) {
        this.fallbackKeySpaceResolver = fallbackKeySpaceResolver;
    }

    @Override
    public TarantoolPersistentEntity<?> getPersistentEntity(Class<?> type) {
        return super.getPersistentEntity(type);
    }

    @Override
    public TarantoolPersistentEntity<?> getPersistentEntity(TypeInformation<?> type) {
        return super.getPersistentEntity(type);

    }

    @Override
    protected <T> TarantoolPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new BasicTarantoolPersistentEntity<>(typeInformation, fallbackKeySpaceResolver);
    }

    @Override
    protected TarantoolPersistentPropertyImpl createPersistentProperty(Field field,
                                                                       PropertyDescriptor descriptor,
                                                                       TarantoolPersistentEntity<?> owner,
                                                                       SimpleTypeHolder simpleTypeHolder) {
        return new TarantoolPersistentPropertyImpl(field, descriptor, owner, simpleTypeHolder);
    }
}

