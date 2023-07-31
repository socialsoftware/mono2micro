package util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.serializer.LabelSerializer;

@JsonSerialize(using = LabelSerializer.class)
public class Label extends Access {
    private String text;

    public Label(String text) {
        super("", 0);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
