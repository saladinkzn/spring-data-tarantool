package ru.shadam.tarantool.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author sala
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD, TYPE })
public @interface SpaceId {

    int value();
}
