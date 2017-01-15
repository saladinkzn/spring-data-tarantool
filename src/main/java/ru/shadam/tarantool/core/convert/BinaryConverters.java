package ru.shadam.tarantool.core.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.Date;

/**
 * @author sala
 */
final class BinaryConverters {
    @WritingConverter
    static class EnumToStringConverter implements Converter<Enum<?>, String> {

        @Override
        public String convert(Enum<?> source) {

            if (source == null) {
                return null;
            }

            return source.toString();
        }
    }

    /**
     * @author Christoph Strobl
     * @since 1.7
     */
    @ReadingConverter
    static final class StringToEnumConverterFactory implements ConverterFactory<byte[], Enum<?>> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T extends Enum<?>> Converter<byte[], T> getConverter(Class<T> targetType) {

            Class<?> enumType = targetType;
            while (enumType != null && !enumType.isEnum()) {
                enumType = enumType.getSuperclass();
            }
            if (enumType == null) {
                throw new IllegalArgumentException("The target type " + targetType.getName() + " does not refer to an enum");
            }
            return new StringToEnum(enumType);
        }

        /**
         * @author Christoph Strobl
         * @since 1.7
         */
        private class StringToEnum<T extends Enum<T>> implements Converter<String, T> {

            private final Class<T> enumType;

            public StringToEnum(Class<T> enumType) {
                this.enumType = enumType;
            }

            @Override
            public T convert(String source) {
                if (source == null || source.length() == 0) {
                    return null;
                }
                return Enum.valueOf(this.enumType, source.trim());
            }
        }
    }

    /**
     * @author Christoph Strobl
     * @since 1.7
     */
    @WritingConverter
    static class BooleanToNumberConverter implements Converter<Boolean, Number> {

        final int _true = 1;
        final int _false = 0;

        @Override
        public Number convert(Boolean source) {

            if (source == null) {
                return null;
            }

            return source ? _true : _false;
        }
    }

    /**
     * @author Christoph Strobl
     * @since 1.7
     */
    @ReadingConverter
    static class NumberToBooleanConverter implements Converter<Number, Boolean> {

        @Override
        public Boolean convert(Number source) {

            if (source == null) {
                return null;
            }

            return (source.intValue() == 1) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    /**
     * @author Christoph Strobl
     * @since 1.7
     */
    @WritingConverter
    static class DateToNumberConverter implements Converter<Date, Number> {

        @Override
        public Number convert(Date source) {

            if (source == null) {
                return null;
            }

            return source.getTime();
        }
    }

    /**
     * @author Christoph Strobl
     * @since 1.7
     */
    @ReadingConverter
    static class NumberToDateConverter implements Converter<Number, Date> {

        @Override
        public Date convert(Number source) {

            if (source == null) {
                return null;
            }

            return new Date(source.longValue());
        }
    }
}
