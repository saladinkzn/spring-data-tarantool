package ru.shadam.tarantool.annotation;

import java.lang.annotation.*;

/**
 * @author sala
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Index {
    int id();
}
