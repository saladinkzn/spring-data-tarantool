package ru.shadam.tarantool.ops.update;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;
import org.tarantool.TarantoolClientOps;
import ru.shadam.tarantool.core.SimpleSocketChannelProvider;
import ru.shadam.tarantool.core.TarantoolOperations;
import ru.shadam.tarantool.core.TarantoolTemplate;
import ru.shadam.tarantool.core.update.UpdateArgOperation;
import ru.shadam.tarantool.core.update.UpdateArgOperator;
import ru.shadam.tarantool.core.update.UpdateStringOperation;
import ru.shadam.tarantool.core.update.UpdateStringOperator;
import ru.shadam.tarantool.serializer.PlainTarantoolSerializer;

import java.util.Collections;
import java.util.List;

/**
 * @author sala
 */
public class TarantoolUpdateObjectTests {
    private static final String COUNTER = "counter";

    private TarantoolClientOps<Integer, List<?>, Object, List<?>> clientOps;
    private TarantoolOperations<Long, String> operations;

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/integration/resources/docker/docker-compose.yml")
            .waitingForService("tarantool", HealthChecks.toHaveAllPortsOpen())
            .build();
    
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

        TarantoolTemplate<Long, String> template = new TarantoolTemplate<>();
        template.setSyncOps(clientOps);
        template.setKeyClass(Long.class);
        template.setValueSerializer(new PlainTarantoolSerializer<>(String.class));
        template.afterPropertiesSet();

        this.operations = template;

        clientOps.eval(IOUtils.toString(TarantoolUpdateIntTests.class.getResource("/init.lua")));

        this.operations.deleteAll(COUNTER);
    }

    @Test
    public void testAssign() {
        operations.insert(COUNTER, 1L, "foo");

        String result = operations.update(COUNTER, 1L, Collections.singletonList(new UpdateArgOperation(UpdateArgOperator.ASSIGN, 2, "bar")));

        Assert.assertEquals("bar", result);
    }

    @Test
    public void testSplice() {
        operations.insert(COUNTER, 1L, "XYZ");

        String result = operations.update(COUNTER, 1L, Collections.singletonList(new UpdateStringOperation(UpdateStringOperator.SPLICE, 2, 2, 1, "!!")));

        Assert.assertEquals("X!!Z", result);
    }



}
