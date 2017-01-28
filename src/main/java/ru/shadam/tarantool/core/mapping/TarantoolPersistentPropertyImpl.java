package ru.shadam.tarantool.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.Assert;
import ru.shadam.tarantool.annotation.Index;
import ru.shadam.tarantool.annotation.Tuple;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
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

        if(tupleIndex != null) {
            Assert.isTrue(tupleIndex >= 0, "Tuple index cannot be negative");
        }

        this.spaceIndex =
                Optional.ofNullable(findPropertyOrOwnerAnnotation(Index.class))
                .map(Index::id)
                .orElseGet(() -> isIdProperty() ? 0 : null);

        if(spaceIndex != null) {
            Assert.isTrue(spaceIndex >= 0, "Space index cannot be negative");
        }

        if(isIdProperty()) {
            Assert.isTrue(tupleIndex != null, "Id property must have tuple index");
        }
    }

    @Override
    public boolean isIdProperty() {
        return super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getName());
    }

    public OptionalInt getTupleIndex() {
        return tupleIndex == null ? OptionalInt.empty() : OptionalInt.of(tupleIndex);
    }

    @Override
    public OptionalInt getSpaceIndexId() {
        return spaceIndex == null ? OptionalInt.empty() : OptionalInt.of(spaceIndex);
    }

    @Override
    protected Association<TarantoolPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }
}
