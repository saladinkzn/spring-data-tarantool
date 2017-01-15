package ru.shadam.tarantool.repository.configuration;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author sala
 */
public class TarantoolRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableTarantoolRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new TarantoolRepositoryConfigurationExtension();
    }
}
