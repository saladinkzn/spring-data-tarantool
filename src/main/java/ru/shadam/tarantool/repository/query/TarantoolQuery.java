package ru.shadam.tarantool.repository.query;

/**
 * @author sala
 */
public class TarantoolQuery {
    private String key;
    private Object value;

    public TarantoolQuery(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TarantoolQuery that = (TarantoolQuery) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "TarantoolQuery{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
