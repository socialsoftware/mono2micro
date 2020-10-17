package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jgrapht.Graphs.successorListOf;

public class LocalTransactionsGraphSerializer extends StdSerializer<DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge>> {

    public LocalTransactionsGraphSerializer() {
            this(null);
    }

    public LocalTransactionsGraphSerializer(Class<DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge>> t) {
        super(t);
    }

    @Override
    public void serialize(
            DirectedAcyclicGraph<Graph.LocalTransaction, DefaultEdge> graph,
            JsonGenerator jsonGenerator,
            SerializerProvider provider
    ) throws IOException {

        List<Graph.LocalTransaction> nodes = new ArrayList<>();
        List<String> links = new ArrayList<>();
        Iterator<Graph.LocalTransaction> iterator = graph.iterator();

        while (iterator.hasNext()) {
            Graph.LocalTransaction lt = iterator.next();

            List<Graph.LocalTransaction> ltChildren = successorListOf(graph, lt);
            for (Graph.LocalTransaction ltC : ltChildren)
                links.add(lt.getId() + "->" + ltC.getId());

            nodes.add(lt);
        }

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("nodes", nodes);
        jsonGenerator.writeObjectField("links", links);
        jsonGenerator.writeEndObject();
    }
}
