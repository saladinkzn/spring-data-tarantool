package ru.shadam.tarantool.util;

import com.palantir.docker.compose.DockerComposeRule;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author sala
 */
public abstract class DockerTarantoolIntegrationTest {
    public static AnnotationConfigApplicationContext createApplicationContext(DockerComposeRule docker) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.getEnvironment()
                .getPropertySources()
                .addFirst(new DockerComposePropertySource("docker-compose", docker));
        applicationContext.register(TarantoolConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }
}
