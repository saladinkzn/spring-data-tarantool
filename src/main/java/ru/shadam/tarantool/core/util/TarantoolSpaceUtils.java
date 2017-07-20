package ru.shadam.tarantool.core.util;

import ru.shadam.tarantool.core.TarantoolIndexOperation;
import ru.shadam.tarantool.core.TarantoolSpaceOperation;

/**
 * @author sala
 */
public abstract class TarantoolSpaceUtils {
    public TarantoolSpaceUtils() {
        throw new UnsupportedOperationException("unsupported");
    }

    public static String createSpaceFunction(String space, TarantoolSpaceOperation operation) {
        return "box.space." + space + ":" + operation.getOperationName();
    }

    public static String createSpaceIndexFunction(String space, String index, TarantoolIndexOperation operation) {
        return "box.space." + space + ".index." + index + ":" + operation.getOperationName();
    }
}
