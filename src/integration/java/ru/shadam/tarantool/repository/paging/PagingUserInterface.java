package ru.shadam.tarantool.repository.paging;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.shadam.tarantool.repository.entity.User;

/**
 * @author sala
 */
@Repository
public interface PagingUserInterface extends PagingAndSortingRepository<User, Long> {
}
