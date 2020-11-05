package util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.serializer.TraceSerializer;

import java.util.List;

@JsonSerialize(using = TraceSerializer.class)
public class Trace {
    private int id;
    private List<Access> a;

    public Trace(int id, List<Access> a) {
        this.id = id;
        this.a = a;
    }

    public void addAccess(Access a) {
        this.a.add(a);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Access> getA() {
        return a;
    }

    public void setA(List<Access> a) {
        this.a = a;
    }
}
