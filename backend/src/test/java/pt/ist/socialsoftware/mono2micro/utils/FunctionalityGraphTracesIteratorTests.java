package pt.ist.socialsoftware.mono2micro.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.NodeToGraphTests;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraph;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.TraceGraphNode;

@RunWith(SpringRunner.class)
@SpringBootTest

public class FunctionalityGraphTracesIteratorTests {
    @Test  
    public void processSubTrace_SimpleTrace(){  
		// setup
        List<TraceGraphNode> trace = new ArrayList<TraceGraphNode>();
        trace.add(NodeToGraphTests.createAccess("R", 1));
        trace.add(NodeToGraphTests.createAccess("W", 2));
        trace.add(NodeToGraphTests.createAccess("W", 3));

        // steps
        TraceGraph graph = FunctionalityGraphTracesIterator.processSubTrace(trace);

        // longestPath
        List<AccessDto> allAccesses = graph.toList();
        assertEquals(3, allAccesses.size());

        AccessDto prev = null;
        AccessDto curr = null;
        for (int i = 0; i < allAccesses.size(); i++) {
            if(prev == null) continue;

            curr = allAccesses.get(i);
            assertTrue(graph.getGraph().containsEdge(prev, curr));

            prev = curr;
        }

    }

    @Test
    public void computeTraceTypes_SimpleTrace() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"W", 3}
			},
            new Object[][]{}
			});

        
        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        TraceDto longestPath = FunctionalityGraphTracesIterator.getLongestTrace(processedSubTrace.getGraph(), "funcName");

        assertEquals(3, longestPath.getAccesses().size());

    }

    @Test
    public void computeTraceTypes_SimpleIf() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"&if", 1}, new Object[]{"W", 7}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{"W", 3}
			},
			new Object[][]{
				new Object[]{"W", 4}, new Object[]{"W", 5}
            },
			new Object[][]{
				new Object[]{"W", 6}
			}
			});

        
        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        TraceDto longestPath = FunctionalityGraphTracesIterator.getLongestTrace(processedSubTrace.getGraph(), "funcName");

        assertEquals(6, longestPath.getAccesses().size());

    }

    @Test
    public void computeTraceTypes_ChainedIfs() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"&if", 1}, new Object[]{"W", 7}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{"W", 3}
			},
			new Object[][]{
				new Object[]{"W", 4}, new Object[]{"W", 5}, new Object[]{"&if", 4}
			},
			new Object[][]{
				new Object[]{"&condition", 5}, new Object[]{"&then", 6}
			},
            new Object[][]{
				new Object[]{"W", -4}, new Object[]{"W", -5}
			},
            new Object[][]{
				new Object[]{"&call", 7}
			},
            new Object[][]{
				new Object[]{"W", 8}, new Object[]{"W", 9}
			}
			});

        
        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        TraceDto longestPath = FunctionalityGraphTracesIterator.getLongestTrace(processedSubTrace.getGraph(), "funcName");

        assertEquals(10, longestPath.getAccesses().size());

    }

    @Test
    public void computeTraceTypes_Loop() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{"R", 1}, new Object[]{"R", 2}
			},
			new Object[][]{
				new Object[]{"R", 3}, new Object[]{"R", 4}
			}
			});

        
        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        TraceDto longestPath = FunctionalityGraphTracesIterator.getLongestTrace(processedSubTrace.getGraph(), "funcName");

        assertEquals(6, longestPath.getAccesses().size());

    }

    @Test
    public void computeTraceTypes_SimpleCall() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"&call", 1}, new Object[]{"W", 5}
			},
			new Object[][]{
				new Object[]{"W", 3}, new Object[]{"W", 4}
			}
			});

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        TraceDto longestPath = FunctionalityGraphTracesIterator.getLongestTrace(processedSubTrace.getGraph(), "funcName");

        assertEquals(5, longestPath.getAccesses().size());

    }

    @Test
    public void computeTraceTypes_CallIfReturn() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"&call", 1}, new Object[]{"W", 9}
			},
            new Object[][]{
				new Object[]{"&if", 2}, new Object[]{"W", 8}
			},
			new Object[][]{
				new Object[]{"&condition", 3}, new Object[]{"&then", 4}, new Object[]{"&else", 5}
			},
			new Object[][]{
				new Object[]{"W", 3}
			},
			new Object[][]{
				new Object[]{"W", 4}, new Object[]{"W", 5}, new Object[]{"#return"}
			},
			new Object[][]{
				new Object[]{"W", 8}, new Object[]{"W", 8}, new Object[]{"W", 8}
			}
			});

        
        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        TraceDto longestPath = FunctionalityGraphTracesIterator.getLongestTrace(processedSubTrace.getGraph(), "funcName");
        TraceDto mostDiffAccessesPath = FunctionalityGraphTracesIterator.getTraceWithMoreDifferentAccesses(processedSubTrace.getGraph(), "funcName");
        TraceDto mostProbablePath = FunctionalityGraphTracesIterator.getMostProbableTrace(processedSubTrace.getGraph(), "funcName");

        assertEquals(8, longestPath.getAccesses().size());
        assertEquals(7, mostDiffAccessesPath.getAccesses().size());
        assertEquals(8, mostProbablePath.getAccesses().size());

    }

    @Test
    public void fillEntityDataStructures_ChainedIfs() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"&if", 1}, new Object[]{"W", 7}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{"W", 3}
			},
			new Object[][]{
				new Object[]{"W", 4}, new Object[]{"W", 5}, new Object[]{"&if", 4}
			},
			new Object[][]{
				new Object[]{"&condition", 5}, new Object[]{"&then", 6}
			},
            new Object[][]{
				new Object[]{"W", 14}, new Object[]{"W", 15}
			},
            new Object[][]{
				new Object[]{"&call", 7}
			},
            new Object[][]{
				new Object[]{"W", 8}, new Object[]{"W", 9}
			}
			});

        
        JSONObject mainTrace = trace.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = FunctionalityGraphTracesIterator.translateSubTrace(trace, mainTrace);

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.processSubTrace(preProcessedTraces);

        
        Map<String, Float> e1e2PairCount = new HashMap<>();
        Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities = new HashMap<>();

        FunctionalityGraphTracesIterator.fillEntityDataStructures(processedSubTrace.getGraph(), e1e2PairCount, entityFunctionalities, "requestedFunctionality");

        assertEquals(22, e1e2PairCount.size());
        assertEquals(10, entityFunctionalities.size());        

    }

    @Test
    public void fillEntityDataStructures_Loop() throws JSONException {
        JSONObject trace = NodeToGraphTests.initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"W", 1}, new Object[]{"W", 2}, new Object[]{"&for_loop", 1}, new Object[]{"W", 8}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"R", 7}
			}
			});

        
        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.getFunctionalityTraceGraph(trace);

        
        Map<String, Float> e1e2PairCount = new HashMap<>();
        Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities = new HashMap<>();

        FunctionalityGraphTracesIterator.fillEntityDataStructures(processedSubTrace.getGraph(), e1e2PairCount, entityFunctionalities, "requestedFunctionality");
        

        assertEquals(8, e1e2PairCount.size());
        assertEquals(5, entityFunctionalities.size());        

    }
}
