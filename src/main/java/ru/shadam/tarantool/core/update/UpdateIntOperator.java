package ru.shadam.tarantool.core.update;

/**
 * @author sala
 */
public enum UpdateIntOperator implements Operator {
    ADDITION("+"),
    SUBSTRACTION("-"),
    BITWISE_AND("&"),
    BITWISE_XOR("^"),
    BITWISE_OR("|"),
    DELETE("#")
    ;

    private final String value;

    UpdateIntOperator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
