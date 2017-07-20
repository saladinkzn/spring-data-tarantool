package ru.shadam.tarantool.core.mapping;

import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author sala
 */
public enum ClassNameKeySpaceResolver implements KeySpaceResolver {
    INSTANCE;

    @Override
    public String resolveKeySpace(Class<?> type) {
        Assert.notNull(type);
        // TODO: testme
        return ClassUtils.getUserClass(type).getSimpleName().toLowerCase();
    }
}
