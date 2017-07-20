package ru.shadam.tarantool.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;
import ru.shadam.tarantool.annotation.Tuple;

/**
 * @author sala
 */
@KeySpace("log_entry")
public class LogEntry {
    @Id
    @Tuple(index = 0)
    private String uid;

    private String text;

    protected LogEntry() {
    }

    public LogEntry(String uid, String text) {
        this.uid = uid;
        this.text = text;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
