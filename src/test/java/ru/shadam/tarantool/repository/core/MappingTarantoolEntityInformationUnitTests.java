package ru.shadam.tarantool.repository.core;

/**
 * @author sala
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mapping.model.MappingException;
import ru.shadam.tarantool.core.convert.ConversionTestEntities;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;

import java.io.Serializable;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class MappingTarantoolEntityInformationUnitTests<T> {

    @Mock
    TarantoolPersistentEntity<T> entity;

    /**
     * @see DATAREDIS-425
     */
    @Test(expected = MappingException.class)
    @SuppressWarnings("unchecked")
    public void throwsMappingExceptionWhenNoIdPropertyPresent() {

        when(entity.hasIdProperty()).thenReturn(false);
        when(entity.getType()).thenReturn((Class<T>) ConversionTestEntities.Person.class);
        new MappingTarantoolEntityInformation<>(entity);
    }
}