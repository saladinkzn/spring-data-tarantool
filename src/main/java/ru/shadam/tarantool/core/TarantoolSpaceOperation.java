package ru.shadam.tarantool.core;

/**
 * @author sala
 */
public enum TarantoolSpaceOperation {
    SELECT("select"),
    INSERT("replace"),
    REPLACE("replace"),
    UPDATE("update"),
    UPSERT("upsert"),
    DELETE("delete"),
    TRUNCATE("truncate"),
    COUNT("count"),
    AUTO_INCREMENT("auto_increment");

    private final String operationName;

    TarantoolSpaceOperation(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }
}
