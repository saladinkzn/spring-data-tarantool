package ru.shadam.tarantool.core.mapping;

/**
 * @author sala
 */
public enum NullSpaceIdResolver implements  SpaceIdResolver {
    INSTANCE;

    @Override
    public Integer resolveSpaceId(Class<?> type) {
        return null;
    }
}
