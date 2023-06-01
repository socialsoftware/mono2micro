package pt.ist.socialsoftware.mono2micro.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import pt.ist.socialsoftware.mono2micro.utils.traceGraph.Access;
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
}
