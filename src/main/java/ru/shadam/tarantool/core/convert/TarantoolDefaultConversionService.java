package ru.shadam.tarantool.core.convert;

import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Collection;

/**
 * @author sala
 */
public class TarantoolDefaultConversionService extends DefaultConversionService {
    public TarantoolDefaultConversionService() {
        super();

        removeConvertible(Collection.class, Object.class);
        removeConvertible(Object.class, Collection.class);
    }

}
