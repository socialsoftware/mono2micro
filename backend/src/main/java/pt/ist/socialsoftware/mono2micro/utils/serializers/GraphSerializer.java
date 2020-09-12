package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jgrapht.Graphs.successorListOf;

public class GraphSerializer extends StdSerializer<DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge>> {

    public GraphSerializer() {
            this(null);
    }

    public GraphSerializer(Class<DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge>> t) {
        super(t);
    }

    @Override
    public void serialize(
            DirectedAcyclicGraph<Controller.LocalTransaction, DefaultEdge> graph,
            JsonGenerator jsonGenerator,
            SerializerProvider provider
    ) throws IOException {

        List<Controller.LocalTransaction> nodes = new ArrayList<>();
        List<String> links = new ArrayList<>();
        Iterator<Controller.LocalTransaction> iterator = graph.iterator();
        while (iterator.hasNext()) {
            Controller.LocalTransaction lt = iterator.next();

            List<Controller.LocalTransaction> ltChildren = successorListOf(graph, lt);
            for (Controller.LocalTransaction ltC : ltChildren)
                links.add(lt.getId() + "->" + ltC.getId());

            nodes.add(lt);
        }

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("nodes", nodes);
        jsonGenerator.writeObjectField("links", links);
        jsonGenerator.writeEndObject();
    }
}
