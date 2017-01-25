package ru.shadam.tarantool.core.update;

import java.util.Arrays;
import java.util.List;

/**
 * @author sala
 */
public class UpdateIntOperation extends Operation<UpdateIntOperator> {
    private final int argument;

    public UpdateIntOperation(UpdateIntOperator operator, int fieldNumber, int argument) {
        super(operator, fieldNumber);
        this.argument = argument;
    }

    public int getArgument() {
        return argument;
    }

    @Override
    public List<?> toTuple() {
        return Arrays.asList(operator.getValue(), fieldNumber, argument);
    }
}
