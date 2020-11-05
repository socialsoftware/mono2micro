package pt.ist.socialsoftware.mono2micro.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
