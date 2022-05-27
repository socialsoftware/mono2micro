package pt.ist.socialsoftware.mono2micro.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Cut;
import pt.ist.socialsoftware.mono2micro.domain.StaticCollection;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CutDeserializer extends StdDeserializer<Cut> {

    public CutDeserializer() {
        this(null);
    }

    public CutDeserializer(Class<StaticCollection> t) {
        super(t);
    }

    public Cut deserialize(
            JsonParser jsonParser,
            DeserializationContext ctxt
    ) throws IOException {
//        JsonToken jsonToken = jsonParser.currentToken();


        Cut cut = new Cut();

        while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                String controllerName = jsonParser.getCurrentName();
                ArrayList<ReducedTraceElementDto> accesses = new ArrayList<>();
//                System.out.println(controllerName);

                while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
                    String clusterId = jsonParser.getCurrentName();
                    Cluster newCluster = new Cluster(Short.valueOf(clusterId), clusterId);
                    while (jsonParser.nextValue() != JsonToken.END_ARRAY) { // iterate over cluster entities
                        newCluster.addEntity(jsonParser.getShortValue());
                    }
                    cut.addNewCluster(newCluster);
                }
            }
        }

        return cut;
    }
}
