package ru.shadam.tarantool.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import ru.shadam.tarantool.annotation.Index;
import ru.shadam.tarantool.annotation.Tuple;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author sala
 */
public class TarantoolPersistentPropertyImpl extends AnnotationBasedPersistentProperty<TarantoolPersistentProperty> implements TarantoolPersistentProperty {
    private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<>();

    static {
        SUPPORTED_ID_PROPERTY_NAMES.add("id");
    }

    private final Integer tupleIndex;
    private final Integer spaceIndex;

    public TarantoolPersistentPropertyImpl(Field field, PropertyDescriptor propertyDescriptor, TarantoolPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);

        this.tupleIndex = Optional.ofNullable(findPropertyOrOwnerAnnotation(Tuple.class))
                .map(Tuple::index)
                .orElse(null);

        this.spaceIndex =
                Optional.ofNullable(findPropertyOrOwnerAnnotation(Index.class))
                .map(Index::id)
                .orElse(null);
    }

    @Override
    public boolean isIdProperty() {
        return super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getName());
    }

    public Integer getTupleIndex() {
        return tupleIndex;
    }

    @Override
    public Integer getSpaceIndexId() {
        return spaceIndex;
    }

    @Override
    protected Association<TarantoolPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }
}
