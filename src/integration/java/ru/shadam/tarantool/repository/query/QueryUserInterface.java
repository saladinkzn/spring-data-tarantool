package ru.shadam.tarantool.repository.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import ru.shadam.tarantool.repository.entity.User;

/**
 * @author sala
 */
public interface QueryUserInterface extends CrudRepository<User, Long> {

    User findByName(String name);

    Page<User> findByName(String name, Pageable pageRequest);
}
