package ru.shadam.tarantool.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.io.IOUtils;
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
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.repository.CrudRepository;
import org.tarantool.*;
import ru.shadam.tarantool.core.SimpleSocketChannelProvider;
import ru.shadam.tarantool.repository.configuration.EnableTarantoolRepositories;
import ru.shadam.tarantool.repository.entity.Address;
import ru.shadam.tarantool.repository.entity.LogEntry;
import ru.shadam.tarantool.repository.entity.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author sala
 */
public class RepositoryIntegrationTests {
    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integration/resources/docker/docker-compose.yml")
            .waitingForService("tarantool", HealthChecks.toHaveAllPortsOpen())
            .build();

    private UserRepository userRepository;
    private LogEntryRepository logEntryRepository;
    private AddressRepository addressRepository;

    @Before
    public void setUp() throws IOException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(TarantoolConfiguration.class);
        DockerPort dockerPort = docker.containers()
                .container("tarantool")
                .port(3301);
        ImmutableMap<String, Object> env = ImmutableMap.of("tarantoolPort", dockerPort.getExternalPort());
        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("rule", env));
        applicationContext.refresh();

        TarantoolClientOps bean = (TarantoolClientOps) applicationContext.getBean("tarantoolSyncOps");
        String eval = IOUtils.toString(RepositoryIntegrationTests.class.getResource("/init.lua"));
        bean.eval(eval);
        //
        userRepository = applicationContext.getBean(UserRepository.class);
        userRepository.deleteAll();

        logEntryRepository = applicationContext.getBean(LogEntryRepository.class);
        logEntryRepository.deleteAll();

        addressRepository = applicationContext.getBean(AddressRepository.class);
        addressRepository.deleteAll();
    }

    @Test
    public void testFindOne() {
        Assert.assertNull(userRepository.findOne(1L));
    }

    @Test
    public void testInsertAndFetch() {
        final User user = new User();
        user.id = 2L;
        user.name = "user";

        final User saved = userRepository.save(user);

        Assert.assertEquals(1, Iterables.size(userRepository.findAll()));
    }

    @Test
    public void testInsertAndDelete() {
        final User user = new User();
        user.id = 3L;
        user.name = "user2";

        final User saved = userRepository.save(user);

        userRepository.delete(3L);

        Assert.assertEquals(0, Iterables.size(userRepository.findAll()));
    }

    @Test
    public void testInsertAndUpdate() {
        final User user = new User();
        user.id = 4L;
        user.name = "John Doe";

        final User saved = userRepository.save(user);
        Assert.assertNotNull(saved);
        Assert.assertEquals(4L, saved.id);
        Assert.assertEquals("John Doe", saved.name);

        user.name = "Jane Doe";

        final User updated = userRepository.save(user);
        Assert.assertNotNull(updated);
        Assert.assertEquals(4L, updated.id);
        Assert.assertEquals("Jane Doe", updated.name);

        final User found = userRepository.findOne(4L);
        Assert.assertNotNull(found);
        Assert.assertEquals("Jane Doe", found.name);
    }

    @Test
    public void testMultipleSave() {
        final User user = new User();
        user.id = 5L;
        user.name = "John Doe";

        final User user2 = new User();
        user2.id = 6L;
        user2.name = "Jane Doe";

        userRepository.save(Arrays.asList(user, user2));

        Assert.assertEquals(2, userRepository.count());
    }


    @Test
    public void testInsertAndDeleteByObject() {
        final User user = new User();
        user.id = 7L;
        user.name = "John Doe";

        userRepository.save(user);

        userRepository.delete(user);

        Assert.assertEquals(0, userRepository.count());
    }

    @Test
    public void testExists() {
        final User user = new User();
        user.id = 8L;
        user.name = "John Doe";

        final User saved = userRepository.save(user);

        Assert.assertTrue(userRepository.exists(8L));
    }

    @Test
    public void testBasicCrudForStringKey() {
        LogEntry logEntry = new LogEntry("asdasd", "Hello, world");
        logEntryRepository.save(logEntry);

        Assert.assertEquals("asdasd", logEntry.getUid());
        Assert.assertEquals("Hello, world", logEntry.getText());

        logEntryRepository.delete("asdasd");

        Assert.assertEquals(0, logEntryRepository.count());
    }

    @Test
    public void testAutoIdGeneration() {
        User user = new User();
        user.name = "John Doe";

        User saved = userRepository.save(user);
        Assert.assertNotEquals(0L, saved.id);
    }

    @Test
    public void testNullFields() {
        Address address = new Address(null, "street", null);

        Address saved = addressRepository.save(address);

        Address retrieved = addressRepository.findOne(saved.getId());
        Assert.assertNull(retrieved.getCity());
        Assert.assertNull(retrieved.getNumber());
        Assert.assertEquals("street", retrieved.getStreet());
    }


    @EnableTarantoolRepositories(basePackages = {"ru.shadam.tarantool"}, considerNestedRepositories = true)
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
            config.username = "test";
            config.password = "test";
            config.initTimeoutMillis = 5000;
            config.writeTimeoutMillis = 5000;
            return new TarantoolClientImpl(socketChannelProvider, config);
        }

        @Bean
        public SocketChannelProvider socketChannelProvider(
            @Value("${DOCKER_HOST_IP:localhost}") String DOCKER_HOST_IP,
            @Value("${tarantoolPort}") int tarantoolPort
        ) {
            System.out.println("DOCKER_HOST_IP: " + DOCKER_HOST_IP);
            return new SimpleSocketChannelProvider(DOCKER_HOST_IP, tarantoolPort);
        }

        @Bean
        @Autowired
        public TarantoolClientOps<Integer, List<?>, Object, List<?>> tarantoolSyncOps(
            TarantoolClient tarantoolClient
        ) {
            return tarantoolClient.syncOps();
        }
    }

    private interface UserRepository extends CrudRepository<User, Long> {
    }

    private interface LogEntryRepository extends CrudRepository<LogEntry, String> {
        
    }

    private interface AddressRepository extends CrudRepository<Address, Long> {
        
    }



}
