package ru.shadam.tarantool.core.convert;

/**
 * @author sala
 */
public class TarantoolData {
    private int spaceId;
    private Object id;

    private Tuple tuple;

    public TarantoolData() {
        this.tuple = new Tuple();
    }

    public TarantoolData(Tuple tuple) {
        this.tuple = tuple;
    }

    public int getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(int spaceId) {
        this.spaceId = spaceId;
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
                "spaceId=" + spaceId +
                ", id=" + id +
                ", tuple=" + tuple +
                '}';
    }

    public Tuple getTuple() {
        return tuple;
    }
}
