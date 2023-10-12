package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.test.context.junit4.SpringRunner;

import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIteratorTests;

@RunWith(SpringRunner.class)
@SuiteClasses({Access.class, Call.class, If.class, Label.class, Loop.class})
public class NodeToGraphTests {

	TraceGraph traceGraph;
	Graph<AccessDto, DefaultWeightedEdge> processedSubTrace;


	@Before
	public void setUp() throws Exception {
        traceGraph = new TraceGraph();
		processedSubTrace = traceGraph.getGraph();
    }

	// Access

	@Test  
    public void nodeToAccessGraph_Access_EmptyTrace(){  
		String mode = "R";
		int entityAccessedId = 19;

        Access access = createAccess(mode, entityAccessedId);

		access.nodeToAccessGraph(traceGraph, null, null, null, null);

		assertEquals(1, processedSubTrace.vertexSet().size());
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte(mode), traceGraph.toList().get(0).getMode());
		assertEquals(entityAccessedId, traceGraph.toList().get(0).getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(traceGraph.toList().get(0), a)).toList().isEmpty());

    }

	@Test  
    public void nodeToAccessGraph_Access_NonEmptyTrace(){  
		String mode = "R";
		int entityAccessedId = 19;

        Access access1 = new Access(-1);
		access1.setMode(mode);
		access1.setEntityAccessedId(entityAccessedId);

		Access access2 = new Access(-1);
		access2.setMode(mode);
		access2.setEntityAccessedId(entityAccessedId);

		access1.nodeToAccessGraph(traceGraph, null, null, null, null);
		access2.nodeToAccessGraph(traceGraph, null, null, null, null);

		assertEquals(2, processedSubTrace.vertexSet().size());
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte(mode), traceGraph.toList().get(0).getMode());
		assertEquals(entityAccessedId, traceGraph.toList().get(0).getEntityID());
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte(mode), traceGraph.toList().get(1).getMode());
		assertEquals(entityAccessedId, traceGraph.toList().get(1).getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(traceGraph.toList().get(0), a)).toList().contains(traceGraph.toList().get(1)));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(traceGraph.toList().get(1), a)).toList().isEmpty());
		
    }

	// Call

	@Test  
    public void nodeToAccessGraph_Call_EmptyTraceSingleCall() throws JSONException{
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&call", 1}
			},
			new Object[][]{
				new Object[]{"R", 6}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		callNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(processedSubTrace.vertexSet().size(), 3); // entry + 1 access + exit = 3

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(2);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access = traceGraph.toList().get(1); // actual access
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte("R"), access.getMode());
		assertEquals(6, access.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access, a)).toList().contains(exitPoint));
		
		
    }

	@Test
	public void nodeToAccessGraph_Call_EmptyTraceMultipleCalls() throws JSONException{
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&call", 1}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"R", 6}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		callNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(3);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access1 = traceGraph.toList().get(1);
		AccessDto access2 = traceGraph.toList().get(2);
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte("R"), access1.getMode());
		assertEquals(6, access1.getEntityID());
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte("R"), access2.getMode());
		assertEquals(6, access2.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access1));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access1, a)).toList().contains(access2));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access2, a)).toList().contains(exitPoint));
		
		
    }

	@Test  
    public void nodeToAccessGraph_Call_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = createAccess("W", 17);
		Access access2 = createAccess("R", 20);
		Access access3 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{"&call", 1}, new Object[]{access2.getMode(), access2.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access3.getMode(), access3.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(1);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);
		
		// steps
		access1.nodeToAccessGraph(traceGraph, null, null, null, null);
		callNode.nodeToAccessGraph(traceGraph, null, null, null, null);
		access2.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(5, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(1);
		AccessDto exitPoint = traceGraph.toList().get(3);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		
		AccessDto access = traceGraph.toList().get(0);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access, a)).toList().contains(entryPoint)); // check if entry connected properly

		AccessDto access_1 = traceGraph.toList().get(2);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access_1));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access_1, a)).toList().contains(exitPoint)); // check if center access is connected

		AccessDto access_2 = traceGraph.toList().get(4);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().contains(access_2)); // check if exit connected properly
		
    }

	@Test  
    public void nodeToAccessGraph_Call_Nested() throws JSONException{
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&call", 1}
			},
			new Object[][]{
				new Object[]{"&call", 2}
			},
			new Object[][]{
				new Object[]{"R", 6}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		callNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(processedSubTrace.vertexSet().size(), 5);

		AccessDto outerEntryPoint = traceGraph.toList().get(0);
		AccessDto outerExitPoint = traceGraph.toList().get(4);
		assertEquals(-1, outerEntryPoint.getEntityID());
		assertEquals(-1, outerExitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(outerExitPoint, a)).toList().isEmpty());

		AccessDto innerEntryPoint = traceGraph.toList().get(1);
		AccessDto innerExitPoint = traceGraph.toList().get(3);
		assertEquals(-1, innerEntryPoint.getEntityID());
		assertEquals(-1, innerExitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(innerExitPoint, a)).toList().contains(outerExitPoint));
		
		AccessDto access = traceGraph.toList().get(2); // actual access
		assertEquals(FunctionalityGraphTracesIterator.accessModeStringToByte("R"), access.getMode());
		assertEquals(6, access.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(innerEntryPoint, a)).toList().contains(access));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access, a)).toList().contains(innerExitPoint));
		
		
    }

	// If

	@Test  
    public void nodeToAccessGraph_If_EmptyTrace() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(5, processedSubTrace.vertexSet().size()); // entry + 3 access + exit = 5

		AccessDto entryPoint = traceGraph.getFirstAccess();
		AccessDto exitPoint = traceGraph.getLastAccess();
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 1; i < 4; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access));

				condition = access;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(exitPoint));
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{"&if", 1}, new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(1);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		access1.nodeToAccessGraph(traceGraph, null, null, null, null);
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);
		access1.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(7, processedSubTrace.vertexSet().size());

		List<AccessDto> a1 = traceGraph.toList();
		AccessDto entryPoint = traceGraph.toList().get(1);
		AccessDto exitPoint = traceGraph.toList().get(5);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 2; i < 5; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access));
				condition = access;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(exitPoint));
			}
			
		}

		access = traceGraph.toList().get(0);
		final AccessDto acc = access;
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(entryPoint));

		access = traceGraph.toList().get(6);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().contains(access));
		
    }

	@Test  
    public void nodeToAccessGraph_If_Nested() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 7}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{"&if", 4}
			},
			new Object[][]{
				new Object[]{"&condition", 5}, new Object[]{"&then", 6}, new Object[]{"&else", 8}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(9, processedSubTrace.vertexSet().size());

		AccessDto outerEntryPoint = traceGraph.toList().get(0);
		AccessDto outerExitPoint = traceGraph.toList().get(8);
		assertEquals(-1, outerEntryPoint.getEntityID());
		assertEquals(-1, outerExitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(outerExitPoint, a)).toList().isEmpty());

		AccessDto innerEntryPoint = traceGraph.toList().get(2);
		AccessDto innerExitPoint = traceGraph.toList().get(6);
		assertEquals(-1, innerEntryPoint.getEntityID());
		assertEquals(-1, innerExitPoint.getEntityID());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 3; i < 5; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(innerEntryPoint, a)).toList().contains(access));
				condition = access;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(innerExitPoint));
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NoCond() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(3);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 0; i < 3; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				condition = entryPoint;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(exitPoint));
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NoThen() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(3);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 1; i < 3; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access));
				condition = access;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(exitPoint));
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NoElse() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 4}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(3);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 1; i < 3; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access));
				condition = access;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(exitPoint));
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NoThenOrElse() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(3, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(2);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto access;
		AccessDto condition = null;

		for (int i = 1; i < 2; i++) {
			access = traceGraph.toList().get(i);
			if(condition == null) {
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(entryPoint, a)).toList().contains(access));
				condition = access;
			} else {
				final AccessDto cond = condition;
				final AccessDto acc = access;
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(cond, a)).toList().contains(access));
				assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(acc, a)).toList().contains(exitPoint));
			}
			
		}
		
		
    }

	// Label

	/*@Test  
    public void testst() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{"R", 0}, new Object[]{"#zero_comparison"}
			},
			new Object[][]{
				new Object[]{"&call", 5}, new Object[]{"R", 17}
			},
			new Object[][]{
				new Object[]{"R", 2}
			},
			new Object[][]{
				new Object[]{"&call", 6}
			},
			new Object[][]{
				new Object[]{"R", 1}, new Object[]{"#return"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(10, processedSubTrace.vertexSet().size());

		TraceGraph subGraph = new TraceGraph(processedSubTrace, traceGraph.toList().get(0));
		subGraph.removeEmptyNodes();

		assertTrue(heuristicFlags.hasReturn);

		AccessDto exitPoint = traceGraph.toList().get(2);		
		Access access = traceGraph.toList().get(1);
		assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint));
		
		
    }*/

	@Test  
    public void nodeToAccessGraph_Label_NormalReturn() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&call", 1}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#return"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		callNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(3, processedSubTrace.vertexSet().size());

		AccessDto exitPoint = traceGraph.toList().get(2);		
		AccessDto access = traceGraph.toList().get(1);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access, a)).toList().contains(exitPoint));
		
		
    }

	@Test  
    public void nodeToAccessGraph_Label_NormalReturnNested() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&call", 1}
			},
			new Object[][]{
				new Object[]{"&call", 2}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#return"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		callNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(5, processedSubTrace.vertexSet().size());

		AccessDto exitPoint = traceGraph.toList().get(3);		
		AccessDto access = traceGraph.toList().get(2);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access, a)).toList().contains(exitPoint));
		
		
    }

	@Test  
    public void nodeToAccessGraph_Label_NormalContinue() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#continue"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		AccessDto expressionStart = traceGraph.toList().get(0);		
		AccessDto bodyEnd = traceGraph.toList().get(2);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(bodyEnd, a)).toList().contains(expressionStart));
			
    }

	@Test  
    public void nodeToAccessGraph_Label_NestedContinue() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"&for_loop", 4}
			},
			new Object[][]{
				new Object[]{"&expression", 5}, new Object[]{"&body", 6}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#continue"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(7, processedSubTrace.vertexSet().size());

		AccessDto innerExpressionStart = traceGraph.toList().get(2);		
		AccessDto innerBodyEnd = traceGraph.toList().get(4);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(innerBodyEnd, a)).toList().contains(innerExpressionStart));
		
    }

	@Test  
    public void nodeToAccessGraph_Label_NormalBreak() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#break"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		AccessDto loopEnd = traceGraph.toList().get(3);		
		AccessDto bodyEnd = traceGraph.toList().get(2);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(bodyEnd, a)).toList().contains(loopEnd));
			
    }

	@Test  
    public void nodeToAccessGraph_Label_NestedBreak() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"&for_loop", 4}
			},
			new Object[][]{
				new Object[]{"&expression", 5}, new Object[]{"&body", 6}
			},
			new Object[][]{
				new Object[]{"R", 6}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#break"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);
		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(7, processedSubTrace.vertexSet().size());

		AccessDto innerLoopEnd = traceGraph.toList().get(5);		
		AccessDto innerBodyEnd = traceGraph.toList().get(4);
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(innerBodyEnd, a)).toList().contains(innerLoopEnd));
		
    }

	@Test
	public void nodeToAccessGraph_Label_FlagReturn() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&if", 1}
			},
			new Object[][]{
				new Object[]{"&condition", 2}, new Object[]{"&then", 3}, new Object[]{"&else", 4}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{"#return"}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		If ifNode = new If(totalTrace, totalTraceArray, traceElementJSON);

		HeuristicFlags heuristicFlags = new HeuristicFlags();

		// steps
		ifNode.nodeToAccessGraph(traceGraph, null, null, null, heuristicFlags);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size());

		double probability = BranchHeuristics.heuristicProbabilities.get(BranchHeuristics.RETURN_H);
		double inverse = 1.0f - probability;
		
		assertEquals(probability, processedSubTrace.getEdgeWeight(processedSubTrace.getEdge(traceGraph.toList().get(1), traceGraph.toList().get(2))), 0);
		assertEquals(inverse, processedSubTrace.getEdgeWeight(processedSubTrace.getEdge(traceGraph.toList().get(1), traceGraph.toList().get(3))), 0);
		
    }

	// Loop

	@Test  
    public void nodeToAccessGraph_Loop_EmptyTrace() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(4, processedSubTrace.vertexSet().size()); // entry + 2 access + exit = 4

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(3);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto expression = traceGraph.toList().get(1);
		AccessDto body = traceGraph.toList().get(2);

		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(expression, a)).toList().contains(body));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(expression, a)).toList().contains(exitPoint));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(body, a)).toList().contains(expression));
		assertFalse(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(body, a)).toList().contains(exitPoint));
		
    }

	@Test  
    public void nodeToAccessGraph_Loop_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(1);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		access1.nodeToAccessGraph(traceGraph, null, null, null, null);
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(5, processedSubTrace.vertexSet().size()); // entry + 2 access + exit = 4

		AccessDto access_1 = traceGraph.toList().get(0);
		AccessDto entryPoint = traceGraph.toList().get(1);
		AccessDto exitPoint = traceGraph.toList().get(4);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(access_1, a)).toList().contains(entryPoint));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto expression = traceGraph.toList().get(2);
		AccessDto body = traceGraph.toList().get(3);

		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(expression, a)).toList().contains(body));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(expression, a)).toList().contains(exitPoint));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(body, a)).toList().contains(expression));
			
    }

	@Test  
    public void nodeToAccessGraph_Loop_NoExpr() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(3, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(2);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());

		AccessDto body = traceGraph.toList().get(1);

		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(body, a)).toList().contains(body));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(body, a)).toList().contains(exitPoint));
	
    }

	@Test  
    public void nodeToAccessGraph_Loop_NoBody() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(3, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(2);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());
		
		AccessDto expression = traceGraph.toList().get(1);

		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(expression, a)).toList().contains(expression));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(expression, a)).toList().contains(exitPoint));
		
    }

	@Test  
    public void nodeToAccessGraph_Loop_MultipleAccesses() throws JSONException{
		// setup
		Access access1 = createAccess("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&for_loop", 1}
			},
			new Object[][]{
				new Object[]{"&expression", 2}, new Object[]{"&body", 3}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Loop loopNode = new Loop(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		loopNode.nodeToAccessGraph(traceGraph, null, null, null, null);

		// result
		assertEquals(6, processedSubTrace.vertexSet().size());

		AccessDto entryPoint = traceGraph.toList().get(0);
		AccessDto exitPoint = traceGraph.toList().get(5);
		assertEquals(-1, entryPoint.getEntityID());
		assertEquals(-1, exitPoint.getEntityID());
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(exitPoint, a)).toList().isEmpty());

		AccessDto prev = null;
		AccessDto current;

		for (int i = 0; i < processedSubTrace.vertexSet().size()-1; i++) {
			if (prev == null) continue;

			current = traceGraph.toList().get(i);

			final AccessDto p = prev;
			assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(p, a)).toList().contains(current));

			prev = current;
		}

		AccessDto firstExpressionAccess = traceGraph.toList().get(1);
		AccessDto lastExpressionAccess = traceGraph.toList().get(2);
		AccessDto lastBodyAccess = traceGraph.toList().get(4);

		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(lastExpressionAccess, a)).toList().contains(exitPoint));
		assertTrue(processedSubTrace.vertexSet().stream().filter(a -> processedSubTrace.containsEdge(lastBodyAccess, a)).toList().contains(firstExpressionAccess));
		
    }

	
	// Utils

	public static JSONObject initializeBaseTrace(Object[][][] traceList) throws JSONException{  
		JSONObject totalTrace = new JSONObject();

		Integer id = 0;

		for (Object[][] trace : traceList) {
			totalTrace.accumulate("t", createTrace(
								id++, 
								trace
								));
		}

		return totalTrace;
		
    }

	static JSONObject createTrace(Integer id, Object[][] accesses) throws JSONException {
		JSONObject trace = new JSONObject();
		JSONArray accessList = new JSONArray();
		JSONArray access;

		for (Object[] accessPair : accesses) {
			access = new JSONArray().put(accessPair[0]);
			if(accessPair.length > 1) {
				access.put(accessPair[1]);
			}
			
			accessList.put(access);
		}

		return trace.accumulate("id", id).accumulate("a", accessList);
	}

	public static Access createAccess(String mode, Integer entityId) {
		Access a = new Access(-1);
		a.setEntityAccessedId(entityId);
		a.setMode(mode);
		return a;
	}



}

