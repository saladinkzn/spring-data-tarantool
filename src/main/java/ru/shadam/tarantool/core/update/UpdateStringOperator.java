package ru.shadam.tarantool.core.update;

/**
 * @author sala
 */
public enum UpdateStringOperator implements Operator {
    SPLICE(":");

    private final String value;

    UpdateStringOperator(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
