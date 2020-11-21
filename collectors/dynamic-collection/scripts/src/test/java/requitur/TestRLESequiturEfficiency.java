package requitur;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import requitur.content.Content;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRLESequiturEfficiency {

	@Test
	@Order(1)
	public void testUnitRuleExample() {
		String[] content = new String[] { "B", "B", "C", "C", "B", "D", "D", "D", "E", "D", "E", "E", "D", "E", "E", "B", "C", "C", "B", "C", "C", "B", "C", "C", "B", "C", "C" };
		System.out.println(Arrays.toString(content));
		List<String> mytrace = Arrays.asList(content);
		final Sequitur seq = new Sequitur();
		seq.addStringElements(mytrace);

		final List<Content> trace = seq.getUncompressedTrace();
		System.out.println(trace + " " + seq.getRules());
		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seq);
		runLengthEncodingSequitur.reduce();
		final List<Content> unexpandedTrace = seq.getUncompressedTrace();
		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seq.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));

		assertEquals(13, runLengthEncodingSequitur.getReadableRLETrace().size());
	}

	@Test
	@Order(2)
	public void testUnitRuleExampleManyOccurrences() {
		String[] content = new String[] { "A", "B", "C", "D", "C", "D", "C", "D", "B", "C", "D", "A", "B", "C", "D", "C", "D", "C", "D", "C", "D", "C", "D" };
		System.out.println(Arrays.toString(content));
		List<String> mytrace = Arrays.asList(content);
		final Sequitur seq = new Sequitur();
		seq.addStringElements(mytrace);

		final List<Content> trace = seq.getUncompressedTrace();
		System.out.println(trace + " " + seq.getRules());
		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seq);
		runLengthEncodingSequitur.reduce();
		final List<Content> unexpandedTrace = seq.getUncompressedTrace();
		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seq.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(3)
	public void testUnitRuleNewSequences() {
		String[] content = new String[] { "A","A","A","A","B","A","C","A","D","D","D","A","C","A","D","D","D","D","A","C","A","D","D","A","C","A","D","D","D","D","D","B","C","B","E","E","B","C","C" };

		System.out.println(Arrays.toString(content));
		List<String> mytrace = Arrays.asList(content);
		final Sequitur seq = new Sequitur();
		seq.addStringElements(mytrace);

		final List<Content> trace = seq.getUncompressedTrace();
		System.out.println(trace + " " + seq.getRules());
		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seq);
		runLengthEncodingSequitur.reduce();

		System.out.println(runLengthEncodingSequitur.getReadableRLETrace());
		final List<Content> unexpandedTrace = seq.getUncompressedTrace();
		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seq.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}
}
