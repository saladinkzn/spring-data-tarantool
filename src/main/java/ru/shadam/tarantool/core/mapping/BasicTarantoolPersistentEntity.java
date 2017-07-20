package ru.shadam.tarantool.core.mapping;

import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

/**
 * @author sala
 */
public class BasicTarantoolPersistentEntity<T>  extends BasicPersistentEntity<T, TarantoolPersistentProperty>
        implements TarantoolPersistentEntity<T> {

    private final String spaceName;

    /**
     * @param information              must not be {@literal null}.
     * @param fallbackSpaceIdResolver
     */
    public BasicTarantoolPersistentEntity(TypeInformation<T> information, KeySpaceResolver fallbackSpaceIdResolver) {
        super(information);

        this.spaceName = detectKeySpace(information.getType(), fallbackSpaceIdResolver);
    }

    private static String detectKeySpace(Class<?> type, KeySpaceResolver fallback) {
        String keySpace = AnnotationBasedSpaceIdResolver.INSTANCE.resolveKeySpace(type);

        if (keySpace != null) {
            return keySpace;
        }

        return (fallback == null ? ClassNameKeySpaceResolver.INSTANCE : fallback).resolveKeySpace(type);
    }

    @Override
    public String getSpaceName() {
        return spaceName;
    }
}
