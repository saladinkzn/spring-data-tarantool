package ru.shadam.tarantool.repository.paging;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.repository.RepositoryIntegrationTests;
import ru.shadam.tarantool.repository.entity.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static ru.shadam.tarantool.util.DockerTarantoolIntegrationTest.createApplicationContext;

/**
 * @author sala
 */
public class PagingIntegrationTests {
    private AnnotationConfigApplicationContext applicationContext;
    private PagingUserInterface pagingUserInterface;

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integration/resources/docker/docker-compose.yml")
            .waitingForService("tarantool", HealthChecks.toHaveAllPortsOpen())
            .build();

    @Before
    public void setUp() throws IOException {
        this.applicationContext = createApplicationContext(docker);

        TarantoolClientOps bean = (TarantoolClientOps) applicationContext.getBean("tarantoolSyncOps");
        String eval = IOUtils.toString(RepositoryIntegrationTests.class.getResource("/init.lua"));
        bean.eval(eval);

        pagingUserInterface = applicationContext.getBean(PagingUserInterface.class);

        for (int i = 1; i <= 10; i++) {
            User entity = new User();
            entity.id = i;
            entity.name = String.format("user%02d", i);
            
            pagingUserInterface.save(entity);
        }
    }

    @After
    public void tearDown() {
        applicationContext.close();
    }

    @Test
    public void testPaging() {
        Page<User> userPage = pagingUserInterface.findAll(new PageRequest(0, 5));

        List<User> users = userPage.getContent();
        Assert.assertThat(users, hasSize(5));
        Assert.assertThat(
                users.stream().map(it -> it.id).collect(Collectors.toList()),
                contains(LongStream.rangeClosed(1, 5).boxed().toArray(Long[]::new))
        );
    }

    @Test
    public void testPagingSecondPage() {
        Page<User> userPage = pagingUserInterface.findAll(new PageRequest(1, 5));

        List<User> users = userPage.getContent();
        Assert.assertThat(users, hasSize(5));

        Assert.assertThat(
                users.stream().map(it -> it.id).collect(Collectors.toList()),
                contains(LongStream.rangeClosed(6, 10).boxed().toArray(Long[]::new))
        );
    }

    @Test
    public void testPagingAndSortById() {
        Page<User> userPage = pagingUserInterface.findAll(
                new PageRequest(0, 5, new Sort(Sort.Direction.DESC, "id"))
        );
        List<User> users = userPage.getContent();
        Assert.assertThat(
                users.stream().map(it -> it.id).collect(Collectors.toList()),
                contains(LongStream.iterate(10, i -> i - 1).limit(5).boxed().toArray(Long[]::new))
        );
    }

    @Test
    public void testPagingAndSortByName() {
        Page<User> userPage = pagingUserInterface.findAll(
                new PageRequest(0, 5, new Sort(Sort.Direction.DESC, "name"))
        );
        List<User> users = userPage.getContent();

        Assert.assertThat(
                users.stream().map(it -> it.id).collect(Collectors.toList()),
                contains(LongStream.iterate(10, i -> i - 1).limit(5).boxed().toArray(Long[]::new))
        );
    }

    @Test
    public void testCount() {
        long result = pagingUserInterface.count();
        Assert.assertEquals(10, result);
    }
}
