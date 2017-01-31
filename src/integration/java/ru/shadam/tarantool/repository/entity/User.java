package ru.shadam.tarantool.repository.entity;

import ru.shadam.tarantool.annotation.SpaceId;
import ru.shadam.tarantool.annotation.Tuple;

/**
 * @author sala
 */
@SpaceId(512)
public class User {
    @Tuple(index = 0)
    public long id;
    @Tuple(index = 1)
    public String name;

    public User() {
    }
}
