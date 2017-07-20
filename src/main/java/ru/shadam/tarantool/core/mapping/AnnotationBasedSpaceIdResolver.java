package ru.shadam.tarantool.core.mapping;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author sala
 */
public enum AnnotationBasedSpaceIdResolver implements KeySpaceResolver {
    INSTANCE;

    @Override
    public String resolveKeySpace(Class<?> type) {
        Assert.notNull(type, "Type for keyspace for null!");

        Class<?> userClass = ClassUtils.getUserClass(type);
        return getKeySpace(userClass);
    }

    private String getKeySpace(Class<?> type) {
        KeySpace keyspace = AnnotatedElementUtils.findMergedAnnotation(type, KeySpace.class);

        if (keyspace != null) {
            return (String) AnnotationUtils.getValue(keyspace);
        }

        return null;
    }
}
