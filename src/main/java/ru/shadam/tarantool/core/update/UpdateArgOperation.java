package ru.shadam.tarantool.core.update;

import java.util.Arrays;
import java.util.List;

/**
 * @author sala
 */
public class UpdateArgOperation extends Operation<UpdateArgOperator> {
    private final Object argument;

    public UpdateArgOperation(UpdateArgOperator operator, int fieldNumber, Object argument) {
        super(operator, fieldNumber);
        this.argument = argument;
    }

    public Object getArgument() {
        return argument;
    }

    @Override
    public List<?> toTuple() {
        return Arrays.asList(operator.getValue(), fieldNumber, argument);
    }
}
