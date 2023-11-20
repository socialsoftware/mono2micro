package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.test.context.junit4.SpringRunner;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;

@RunWith(SpringRunner.class)
@SuiteClasses({Access.class, Call.class, If.class, Label.class, Loop.class})
public class TraceGraphTests {

	TraceGraph traceGraph;
	Graph<AccessDto, DefaultWeightedEdge> processedSubTrace;

	final double DOUBLE_DELTA = 0.01;


	@Before
	public void setUp() throws Exception {
        traceGraph = new TraceGraph();
		processedSubTrace = traceGraph.getGraph();
    }

	@After
	public void after(){
		// ensure that there is always only the root node without a predecessor
		assertEquals(1, processedSubTrace.vertexSet().stream().filter(v -> Graphs.predecessorListOf(processedSubTrace, v).size() == 0).toList().size());
		
	}

	@Test  
    public void traceGraph_cleanAuxiliaryNodes_simpleDirect(){  
		AccessDto a1 = new AccessDto();
		AccessDto a2 = new AccessDto();
		AccessDto a3 = new AccessDto();
		
		a1.setEntityID((short)1);
		a3.setEntityID((short)2);

		traceGraph.addEdge(a1, a2, 1);
		traceGraph.addEdge(a2, a3, 1);

		traceGraph.cleanAuxiliaryNodes();

		assertFalse(traceGraph.getGraph().containsVertex(a2));
		assertEquals(1d, traceGraph.getGraph().getEdgeWeight(traceGraph.getGraph().getEdge(a1, a3)), DOUBLE_DELTA);
	
	
	}

	@Test  
    public void traceGraph_cleanAuxiliaryNodes_simpleTwoPaths(){  
		AccessDto a1 = new AccessDto();
		AccessDto a2 = new AccessDto();
		AccessDto a3 = new AccessDto();
		
		a1.setEntityID((short)1);
		a3.setEntityID((short)2);

		traceGraph.addEdge(a1, a2, 0.5f);
		traceGraph.addEdge(a2, a3, 1);
		traceGraph.addEdge(a1, a3, 0.5f);

		traceGraph.cleanAuxiliaryNodes();

		assertFalse(traceGraph.getGraph().containsVertex(a2));
		assertEquals(1d, traceGraph.getGraph().getEdgeWeight(traceGraph.getGraph().getEdge(a1, a3)), DOUBLE_DELTA);
	
	
	}

	@Test  
    public void traceGraph_cleanAuxiliaryNodes_multipleMiddleNodes(){  
		AccessDto a1 = new AccessDto();
		AccessDto a2 = new AccessDto();
		AccessDto a3 = new AccessDto();
		AccessDto a4 = new AccessDto();
		
		a1.setEntityID((short)1);
		a4.setEntityID((short)2);

		traceGraph.addEdge(a1, a2, 1);
		traceGraph.addEdge(a2, a3, 1);
		traceGraph.addEdge(a3, a4, 1);

		traceGraph.cleanAuxiliaryNodes();

		assertFalse(traceGraph.getGraph().containsVertex(a2));
		assertFalse(traceGraph.getGraph().containsVertex(a3));
		assertEquals(1d, traceGraph.getGraph().getEdgeWeight(traceGraph.getGraph().getEdge(a1, a4)), DOUBLE_DELTA);
	
	
	}



}

