package ru.shadam.tarantool.repository.configuration;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.util.StringUtils;
import ru.shadam.tarantool.core.TarantoolKeyValueTemplate;
import ru.shadam.tarantool.core.TarantoolOpsImpl;
import ru.shadam.tarantool.core.convert.MappingTarantoolConverter;
import ru.shadam.tarantool.core.mapping.TarantoolMappingContext;

/**
 * @author sala
 */
public class TarantoolRepositoryConfigurationExtension extends KeyValueRepositoryConfigurationExtension {
    private static final String TARANTOOL_CONVERTER_BEAN_NAME = "tarantoolConverter";
    private static final String TARANTOOL_OPS_IMPL_BEAN_NAME = "tarantoolOpsImpl";

    @Override
    protected String getDefaultKeyValueTemplateRef() {
        return "tarantoolKeyValueTemplate";
    }

    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource configurationSource) {
        String tarantolSyncOpsRef = configurationSource.getAttribute("tarantoolSyncOpsRef");
        // register context
        RootBeanDefinition mappingContextDefinition = createTarantoolMappingContext();
        mappingContextDefinition.setSource(configurationSource.getSource());

        registerIfNotAlreadyRegistered(mappingContextDefinition, registry, MAPPING_CONTEXT_BEAN_NAME, configurationSource);

        // Register converter
        RootBeanDefinition redisConverterDefinition = createTarantoolConverterDefinition();
        redisConverterDefinition.setSource(configurationSource.getSource());

        registerIfNotAlreadyRegistered(redisConverterDefinition, registry, TARANTOOL_CONVERTER_BEAN_NAME, configurationSource);

        // register tarantoolOpsImpl
        RootBeanDefinition tarantoolOpsImplDefinition = new RootBeanDefinition(TarantoolOpsImpl.class);

        ConstructorArgumentValues constructorArgumentValuesForTarantoolOpsImpl = new ConstructorArgumentValues();
        if (StringUtils.hasText(tarantolSyncOpsRef)) {
            constructorArgumentValuesForTarantoolOpsImpl.addIndexedArgumentValue(0,
                    new RuntimeBeanReference(tarantolSyncOpsRef));
        }

        tarantoolOpsImplDefinition.setConstructorArgumentValues(constructorArgumentValuesForTarantoolOpsImpl);

        registerIfNotAlreadyRegistered(tarantoolOpsImplDefinition, registry, TARANTOOL_OPS_IMPL_BEAN_NAME,
                configurationSource);

        super.registerBeansForRoot(registry, configurationSource);
    }

    /*
 * (non-Javadoc)
 * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getDefaultKeyValueTemplateBeanDefinition(org.springframework.data.repository.config.RepositoryConfigurationSource)
 */
    @Override
    protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(
            RepositoryConfigurationSource configurationSource) {
        RootBeanDefinition keyValueTemplateDefinition = new RootBeanDefinition(TarantoolKeyValueTemplate.class);

        ConstructorArgumentValues constructorArgumentValuesForKeyValueTemplate = new ConstructorArgumentValues();
        constructorArgumentValuesForKeyValueTemplate.addIndexedArgumentValue(0,
                new RuntimeBeanReference(TARANTOOL_OPS_IMPL_BEAN_NAME));
        constructorArgumentValuesForKeyValueTemplate.addIndexedArgumentValue(1,
                new RuntimeBeanReference(MAPPING_CONTEXT_BEAN_NAME));
        constructorArgumentValuesForKeyValueTemplate.addIndexedArgumentValue(2,
                new RuntimeBeanReference(TARANTOOL_CONVERTER_BEAN_NAME));

        keyValueTemplateDefinition.setConstructorArgumentValues(constructorArgumentValuesForKeyValueTemplate);

        return keyValueTemplateDefinition;
    }

    private RootBeanDefinition createTarantoolMappingContext() {
        return new RootBeanDefinition(TarantoolMappingContext.class);
    }

    private RootBeanDefinition createTarantoolConverterDefinition() {
        RootBeanDefinition beanDef = new RootBeanDefinition(MappingTarantoolConverter.class);

        ConstructorArgumentValues args = new ConstructorArgumentValues();
        args.addIndexedArgumentValue(0, new RuntimeBeanReference(MAPPING_CONTEXT_BEAN_NAME));
        beanDef.setConstructorArgumentValues(args);

        return beanDef;
    }

}
