package ru.shadam.tarantool.core;

/**
 * @author sala
 */
public enum Iterator {
    /**
     * Equality comparison. Tuples returned in ascending order by index key.
     */
    EQ(0),
    /**
     * Equality comparison. Tuples return in descending order by index key.
     */
    REQ(1),
    /**
     * Same as GE.
     */
    ALL(2),
    /**
     *  Lesser than (<) comparison. Tuples are returned in descending order by index key.
     */
    LT(3),
    /**
     *  Lesser than or equal to (<=) comparison. Tuples are returned in descending order by index key.
     */
    LE(4),
    /**
     * Greater or equal to (>=) comparison
     */
    GE(5),
    /**
     * Greater than (>) comparison. Tuples are returned in ascending order by index key.
     */
    GT(6);

    private final int value;

    Iterator(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
