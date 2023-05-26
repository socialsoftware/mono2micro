package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
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

        Access access = new Access();
		access.setMode(mode);
		access.setEntityAccessedId(entityAccessedId);

		access.nodeToAccessGraph(processedSubTrace, null, null, null);

		assertEquals(processedSubTrace.size(), 1);
		assertEquals(processedSubTrace.get(0), access);
		assertEquals(access.getNextAccessProbabilities().keySet().isEmpty(), true);
		
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

		assertEquals(processedSubTrace.size(), 2);
		assertEquals(processedSubTrace.get(0), access1);
		assertEquals(processedSubTrace.get(1), access2);
		assertEquals(access1.getNextAccessProbabilities().containsKey(access2), true);
		assertEquals(access2.getNextAccessProbabilities().keySet().isEmpty(), true);
		
    }

	// Call

	@Test  
    public void nodeToAccessGraph_Call_EmptyTrace() throws JSONException{
		// setup
		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"call", 1}
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
		assertEquals(processedSubTrace.size(), 3); // call entry + 1 access + call exit = 3

		Access entryPoint = processedSubTrace.get(0);
		Access exitPoint = processedSubTrace.get(2);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		assertEquals(exitPoint.getNextAccessProbabilities().keySet().isEmpty(), true);
		
		Access access = processedSubTrace.get(1); // actual access
		assertEquals(access.getMode(), "R");
		assertEquals(access.getEntityAccessedId(), 6);
		assertEquals(entryPoint.getNextAccessProbabilities().containsKey(access), true);
		assertEquals(access.getNextAccessProbabilities().containsKey(exitPoint), true);
		
		
    }

	@Test  
    public void nodeToAccessGraph_Call_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = new Access("W", 17);
		Access access2 = new Access("R", 20);
		Access access3 = new Access("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{"call", 1}, new Object[]{access2.getMode(), access2.getEntityAccessedId()}
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
		assertEquals(processedSubTrace.size(), 5);

		Access entryPoint = processedSubTrace.get(1);
		Access exitPoint = processedSubTrace.get(3);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		
		Access access;
		access = processedSubTrace.get(0);
		assertEquals(access.getNextAccessProbabilities().containsKey(entryPoint), true); // check if entry connected properly

		access = processedSubTrace.get(2);
		assertEquals(entryPoint.getNextAccessProbabilities().containsKey(access), true);
		assertEquals(access.getNextAccessProbabilities().containsKey(exitPoint), true); // check if center access is connected

		access = processedSubTrace.get(4);
		assertEquals(exitPoint.getNextAccessProbabilities().containsKey(access), true); // check if exit connected properly
		
    }

	// If

	@Test  
    public void nodeToAccessGraph_If_EmptyTrace() throws JSONException{
		// setup
		Access access1 = new Access("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{"if", 1}
			},
			new Object[][]{
				new Object[]{"condition", 2}, new Object[]{"then", 3}, new Object[]{"else", 4}
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
		assertEquals(processedSubTrace.size(), 5); // entry + 3 access + call exit = 5

		Access entryPoint = processedSubTrace.get(0);
		Access exitPoint = processedSubTrace.get(4);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		assertEquals(exitPoint.getNextAccessProbabilities().keySet().isEmpty(), true);
		
		Access access;
		Access condition = null;

		for (int i = 1; i < 4; i++) {
			access = processedSubTrace.get(i);
			if(condition == null) {
				assertEquals(entryPoint.getNextAccessProbabilities().containsKey(access), true);
				condition = access;
			} else {
				assertEquals(condition.getNextAccessProbabilities().containsKey(access), true);
				assertEquals(access.getNextAccessProbabilities().containsKey(exitPoint), true);
			}
			
		}
		
		
    }

	@Test  
    public void nodeToAccessGraph_If_NonEmptyTrace() throws JSONException{
		// setup
		Access access1 = new Access("R", 6);

		JSONObject totalTrace = initializeBaseTrace(new Object[][][]{
			new Object[][]{
				new Object[]{access1.getMode(), access1.getEntityAccessedId()}, new Object[]{"if", 1}, new Object[]{access1.getMode(), access1.getEntityAccessedId()}
			},
			new Object[][]{
				new Object[]{"condition", 2}, new Object[]{"then", 3}, new Object[]{"else", 4}
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
		assertEquals(processedSubTrace.size(), 7); // entry + 3 access + call exit = 5

		Access entryPoint = processedSubTrace.get(1);
		Access exitPoint = processedSubTrace.get(5);
		assertNull(entryPoint.getMode());
		assertNull(exitPoint.getMode());
		
		Access access;
		Access condition = null;

		for (int i = 2; i < 5; i++) {
			access = processedSubTrace.get(i);
			if(condition == null) {
				assertEquals(entryPoint.getNextAccessProbabilities().containsKey(access), true);
				condition = access;
			} else {
				assertEquals(condition.getNextAccessProbabilities().containsKey(access), true);
				assertEquals(access.getNextAccessProbabilities().containsKey(exitPoint), true);
			}
			
		}

		access = processedSubTrace.get(0);
		assertEquals(access.getNextAccessProbabilities().containsKey(entryPoint), true);

		access = processedSubTrace.get(6);
		assertEquals(exitPoint.getNextAccessProbabilities().containsKey(access), true);
		
    }

	// Label



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
			access = new JSONArray().put(accessPair[0]).put(accessPair[1]);
			accessList.put(access);
		}

		return trace.accumulate("id", id).accumulate("a", accessList);
	}




}

