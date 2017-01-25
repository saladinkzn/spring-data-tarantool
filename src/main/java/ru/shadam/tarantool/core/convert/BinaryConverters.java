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
    static final class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {

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
