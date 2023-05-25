package pt.ist.socialsoftware.mono2micro.utils.traceGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.test.context.junit4.SpringRunner;

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
		JSONObject totalTrace = new JSONObject("{ \"t\" : [ { \"id\": 0, \"a\": [ [ \"call\", 1 ] ] }, { \"id\": 1, \"a\": [ [ \"R\", 6 ] ] } ] }");
		JSONArray totalTraceArray = totalTrace.getJSONArray("t");
		JSONArray traceElementJSON = new JSONObject("{ \"id\": 0, \"a\": [ [ \"call\", 1 ] ] }").getJSONArray("a").getJSONArray(0);

		Call callNode = new Call(totalTrace, totalTraceArray, traceElementJSON);

		callNode.nodeToAccessGraph(processedSubTrace, null, null, null);

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

	// If



	// Label



	// Loop




}

