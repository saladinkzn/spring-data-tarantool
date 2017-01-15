package ru.shadam.tarantool.core.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

/**
 * @author sala
 */
public class BasicTarantoolPersistentEntity<T>  extends BasicPersistentEntity<T, TarantoolPersistentProperty>
        implements TarantoolPersistentEntity<T> {

    private final Integer spaceId;

    /**
     * @param information              must not be {@literal null}.
     * @param fallbackSpaceIdResolver
     */
    public BasicTarantoolPersistentEntity(TypeInformation<T> information, SpaceIdResolver fallbackSpaceIdResolver) {
        super(information);

        this.spaceId = detectKeySpace(information.getType(), fallbackSpaceIdResolver);
    }

    private static Integer detectKeySpace(Class<?> type, SpaceIdResolver fallback) {
        Integer keySpace = AnnotationBasedSpaceIdResolver.INSTANCE.resolveSpaceId(type);

        if (keySpace != null) {
            return keySpace;
        }

        return (fallback == null ? NullSpaceIdResolver.INSTANCE : fallback).resolveSpaceId(type);
    }

    @Override
    public Integer getSpaceId() {
        return spaceId;
    }
}
