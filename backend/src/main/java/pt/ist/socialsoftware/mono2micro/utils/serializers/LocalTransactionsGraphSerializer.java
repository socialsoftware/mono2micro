package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;

public class LocalTransactionsGraphSerializer extends StdSerializer<DirectedAcyclicGraph<LocalTransaction, DefaultEdge>> {

    public LocalTransactionsGraphSerializer() {
            this(null);
    }

    public LocalTransactionsGraphSerializer(Class<DirectedAcyclicGraph<LocalTransaction, DefaultEdge>> t) {
        super(t);
    }

    @Override
    public void serialize(
            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> graph,
            JsonGenerator jsonGenerator,
            SerializerProvider provider
    )
        throws IOException
    {

        Utils.GetSerializableLocalTransactionsGraphResult serializableLocalTransactionsGraph = Utils.getSerializableLocalTransactionsGraph(graph);

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("nodes", serializableLocalTransactionsGraph.getNodes());
        jsonGenerator.writeObjectField("links", serializableLocalTransactionsGraph.getLinks());
        jsonGenerator.writeEndObject();
    }
}
