package ru.shadam.tarantool.util;

import com.palantir.docker.compose.DockerComposeRule;
import org.springframework.core.env.PropertySource;

import java.util.Optional;

/**
 * @author sala
 */
public class DockerComposePropertySource extends PropertySource<DockerComposeRule> {
    public DockerComposePropertySource(String name, DockerComposeRule source) {
        super(name, source);
    }

    @Override
    public Object getProperty(String name) {
        Optional<ContainerNameAndInternalPort> maybeContainerAndPort = tryParse(name);

        return maybeContainerAndPort
                .map(it -> source.containers().container(it.containerName).port(it.internalPort).getExternalPort())
                .orElse(null);
    }

    private Optional<ContainerNameAndInternalPort> tryParse(String name) {
        String[] containerNameAndPort = name.split("\\.");

        if (containerNameAndPort.length != 2) {
            if (logger.isDebugEnabled()) {
                logger.debug("Input string does not match format containerName.port");
            }
            return Optional.empty();
        }

        String containerName = containerNameAndPort[0];
        int port;
        try {
            port = Integer.parseInt(containerNameAndPort[1]);
        } catch (NumberFormatException nfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("Port string cannot be parsed to integer");
            }
            return Optional.empty();
        }

        return Optional.of(new ContainerNameAndInternalPort(containerName, port));
    }

    private static class ContainerNameAndInternalPort {
        private String containerName;
        private int internalPort;

        public ContainerNameAndInternalPort(String containerName, int internalPort) {
            this.containerName = containerName;
            this.internalPort = internalPort;
        }
    }
}
