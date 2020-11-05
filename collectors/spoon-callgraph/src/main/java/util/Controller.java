package util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.serializer.ControllerSerializer;

import java.util.List;

@JsonSerialize(using = ControllerSerializer.class)
public class Controller {

    private List<Trace> t;

    public Controller(List<Trace> t) {
        this.t = t;
    }

    public List<Trace> getT() {
        return t;
    }

    public void setT(List<Trace> t) {
        this.t = t;
    }
}
