package ru.shadam.tarantool.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author sala
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Tuple {
    int index();
}
