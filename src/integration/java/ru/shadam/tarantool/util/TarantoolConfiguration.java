package ru.shadam.tarantool.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.tarantool.*;
import ru.shadam.tarantool.core.SimpleSocketChannelProvider;
import ru.shadam.tarantool.repository.configuration.EnableTarantoolRepositories;

import java.util.List;

/**
 * @author sala
 */
@EnableTarantoolRepositories(basePackages = {"ru.shadam.tarantool"})
@Configuration
public class TarantoolConfiguration {
    @Bean
    private static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(Environment environment) {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setEnvironment(environment);
        return propertySourcesPlaceholderConfigurer;
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
            @Value("${tarantool.3301}") int tarantoolPort
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
