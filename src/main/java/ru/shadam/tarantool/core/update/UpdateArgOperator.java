package ru.shadam.tarantool.core.update;

/**
 * @author sala
 */
public enum UpdateArgOperator implements Operator {
    INSERT("!"),
    ASSIGN("=");

    private final String value;

    UpdateArgOperator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
