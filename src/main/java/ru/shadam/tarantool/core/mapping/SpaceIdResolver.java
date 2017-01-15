package ru.shadam.tarantool.core.mapping;

/**
 * @author sala
 */
public interface SpaceIdResolver {
    /**
     * Determine the {@literal keySpace} to use for a given type.
     *
     * @param type must not be {@literal null}.
     * @return
     */
    Integer resolveSpaceId(Class<?> type);
}
