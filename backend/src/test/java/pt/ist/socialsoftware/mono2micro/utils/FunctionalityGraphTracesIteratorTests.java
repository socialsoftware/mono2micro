package pt.ist.socialsoftware.mono2micro.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;
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
        trace.add(new Access("R", 1));
        trace.add(new Access("W", 2));
        trace.add(new Access("W", 3));

        // steps
        TraceGraph graph = FunctionalityGraphTracesIterator.processSubTrace(trace);

        // result
        List<Access> allAccesses = graph.getAllAccesses();
        assertEquals(3, allAccesses.size());

        Access prev = null;
        Access curr = null;
        for (int i = 0; i < allAccesses.size(); i++) {
            if(prev == null) continue;

            curr = allAccesses.get(i);
            assertTrue(prev.getNextAccessProbabilities().containsKey(curr));

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

        
        JSONObject mainTrace = trace.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = FunctionalityGraphTracesIterator.translateSubTrace(trace, mainTrace);

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.processSubTrace(preProcessedTraces);

        PathData pathData = FunctionalityGraphTracesIterator.computeTraceTypes(processedSubTrace.getFirstAccess(), new HashMap<Access, PathData>(), new ArrayList<>());

        assertEquals(3, pathData.getLongestPath().size());

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

        
        JSONObject mainTrace = trace.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = FunctionalityGraphTracesIterator.translateSubTrace(trace, mainTrace);

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.processSubTrace(preProcessedTraces);

        PathData pathData = FunctionalityGraphTracesIterator.computeTraceTypes(processedSubTrace.getFirstAccess(), new HashMap<Access, PathData>(), new ArrayList<>());

        assertEquals(8, pathData.getLongestPath().size());

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

        
        JSONObject mainTrace = trace.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = FunctionalityGraphTracesIterator.translateSubTrace(trace, mainTrace);

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.processSubTrace(preProcessedTraces);

        PathData pathData = FunctionalityGraphTracesIterator.computeTraceTypes(processedSubTrace.getFirstAccess(), new HashMap<Access, PathData>(), new ArrayList<>());

        
        TraceDto t = FunctionalityGraphTracesIterator.pathDataAccessListToTraceDto(pathData.getLongestPath());

        assertEquals(16, pathData.getLongestPath().size());

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

        
        JSONObject mainTrace = trace.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = FunctionalityGraphTracesIterator.translateSubTrace(trace, mainTrace);

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.processSubTrace(preProcessedTraces);

        PathData pathData = FunctionalityGraphTracesIterator.computeTraceTypes(processedSubTrace.getFirstAccess(), new HashMap<Access, PathData>(), new ArrayList<>());

        assertEquals(7, pathData.getLongestPath().size());

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
				new Object[]{"W", 8}, new Object[]{"W", 8}
			}
			});

        
        JSONObject mainTrace = trace.getJSONArray("t").getJSONObject(0);

        List<TraceGraphNode> preProcessedTraces = FunctionalityGraphTracesIterator.translateSubTrace(trace, mainTrace);

        TraceGraph processedSubTrace = FunctionalityGraphTracesIterator.processSubTrace(preProcessedTraces);

        PathData pathData = FunctionalityGraphTracesIterator.computeTraceTypes(processedSubTrace.getFirstAccess(), new HashMap<Access, PathData>(), new ArrayList<>());

        assertEquals(11, pathData.getLongestPath().size());
        assertEquals(9, pathData.getMostDifferentAccessesPath().size());
        assertEquals(11, pathData.getMostProbablePath().size());

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
				new Object[]{"W", -4}, new Object[]{"W", -5}
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

        PathData pathData = FunctionalityGraphTracesIterator.computeTraceTypes(processedSubTrace.getFirstAccess(), new HashMap<Access, PathData>(), new ArrayList<>());

        
        Map<String, Float> e1e2PairCount = new HashMap<>();
        Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>();

        FunctionalityGraphTracesIterator.fillEntityDataStructures(processedSubTrace.getFirstAccess(), new ArrayList<>(), 1f, e1e2PairCount, entityFunctionalities, "requestedFunctionality");

        assertEquals(10, e1e2PairCount.size());
        assertEquals(10, entityFunctionalities.size());        

    }
}
