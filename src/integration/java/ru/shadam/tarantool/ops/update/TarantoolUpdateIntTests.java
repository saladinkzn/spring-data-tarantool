package ru.shadam.tarantool.ops.update;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.SimpleSocketChannelProvider;
import ru.shadam.tarantool.core.TarantoolOperations;
import ru.shadam.tarantool.core.TarantoolTemplate;
import ru.shadam.tarantool.core.update.UpdateIntOperation;
import ru.shadam.tarantool.core.update.UpdateIntOperator;
import ru.shadam.tarantool.serializer.PlainTarantoolSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sala
 */
@RunWith(Parameterized.class)
public class TarantoolUpdateIntTests {
    private static final String COUNTER = "counter";

    private TarantoolClientOps<Integer, List<?>, Object, List<?>> clientOps;
    private TarantoolOperations<Long, Long> operations;

    private final long initialValue;
    private final UpdateIntOperator operator;
    private final int argument;
    private final long expectedValue;

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integration/resources/docker/docker-compose.yml")
            .waitingForService("tarantool", HealthChecks.toHaveAllPortsOpen())
            .build();


    @Parameterized.Parameters(name = "{0} {1} {2}={3}")
    public static List<Object[]> parameters() {
        return Arrays.asList(
                new Object[] { 1L, UpdateIntOperator.ADDITION, 1, 2L },
                new Object[] { 1L, UpdateIntOperator.SUBSTRACTION, 1, 0L },
                new Object[] { 0b111, UpdateIntOperator.BITWISE_AND, 0b010, 0b010 },
                new Object[] { 0b010, UpdateIntOperator.BITWISE_OR, 0b101, 0b111 }
        );
    }

    public TarantoolUpdateIntTests(long initialValue, UpdateIntOperator operator, int argument, long expectedValue) {
        this.initialValue = initialValue;
        this.operator = operator;
        this.argument = argument;
        this.expectedValue = expectedValue;
    }

    @Before
    public void setUp() throws Exception {
        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "test";
        config.password = "test";

        int tarantoolPort = docker.containers()
                .container("tarantool")
                .port(3301)
                .getExternalPort();
        
        SimpleSocketChannelProvider provider = new SimpleSocketChannelProvider("localhost", tarantoolPort);
        
        clientOps = new TarantoolClientImpl(provider, config).syncOps();

        TarantoolTemplate<Long, Long> template = new TarantoolTemplate<>();
        template.setSyncOps(clientOps);
        template.setKeyClass(Long.class);
        template.setValueSerializer(new PlainTarantoolSerializer<>(Long.class));
        template.afterPropertiesSet();
        
        this.operations = template;

        clientOps.eval(IOUtils.toString(TarantoolUpdateIntTests.class.getResource("/init.lua")));

        this.operations.deleteAll(COUNTER);

    }

    @Test
    public void testUpdate() {
        operations.insert(COUNTER, 1L, initialValue);
        Long result = operations.update(COUNTER, 1L, Collections.singletonList(new UpdateIntOperation(operator, 2, argument)));

        Assert.assertEquals(expectedValue, result.longValue());
    }
}
