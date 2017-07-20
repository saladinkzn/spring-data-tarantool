package ru.shadam.tarantool.core;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sala
 */
public abstract class TarantoolPageUtils {
    public static Map<String, Object> createOptions(int pageSize, int offset) {
        return createOptions(pageSize, offset, null);
    }

    /**
     * build options map for tarantool paging request
     * @param pageSize
     * @param offset
     * @param iterator tarantool iterator
     * @return map to push into tarantool
     */
    public static Map<String, Object> createOptions(int pageSize, int offset, Iterator iterator) {
        Map<String, Object> options = new HashMap<>();
        if (pageSize != Integer.MAX_VALUE) {
            options.put("limit", pageSize);
        }
        if (offset != 0) {
            options.put("offset", offset);
        }
        if (iterator != null) {
            options.put("iterator", iterator.getValue());
        }
        return options;
    }

    /**
     * build options map for tarantool paging request
     * @param pageable spring-data pageable (page size and offset provider)
     * @param iterator tarantool iterator
     * @return map to push into tarantool
     */
    public static Map<String, Object> createOptions(Pageable pageable, Iterator iterator) {
        Map<String, Object> options = new HashMap<>();
        if (pageable.getPageSize() != Integer.MAX_VALUE) {
            options.put("limit", pageable.getPageSize());
        }
        if (pageable.getOffset() != 0) {
            options.put("offset", pageable.getOffset());
        }
        if (iterator != null) {
            options.put("iterator", iterator.getValue());
        }
        return options;
    }
}
