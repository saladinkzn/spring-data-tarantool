package ru.shadam.tarantool.core.update;

import java.util.Arrays;
import java.util.List;

/**
 * @author sala
 */
public class UpdateStringOperation extends Operation<UpdateStringOperator> {
    private final int position;
    private final int offset;
    private final String argument;


    public UpdateStringOperation(
            UpdateStringOperator operator, int fieldNumber,
            int position, int offset,
            String argument
    ) {
        super(operator, fieldNumber);
        this.position = position;
        this.offset = offset;
        this.argument = argument;
    }

    public int getPosition() {
        return position;
    }

    public int getOffset() {
        return offset;
    }

    public String getArgument() {
        return argument;
    }

    @Override
    public List<?> toTuple() {
        return Arrays.asList(operator.getValue(), fieldNumber, position, offset, argument);
    }
}
