package ru.shadam.tarantool.core.convert;

/**
 * @author sala
 */
public class TarantoolData {
    private Object id;

    private Tuple tuple;

    public TarantoolData() {
        this.tuple = new Tuple();
    }

    public TarantoolData(Tuple tuple) {
        this.tuple = tuple;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "TarantoolData{" +
                ", id=" + id +
                ", tuple=" + tuple +
                '}';
    }

    public Tuple getTuple() {
        return tuple;
    }
}
