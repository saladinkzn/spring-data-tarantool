package ru.shadam.tarantool.repository.query;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.repository.RepositoryIntegrationTests;
import ru.shadam.tarantool.repository.entity.User;

import java.io.IOException;

import static ru.shadam.tarantool.util.DockerTarantoolIntegrationTest.createApplicationContext;

/**
 * @author sala
 */
public class QueryIntegrationTests {
    private AnnotationConfigApplicationContext applicationContext;
    private QueryUserInterface queryUserInterface;

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integration/resources/docker/docker-compose.yml")
            .waitingForService("tarantool", HealthChecks.toHaveAllPortsOpen())
            .build();

    @Before
    public void setUp() throws IOException {
        this.applicationContext = createApplicationContext(docker);
        TarantoolClientOps bean = (TarantoolClientOps) this.applicationContext.getBean("tarantoolSyncOps");
        String eval = IOUtils.toString(RepositoryIntegrationTests.class.getResource("/init.lua"));
        bean.eval(eval);

        queryUserInterface = applicationContext.getBean(QueryUserInterface.class);

        User entity = createUser();
        queryUserInterface.save(entity);
    }

    @After
    public void clean() {
        queryUserInterface.deleteAll();
        applicationContext.close();
    }

    @Test
    public void testFindByName() {
        User result = queryUserInterface.findByName("ASDASD");
        Assert.assertNotNull(result);
        Assert.assertEquals("ASDASD", result.name);
    }

    @Test
    public void testFindByNameNotFound() {
        User result = queryUserInterface.findByName("DEFDEF");
        Assert.assertNull(result);
    }

    @Test
    public void testPagingMethod() {
        for (int i = 0; i < 9; i ++) {
            queryUserInterface.save(createUser());
        }

        Page<User> page = queryUserInterface.findByName("ASDASD", new PageRequest(0,3));
        Assert.assertEquals(3, page.getSize());
        Assert.assertEquals(3, page.getContent().size());
        Assert.assertEquals(4, page.getTotalPages());
        Assert.assertEquals(10, page.getTotalElements());
    }

    private static User createUser() {
        User entity = new User();
        entity.name = "ASDASD";
        return entity;
    }
}
