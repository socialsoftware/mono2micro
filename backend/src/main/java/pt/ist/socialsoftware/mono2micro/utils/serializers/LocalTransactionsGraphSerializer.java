package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.jgrapht.Graphs.successorListOf;

public class LocalTransactionsGraphSerializer extends StdSerializer<DirectedAcyclicGraph<Decomposition.LocalTransaction, DefaultEdge>> {

    public LocalTransactionsGraphSerializer() {
            this(null);
    }

    public LocalTransactionsGraphSerializer(Class<DirectedAcyclicGraph<Decomposition.LocalTransaction, DefaultEdge>> t) {
        super(t);
    }

    @Override
    public void serialize(
            DirectedAcyclicGraph<Decomposition.LocalTransaction, DefaultEdge> graph,
            JsonGenerator jsonGenerator,
            SerializerProvider provider
    ) throws IOException {

        List<Decomposition.LocalTransaction> nodes = new ArrayList<>();
        List<String> links = new ArrayList<>();
        Iterator<Decomposition.LocalTransaction> iterator = graph.iterator();

        while (iterator.hasNext()) {
            Decomposition.LocalTransaction lt = iterator.next();

            List<Decomposition.LocalTransaction> ltChildren = successorListOf(graph, lt);
            for (Decomposition.LocalTransaction ltC : ltChildren)
                links.add(lt.getId() + "->" + ltC.getId());

            nodes.add(lt);
        }

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("nodes", nodes);
        jsonGenerator.writeObjectField("links", links);
        jsonGenerator.writeEndObject();
    }
}
