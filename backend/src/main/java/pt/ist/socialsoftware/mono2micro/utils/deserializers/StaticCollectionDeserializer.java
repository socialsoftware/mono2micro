package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.StaticCollection;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StaticCollectionDeserializer extends StdDeserializer<StaticCollection> {

    public StaticCollectionDeserializer() {
        this(null);
    }

    public StaticCollectionDeserializer(Class<StaticCollection> t) {
        super(t);
    }

    public StaticCollection deserialize(
            JsonParser jsonParser,
            DeserializationContext ctxt
    ) throws IOException {
//        JsonToken jsonToken = jsonParser.currentToken();


        StaticCollection collection = new StaticCollection();

        while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                String controllerName = jsonParser.getCurrentName();
                ArrayList<ReducedTraceElementDto> accesses = new ArrayList<>();
//                System.out.println(controllerName);

                while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
                    switch (jsonParser.getCurrentName()) {
                        case "f":
                            break;
                        case "t": // array of traces
                            while (jsonParser.nextValue() != JsonToken.END_ARRAY) { // iterate over trace objects
                                while (jsonParser.nextValue() != JsonToken.END_OBJECT) { // iterate over trace object fields
                                    switch (jsonParser.getCurrentName()) {
                                        case "id":
                                        case "f":
                                            break;
                                        case "a":
                                            while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
                                                ReducedTraceElementDto rte = jsonParser.readValueAs(
                                                        ReducedTraceElementDto.class
                                                );
                                                accesses.add(rte);
                                            }
                                            break;

                                        default:
                                            Utils.print(
                                                    "Unexpected field name when parsing Trace: " + jsonParser.getCurrentName(),
                                                    Utils.lineno()
                                            );

                                            System.exit(-1);
                                    }
                                }
                            }
                            collection.addNewControllerAccesses(controllerName, accesses);
                            break;

                        default:
                            Utils.print(
                                    "Unexpected field name when parsing Static Collection: " + jsonParser.getCurrentName(),
                                    Utils.lineno()
                            );
                            System.exit(-1);
                    }
                }
            }
        }

        return collection;
    }
}
