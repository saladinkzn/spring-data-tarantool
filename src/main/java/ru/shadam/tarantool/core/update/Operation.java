package ru.shadam.tarantool.core.update;

import java.util.List;

/**
 * @author sala
 */
public abstract class Operation<TOperator extends Operator> {
    protected final TOperator operator;
    protected final int fieldNumber;

    public Operation(TOperator operator, int fieldNumber) {
        this.operator = operator;
        this.fieldNumber = fieldNumber;
    }

    public TOperator getOperator() {
        return operator;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public abstract List toTuple();

}
