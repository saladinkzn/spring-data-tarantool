package ru.shadam.tarantool.core;

/**
 * @author sala
 */
public enum TarantoolIndexOperation {
    SELECT("select"),
    UPDATE("update"),
    COUNT("count");

    TarantoolIndexOperation(String operationName) {
        this.operationName = operationName;
    }

    private final String operationName;

    public String getOperationName() {
        return operationName;
    }
}
