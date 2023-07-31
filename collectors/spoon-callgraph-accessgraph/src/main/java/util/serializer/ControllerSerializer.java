package util.serializer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import util.Controller;
import java.io.IOException;

public class ControllerSerializer extends StdSerializer<Controller> {
    public ControllerSerializer() {
        this(null);
    }

    public ControllerSerializer(Class<Controller> t) {
        super(t);
    }

    @Override
    public void serialize(
            Controller controller,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeStartObject();

        if (controller.getT().size() > 0) {
            jsonGenerator.writeObjectField("t", controller.getT());
        }

        jsonGenerator.writeEndObject();
    }
}