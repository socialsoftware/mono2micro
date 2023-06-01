package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.util.JSON;

@RunWith(SpringRunner.class)
@SuiteClasses({Access.class, Call.class, If.class, Label.class, Loop.class})
public class NodeToGraphTests {

	List<Access> processedSubTrace;

	@Before
	public void setUp() throws Exception {
        processedSubTrace = new ArrayList<Access>();
    }

	// Access

	@Test  
    public void nodeToAccessGraph_Access_EmptyTrace(){  
		String mode = "R";
		int entityAccessedId = 19;

        Access access = new Access(mode, entityAccessedId);

		access.nodeToAccessGraph(processedSubTrace, null, null, null);

		assertEquals(1, processedSubTrace.size());
		assertEquals(access, processedSubTrace.get(0));
		assertEquals(true, access.getNextAccessProbabilities().keySet().isEmpty());

		assertEquals(mode, access.getMode());
		assertEquals(entityAccessedId, access.getEntityAccessedId());
    }

	@Test  
    public void nodeToAccessGraph_Access_NonEmptyTrace(){  
		String mode = "R";
		int entityAccessedId = 19;

        Access access1 = new Access();
		access1.setMode(mode);
		access1.setEntityAccessedId(entityAccessedId);

		Access access2 = new Access();
		access2.setMode(mode);
		access2.setEntityAccessedId(entityAccessedId);

		access1.nodeToAccessGraph(processedSubTrace, null, null, null);
		access2.nodeToAccessGraph(processedSubTrace, null, null, null);

		assertEquals(2, processedSubTrace.size());
		assertEquals(access1, processedSubTrace.get(0));
		assertEquals(access2, processedSubTrace.get(1));
		assertTrue(access1.getNextAccessProbabilities().containsKey(access2));
		assertTrue(access2.getNextAccessProbabilities().keySet().isEmpty());
		
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
		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(processedSubTrace.size(), 3); // entry + 1 access + exit = 3

		Access entryPoint = processedSubTrace.get(0);
		Access exitPoint = processedSubTrace.get(2);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		assertTrue(exitPoint.getNextAccessProbabilities().keySet().isEmpty());
		
		Access access = processedSubTrace.get(1); // actual access
		assertEquals("R", access.getMode());
		assertEquals(6, access.getEntityAccessedId());
		assertTrue(entryPoint.getNextAccessProbabilities().containsKey(access));
		assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint));
		
		
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
		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(4, processedSubTrace.size());

		Access entryPoint = processedSubTrace.get(0);
		Access exitPoint = processedSubTrace.get(3);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		assertTrue(exitPoint.getNextAccessProbabilities().keySet().isEmpty());
		
		Access access1 = processedSubTrace.get(1);
		Access access2 = processedSubTrace.get(2);
		assertEquals("R", access1.getMode());
		assertEquals(6, access1.getEntityAccessedId());
		assertEquals("R", access2.getMode());
		assertEquals(6, access2.getEntityAccessedId());
		assertTrue(entryPoint.getNextAccessProbabilities().containsKey(access1));
		assertTrue(access1.getNextAccessProbabilities().containsKey(access2));
		assertTrue(access2.getNextAccessProbabilities().containsKey(exitPoint));
		
		
    }

	@Test  
    public void nodeToAccessGraph_Call_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = new Access("W", 17);
		Access access2 = new Access("R", 20);
		Access access3 = new Access("R", 6);

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
		access1.nodeToAccessGraph(processedSubTrace, null, null, null);
		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);
		access2.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(5, processedSubTrace.size());

		Access entryPoint = processedSubTrace.get(1);
		Access exitPoint = processedSubTrace.get(3);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		
		Access access;
		access = processedSubTrace.get(0);
		assertTrue(access.getNextAccessProbabilities().containsKey(entryPoint)); // check if entry connected properly

		access = processedSubTrace.get(2);
		assertTrue(entryPoint.getNextAccessProbabilities().containsKey(access));
		assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint)); // check if center access is connected

		access = processedSubTrace.get(4);
		assertTrue(exitPoint.getNextAccessProbabilities().containsKey(access)); // check if exit connected properly
		
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
		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(processedSubTrace.size(), 5);

		Access outerEntryPoint = processedSubTrace.get(0);
		Access outerExitPoint = processedSubTrace.get(4);
		assertNull(outerEntryPoint.getMode());
		assertNull(outerExitPoint.getMode());
		assertTrue(outerExitPoint.getNextAccessProbabilities().keySet().isEmpty());

		Access innerEntryPoint = processedSubTrace.get(1);
		Access innerExitPoint = processedSubTrace.get(3);
		assertNull(innerEntryPoint.getMode());
		assertNull(innerExitPoint.getMode());
		assertTrue(innerExitPoint.getNextAccessProbabilities().containsKey(outerExitPoint));
		
		Access access = processedSubTrace.get(2); // actual access
		assertEquals("R", access.getMode());
		assertEquals(6, access.getEntityAccessedId());
		assertTrue(innerEntryPoint.getNextAccessProbabilities().containsKey(access));
		assertTrue(access.getNextAccessProbabilities().containsKey(innerExitPoint));
		
		
    }

	// If

	@Test  
    public void nodeToAccessGraph_If_EmptyTrace() throws JSONException{
		// setup
		Access access1 = new Access("R", 6);

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
		ifNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(5, processedSubTrace.size()); // entry + 3 access + exit = 5

		Access entryPoint = processedSubTrace.get(0);
		Access exitPoint = processedSubTrace.get(4);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		assertTrue(exitPoint.getNextAccessProbabilities().keySet().isEmpty());
		
		Access access;
		Access condition = null;

		for (int i = 1; i < 4; i++) {
			access = processedSubTrace.get(i);
			if(condition == null) {
				assertTrue(entryPoint.getNextAccessProbabilities().containsKey(access));
				condition = access;
			} else {
				assertTrue(condition.getNextAccessProbabilities().containsKey(access));
				assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint));
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = new Access("R", 6);

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
		access1.nodeToAccessGraph(processedSubTrace, null, null, null);
		ifNode.nodeToAccessGraph(processedSubTrace, null, null, null);
		access1.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(7, processedSubTrace.size());

		Access entryPoint = processedSubTrace.get(1);
		Access exitPoint = processedSubTrace.get(5);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		
		Access access;
		Access condition = null;

		for (int i = 2; i < 5; i++) {
			access = processedSubTrace.get(i);
			if(condition == null) {
				assertTrue(entryPoint.getNextAccessProbabilities().containsKey(access));
				condition = access;
			} else {
				assertTrue(condition.getNextAccessProbabilities().containsKey(access));
				assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint));
			}
			
		}

		access = processedSubTrace.get(0);
		assertTrue(access.getNextAccessProbabilities().containsKey(entryPoint));

		access = processedSubTrace.get(6);
		assertTrue(exitPoint.getNextAccessProbabilities().containsKey(access));
		
    }

	@Test  
    public void nodeToAccessGraph_If_Nested() throws JSONException{
		// setup
		Access access1 = new Access("R", 6);

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
		ifNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(9, processedSubTrace.size());

		Access outerEntryPoint = processedSubTrace.get(0);
		Access outerExitPoint = processedSubTrace.get(8);
		assertNull(outerEntryPoint.getMode());
		assertNull(outerExitPoint.getMode());
		assertTrue(outerExitPoint.getNextAccessProbabilities().keySet().isEmpty());

		Access innerEntryPoint = processedSubTrace.get(2);
		Access innerExitPoint = processedSubTrace.get(6);
		assertNull(innerEntryPoint.getMode());
		assertNull(innerExitPoint.getMode());
		
		Access access;
		Access condition = null;

		for (int i = 3; i < 5; i++) {
			access = processedSubTrace.get(i);
			if(condition == null) {
				assertTrue(innerEntryPoint.getNextAccessProbabilities().containsKey(access));
				condition = access;
			} else {
				assertTrue(condition.getNextAccessProbabilities().containsKey(access));
				assertTrue(access.getNextAccessProbabilities().containsKey(innerExitPoint));
			}
			
		}
		
		
    }

	// Label

	@Test  
    public void nodeToAccessGraph_Label_NormalReturn() throws JSONException {
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"&call", 1}
			},
			new Object[][]{
				new Object[]{"R", 6}, new Object[]{"#return"}, new Object[]{"R", 6}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(4, processedSubTrace.size());

		Access exitPoint = processedSubTrace.get(3);		
		Access access = processedSubTrace.get(1);
		assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint));
		
		
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
				new Object[]{"R", 6}, new Object[]{"#return"}, new Object[]{"R", 6}
			}
			});
		
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = totalTraceArray.getJSONObject(0).getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);

		// steps
		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);

		// result
		assertEquals(6, processedSubTrace.size());

		Access exitPoint = processedSubTrace.get(4);		
		Access access = processedSubTrace.get(2);
		assertTrue(access.getNextAccessProbabilities().containsKey(exitPoint));
		
		
    }

	// Loop


	
	// Utils

	public JSONObject initializeBaseTrace(Object[][][] traceList) throws JSONException{  
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

	JSONObject createTrace(Integer id, Object[][] accesses) throws JSONException {
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




}

