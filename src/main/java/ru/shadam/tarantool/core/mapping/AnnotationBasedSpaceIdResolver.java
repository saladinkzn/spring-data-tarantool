package ru.shadam.tarantool.core.mapping;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import ru.shadam.tarantool.annotation.SpaceId;

/**
 * @author sala
 */
public enum AnnotationBasedSpaceIdResolver implements SpaceIdResolver {
    INSTANCE;

    @Override
    public Integer resolveSpaceId(Class<?> type) {
        Assert.notNull(type, "Type for keyspace for null!");

        Class<?> userClass = ClassUtils.getUserClass(type);
        return getKeySpace(userClass);
    }

    private Integer getKeySpace(Class<?> type) {
        SpaceId keyspace = AnnotatedElementUtils.findMergedAnnotation(type, SpaceId.class);

        if (keyspace != null) {
            return (Integer) AnnotationUtils.getValue(keyspace);
        }

        return null;
    }
}
