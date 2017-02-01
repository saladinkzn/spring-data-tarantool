package ru.shadam.tarantool.ops;

import com.google.common.collect.Iterables;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.tarantool.*;
import ru.shadam.tarantool.core.SimpleSocketChannelProvider;
import ru.shadam.tarantool.core.TarantoolOperations;
import ru.shadam.tarantool.core.TarantoolTemplate;
import ru.shadam.tarantool.ops.entity.User;

import java.util.List;

/**
 * @author sala
 */
public class TarantoolOperationsTests {
    public static final int SPACE_ID = 512;
    public static final int SPACE_STRING_ID = 513;
    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integration/resources/docker/docker-compose.yml")
            .waitingForService("tarantool", HealthChecks.toHaveAllPortsOpen())
            .build();

    private AnnotationConfigApplicationContext applicationContext;
    private TarantoolOperations<Long, User> userOperations;
    private TarantoolOperations<String, User> stringUserTarantoolOperations;

    @Before
    public void setUp() {
        this.applicationContext = new AnnotationConfigApplicationContext(TarantoolConfiguration.class);
        userOperations = ((TarantoolOperations<Long, User>) applicationContext.getBean("userTarantoolOperations"));
        userOperations.deleteAll(SPACE_ID);
        //
        stringUserTarantoolOperations = ((TarantoolOperations<String, User>) applicationContext.getBean("stringUserTarantoolOperations"));
        stringUserTarantoolOperations.deleteAll(SPACE_STRING_ID);
    }

    @Test
    public void testFindOne() {
        final User user = userOperations.select(SPACE_ID, 0, 1L);
        Assert.assertNull(user);
    }

    @Test
    public void testInsertAndFetch() {
        User user = new User();
        user.id = 2L;

        final User insert = userOperations.insert(SPACE_ID, 2L, user);
        //
        final User selected = userOperations.select(SPACE_ID, 0, 2L);
        Assert.assertEquals(2L, selected.id);
    }

    @Test
    public void testSelectKeys() {
        User user = new User();
        user.id = 3L;
        user.name = "John Doe";

        userOperations.insert(SPACE_ID, 3L, user);

        final List<Long> keys = userOperations.selectKeys(SPACE_ID, 0);

        Assert.assertEquals(1, keys.size());
        Assert.assertEquals(3L, Iterables.getOnlyElement(keys).longValue());
    }

    @Test
    public void testInsertAndFetchStringKeys() {
        User user = new User();
        user.id = 4L;
        user.name = "John Doe";

        stringUserTarantoolOperations.insert(SPACE_STRING_ID, "4", user);

        final List<String> keys = stringUserTarantoolOperations.selectKeys(SPACE_STRING_ID, 0);
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals("4", Iterables.getOnlyElement(keys));
    }

    @Test
    public void testInsertAndDelete() {
        stringUserTarantoolOperations.insert(SPACE_STRING_ID, "5", null);

        final User user = stringUserTarantoolOperations.delete(SPACE_STRING_ID, "5");
        Assert.assertNull(user);

        Assert.assertEquals(0, stringUserTarantoolOperations.select(SPACE_STRING_ID, 0).size());
    }



    @Configuration
    public static class TarantoolConfiguration {
        @Bean
        private static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        @Autowired
        public TarantoolClient tarantoolClient(
                SocketChannelProvider socketChannelProvider
        ) {
            final TarantoolClientConfig config = new TarantoolClientConfig();
            config.username = "guest";
            config.initTimeoutMillis = 5000;
            config.writeTimeoutMillis = 5000;
            return new TarantoolClientImpl(socketChannelProvider, config);
        }

        @Bean
        public SocketChannelProvider socketChannelProvider(
                @Value("${DOCKER_HOST_IP:localhost}") String DOCKER_HOST_IP
        ) {
            System.out.println("DOCKER_HOST_IP: " + DOCKER_HOST_IP);
            return new SimpleSocketChannelProvider(DOCKER_HOST_IP, 3301);
        }

        @Bean
        @Autowired
        public TarantoolClientOps<Integer, List<?>, Object, List<?>> tarantoolSyncOps(
                TarantoolClient tarantoolClient
        ) {
            return tarantoolClient.syncOps();
        }

        @Bean
        @Autowired
        public TarantoolOperations<Long, User> userTarantoolOperations(
                TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps
        ) {
            final TarantoolTemplate<Long, User> tarantoolTemplate = new TarantoolTemplate<>();
            tarantoolTemplate.setSyncOps(syncOps);
            tarantoolTemplate.setKeyClass(Long.class);
            return tarantoolTemplate;
        }

        @Bean
        @Autowired
        public TarantoolOperations<String, User> stringUserTarantoolOperations(
                TarantoolClientOps<Integer, List<?>, Object, List<?>> syncOps
        ) {
            final TarantoolTemplate<String, User> stringUserTarantoolTemplate = new TarantoolTemplate<>();
            stringUserTarantoolTemplate.setKeyClass(String.class);
            stringUserTarantoolTemplate.setSyncOps(syncOps);
            return stringUserTarantoolTemplate;
        }
    }
}
