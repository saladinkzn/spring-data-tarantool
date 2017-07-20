package ru.shadam.tarantool.repository.entity;

import ru.shadam.tarantool.annotation.Index;
import ru.shadam.tarantool.annotation.Tuple;

/**
 * @author sala
 */
public class User {
    @Tuple(index = 0)
    public long id;
    @Tuple(index = 1)
    @Index("name_index")
    public String name;

    public User() {
    }
}
