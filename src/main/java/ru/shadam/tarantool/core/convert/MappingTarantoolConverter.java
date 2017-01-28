package ru.shadam.tarantool.core.convert;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.convert.*;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import ru.shadam.tarantool.core.mapping.TarantoolMappingContext;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentEntity;
import ru.shadam.tarantool.core.mapping.TarantoolPersistentProperty;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author sala
 */
public class MappingTarantoolConverter implements TarantoolConverter, InitializingBean {
    private static final int TYPE_HINT_INDEX = 0;

    private final EntityInstantiators entityInstantiators;
    private final TarantoolMappingContext mappingContext;
    private final TypeMapper<TarantoolData> typeMapper;
    private final TarantoolDefaultConversionService conversionService;
    private final CustomConversions customConversions;

    public MappingTarantoolConverter(TarantoolMappingContext tarantoolMappingContext) {
        this.mappingContext = tarantoolMappingContext;

        this.conversionService = new TarantoolDefaultConversionService();

        this.customConversions = new CustomConversions();
        this.entityInstantiators = new EntityInstantiators();
        this.typeMapper = new DefaultTypeMapper<>(new TarantoolTypeAliasAccessor(conversionService));
    }

    @Override
    public TarantoolMappingContext getMappingContext() {
        return null;
    }

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }

    @Override
    public <R> R read(Class<R> type, TarantoolData source) {
        return readInternal(Path.empty(), type, source);
    }

    @SuppressWarnings("unchecked")
    private <R> R readInternal(Path path, Class<R> type, TarantoolData source) {
        // first item in tuple is class information.
        if (source.getTuple() == null || source.getTuple().isEmpty()) {
            return null;
        }

        TypeInformation<?> readType = typeMapper.readType(source);
        TypeInformation<?> typeToUse = readType != null ? readType : ClassTypeInformation.from(type);
        final TarantoolPersistentEntity<?> entity = mappingContext.getPersistentEntity(typeToUse);

        if (conversionService.canConvert(Tuple.class, typeToUse.getType())) {
            return ((R) conversionService.convert(source.getTuple(), typeToUse.getType()));
        }

        final EntityInstantiator instantiator = entityInstantiators.getInstantiatorFor(entity);

        final Object instance = instantiator.createInstance(entity,
                new PersistentEntityParameterValueProvider<>(entity,
                        new ConverterAwareParameterValueProvider(source, conversionService), null));

        final PersistentPropertyAccessor accessor = entity.getPropertyAccessor(instance);

        entity.doWithProperties(new PropertyHandler<TarantoolPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(TarantoolPersistentProperty persistentProperty) {
                if(!persistentProperty.getTupleIndex().isPresent()) {
                    // Skip property that doesn't have tupleIndex
                    return;
                }

                final int tupleIndex = persistentProperty.getTupleIndex().getAsInt();

                Path currentPath = !path.isEmpty() ? Path.concat(path, tupleIndex) : Path.of(tupleIndex);

                PreferredConstructor<?, TarantoolPersistentProperty> constructor = entity.getPersistenceConstructor();

                if (constructor.isConstructorParameter(persistentProperty)) {
                    return;
                }

                Object currentValue = source.getTuple().get(currentPath);
                if(currentValue == null) {
                    return;
                }

                // TODO: list and map support
                if (persistentProperty.isCollectionLike()) {

                    Object targetValue = readCollectionOrArray(currentPath, persistentProperty.getType(),
                            persistentProperty.getTypeInformation().getComponentType().getActualType().getType(), source.getTuple());
                    accessor.setProperty(persistentProperty, targetValue);

                } else if (persistentProperty.isEntity() && !conversionService.canConvert(currentValue.getClass(),  persistentProperty.getTypeInformation().getActualType().getType())) {
                    Class<?> targetType = persistentProperty.getTypeInformation().getActualType().getType();

                    Tuple bucket = source.getTuple().extract(currentPath);

                    TarantoolData source = new TarantoolData(bucket);

                    String type = ((String) bucket.get(Path.concat(path, TYPE_HINT_INDEX)));
                    if (type != null && !type.isEmpty()) {
                        source.getTuple().set(Path.of(TYPE_HINT_INDEX), type);
                    }

                    accessor.setProperty(persistentProperty, MappingTarantoolConverter.this.readInternal(currentPath, targetType, source));
                } else {
                    if (persistentProperty.isIdProperty() && path.isEmpty()) {
                        accessor.setProperty(persistentProperty, MappingTarantoolConverter.this.fromTuple(source.getTuple().get(currentPath), persistentProperty.getActualType()));
                    }

                    Class<?> ttu = MappingTarantoolConverter.this.getTypeHint(currentPath, source.getTuple(), persistentProperty.getActualType());
                    accessor.setProperty(persistentProperty, MappingTarantoolConverter.this.fromTuple(source.getTuple().get(currentPath), ttu));
                }
            }
        });

        return ((R) instance);
    }

    private Class<?> getTypeHint(Path path, Tuple bucket, Class<?> fallback) {

        Object typeInfo = null;

        if (typeInfo == null) {
            return fallback;
        }

        String typeName = fromTuple(typeInfo, String.class);
        try {
            return ClassUtils.forName(typeName, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new MappingException(String.format("Cannot find class for type %s. ", typeName), e);
        } catch (LinkageError e) {
            throw new MappingException(String.format("Cannot find class for type %s. ", typeName), e);
        }
    }

    private <T> T fromTuple(Object source, Class<T> actualType) {
        return conversionService.convert(source, actualType);
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public void write(Object source, final TarantoolData sink) {
        final TarantoolPersistentEntity entity = mappingContext.getPersistentEntity(source.getClass());

        sink.setSpaceId(entity.getSpaceId());

        writeInternal(entity.getSpaceId(), Path.empty(), source, entity.getTypeInformation(), sink);
        sink.setId(getConversionService().convert(entity.getIdentifierAccessor(source).getIdentifier(), entity.getIdProperty().getType()));
    }

    /**
     * @param spaceId
     * @param path
     * @param value
     * @param typeHint
     * @param sink
     */
    private void writeInternal(final int spaceId, final Path path, final Object value, TypeInformation<?> typeHint,
                               final TarantoolData sink) {
        if (value == null) {
            return;
        }

        if (value.getClass() != typeHint.getType()) {
            sink.getTuple().set((!path.isEmpty() ? Path.concat(path, TYPE_HINT_INDEX) : Path.of(TYPE_HINT_INDEX)), value.getClass().getName());
        }

        final TarantoolPersistentEntity<?> entity = mappingContext.getPersistentEntity(value.getClass());
        final PersistentPropertyAccessor accessor = entity.getPropertyAccessor(value);

        entity.doWithProperties(new PropertyHandler<TarantoolPersistentProperty>() {
            @Override
            public void doWithPersistentProperty(TarantoolPersistentProperty persistentProperty) {
                final OptionalInt optTupleIndex = persistentProperty.getTupleIndex();
                if(!optTupleIndex.isPresent()) {
                    // Do not write property without tuple index
                    return;
                }

                final int tupleIndex = optTupleIndex.getAsInt();

                Path propertyPath = (!path.isEmpty() ? Path.concat(path, tupleIndex) : Path.of(tupleIndex));

                if (persistentProperty.isIdProperty()) {
                    sink.getTuple().set(propertyPath, accessor.getProperty(persistentProperty));
                    return;
                }

                if (persistentProperty.isCollectionLike()) {

                    final Object property = accessor.getProperty(persistentProperty);

                    if (property == null || Iterable.class.isAssignableFrom(property.getClass())) {

                        writeCollection(spaceId, propertyPath, (Iterable<?>) property,
                                persistentProperty.getTypeInformation().getComponentType(), sink);
                    } else if (property.getClass().isArray()) {

                        writeCollection(spaceId, propertyPath, CollectionUtils.arrayToList(property),
                                persistentProperty.getTypeInformation().getComponentType(), sink);
                    } else {

                        throw new RuntimeException("Don't know how to handle " + property.getClass() + " type collection");
                    }

                } else if (persistentProperty.isEntity()) {
                    writeInternal(spaceId, propertyPath, accessor.getProperty(persistentProperty),
                            persistentProperty.getTypeInformation().getActualType(), sink);
                } else {
                    Object propertyValue = accessor.getProperty(persistentProperty);
                    writeToBucket(propertyPath, propertyValue, sink, persistentProperty.getType());
                }
            }
        });
    }

    /**
     * @param spaceId
     * @param path
     * @param values
     * @param typeHint
     * @param sink
     */
    private void writeCollection(int spaceId, Path path, Iterable<?> values, TypeInformation<?> typeHint,
                                 TarantoolData sink) {

        if (values == null) {
            return;
        }

        int i = 0;
        for (Object value : values) {

            if (value == null) {
                break;
            }

            Path currentPath = Path.concat(path, i);


            if (isTarantoolNativeType(value.getClass()) || customConversions.hasCustomWriteTarget(value.getClass())) {
                writeToBucket(currentPath, value, sink, typeHint.getType());
            } else {
                writeInternal(spaceId, currentPath, value, typeHint, sink);
            }
            i++;
        }
    }

    private void writeToBucket(Path path, Object value, TarantoolData sink, Class<?> propertyType) {

        if (value == null) {
            return;
        }

        if(isTarantoolNativeType(value.getClass())) {
            sink.getTuple().set(path, value);
        }

        // if value.class is supported {
        if (customConversions.hasCustomWriteTarget(value.getClass())) {
            Class<?> targetType = customConversions.getCustomWriteTarget(value.getClass());

            if(isTarantoolNativeType(targetType)) {
                sink.getTuple().set(path, toTarantoolNativeType(value, targetType));
            }  else {
                throw new IllegalArgumentException(
                        String.format("Cannot convert value '%s' of type %s to tarantool native type.", value, value.getClass()));
            }
        }
    }

    private Object readCollectionOrArray(Path path, Class<?> collectionType, Class<?> valueType, Tuple tuple) {
        List<Path> keys = new ArrayList<>(tuple.extractAllKeysFor(path));

        boolean isArray = collectionType.isArray();
        Class<?> collectionTypeToUse = isArray ? ArrayList.class : collectionType;
        Collection<Object> target = CollectionFactory.createCollection(collectionTypeToUse, valueType, keys.size());

        for (Path key : keys) {

//            if (key.endsWith(TYPE_HINT_ALIAS)) {
//                continue;
//            }
//
            Tuple elementData = tuple.extract(key);

//            byte[] typeInfo = elementData.get(key + "." + TYPE_HINT_ALIAS);
//            if (typeInfo != null && typeInfo.length > 0) {
//                elementData.put(TYPE_HINT_ALIAS, typeInfo);
//            }

            Class<?> typeToUse = getTypeHint(key, tuple, valueType);
            if (isTarantoolNativeType(valueType)) {
                target.add(fromTarantoolNativeType(elementData.get(key), typeToUse));
            } else {
                target.add(readInternal(key, valueType, new TarantoolData(elementData)));
            }
        }

        return isArray ? toArray(target, collectionType, valueType) : target;
    }

    /**
     * Converts a given {@link Collection} into an array considering primitive types.
     *
     * @param source {@link Collection} of values to be added to the array.
     * @param arrayType {@link Class} of array.
     * @param valueType to be used for conversion before setting the actual value.
     * @return
     */
    private Object toArray(Collection<Object> source, Class<?> arrayType, Class<?> valueType) {

        if (!ClassUtils.isPrimitiveArray(arrayType)) {
            return source.toArray((Object[]) Array.newInstance(valueType, source.size()));
        }

        Object targetArray = Array.newInstance(valueType, source.size());
        Iterator<Object> iterator = source.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Array.set(targetArray, i, conversionService.convert(iterator.next(), valueType));
            i++;
        }
        return targetArray;
    }

    private static boolean isTarantoolNativeType(Class<?> type) {
        // MsgPack supported classes:
        return Boolean.class.isAssignableFrom(type) ||
                Number.class.isAssignableFrom(type) ||
                String.class.isAssignableFrom(type) ||
                byte[].class.isAssignableFrom(type);
    }

    public Object toTarantoolNativeType(Object value, Class<?> targetType) {
        if (value instanceof Boolean ||
            value instanceof Number ||
            value instanceof String ||
            value instanceof byte[]
        ) {
            return value;
        }

        if (ClassUtils.isAssignable(Boolean.class, targetType)) {
            return conversionService.convert(value, Boolean.class);
        } else if (ClassUtils.isAssignable(Number.class, targetType)) {
            return conversionService.convert(value, Number.class);
        } else if (ClassUtils.isAssignable(String.class, targetType)) {
            return conversionService.convert(value, String.class);
        } else if (ClassUtils.isAssignable(byte[].class, targetType)) {
            return conversionService.convert(value, byte[].class);
        } else {
            throw new IllegalStateException("Unknown targetType: " + targetType);
        }
    }

    public Object fromTarantoolNativeType(Object value, Class<?> typeToUse) {
        return conversionService.convert(value, typeToUse);
    }

    @Override
    public void afterPropertiesSet() {
        this.initializeConverters();
    }

    private void initializeConverters() {
        customConversions.registerConvertersIn(conversionService);
    }

    private static class TarantoolTypeAliasAccessor implements TypeAliasAccessor<TarantoolData> {
        private final int typeIndex;

        private final ConversionService conversionService;

        public TarantoolTypeAliasAccessor(ConversionService conversionService) {
            this(conversionService, TYPE_HINT_INDEX);
        }

        public TarantoolTypeAliasAccessor(ConversionService conversionService, int typeIndex) {
            this.typeIndex = typeIndex;
            this.conversionService = conversionService;
        }

        @Override
        public Object readAliasFrom(TarantoolData source) {
            return conversionService.convert(source.getTuple().get(Path.of(typeIndex)), String.class);
        }

        @Override
        public void writeTypeTo(TarantoolData sink, Object alias) {
            sink.getTuple().set(Path.of(typeIndex), conversionService.convert(alias, String.class));
        }
    }

    /**
     * @author Christoph Strobl
     */
    private static class ConverterAwareParameterValueProvider
            implements PropertyValueProvider<TarantoolPersistentProperty> {
        private static final int OFFSET = 1;

        private final TarantoolData source;
        private final ConversionService conversionService;

        public ConverterAwareParameterValueProvider(TarantoolData source, ConversionService conversionService) {
            this.source = source;
            this.conversionService = conversionService;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getPropertyValue(TarantoolPersistentProperty property) {
            final OptionalInt optTupleIndex = property.getTupleIndex();
            if(!optTupleIndex.isPresent()) {
                // TODO: is it right?
                return (T) conversionService.convert(null, property.getActualType());
            }

            int tupleIndex = optTupleIndex.getAsInt();

            return (T) conversionService.convert(source.getTuple().get(Path.of(tupleIndex)), property.getActualType());
        }
    }
}
