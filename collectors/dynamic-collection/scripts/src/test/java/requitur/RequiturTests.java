package requitur;

import org.junit.jupiter.api.*;
import requitur.content.Content;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RequiturTests {
	private static final Logger LOG = LoggerFactory.getLogger(RequiturTests.class);

	@BeforeEach
	void setUp(TestInfo testInfo) {
		LOG.info("STARTING TEST " + testInfo.getDisplayName());
	}

	@Test
	@Order(1)
	public void test3AReduction() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		for (int i = 0; i < 3; i++) {
			mytrace.add("A");
			seg.addStringElement("A");
		}

//		seg.addStringElements(mytrace);

		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		assertEquals(1, trace.size());
		assertEquals(3, trace.get(0).getOccurrences());

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);

		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(2)
	public void testViewExample() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		mytrace.add("A");
		mytrace.add("B");

		seg.addStringElement("A");
		seg.addStringElement("B");

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				mytrace.add("C");
				mytrace.add("D");

				seg.addStringElement("C");
				seg.addStringElement("D");
			}

			mytrace.add("E");
			seg.addStringElement("E");
		}

		mytrace.add("E");
		seg.addStringElement("E");

		System.out.println("trace: " + mytrace);

//		seg.addStringElements(mytrace);

		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));

	}

	@Test
	@Order(3)
	public void testSimpleTraceReduction() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("B");

			seg.addStringElement("A");
			seg.addStringElement("B");
		}

		System.out.println("trace: " + mytrace);

//		seg.addStringElements(mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(4)
	public void testTriple() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		for (int i = 0; i < 1; i++) {
			mytrace.add("A");
			mytrace.add("B");
			mytrace.add("C");

			seg.addStringElement("A");
			seg.addStringElement("B");
			seg.addStringElement("C");

		}

		for (int i = 0; i < 2; i++) {
			mytrace.add("D");
			mytrace.add("E");
			mytrace.add("F");

			seg.addStringElement("D");
			seg.addStringElement("E");
			seg.addStringElement("F");
		}

		System.out.println("trace: " + mytrace);

//		seg.addStringElements(mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(5)
	public void testRuleCompression() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("A");
			mytrace.add("C");

			seg.addStringElement("A");
			seg.addStringElement("A");
			seg.addStringElement("C");
		}

		System.out.println("trace: " + mytrace);

//		seg.addStringElements(mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(6)
	public void testRuleDeletion() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				mytrace.add("A");
				mytrace.add("A");

				seg.addStringElement("A");
				seg.addStringElement("A");
			}

			mytrace.add("B");
			mytrace.add("B");
			mytrace.add("C");

			seg.addStringElement("B");
			seg.addStringElement("B");
			seg.addStringElement("C");
		}

		System.out.println("trace: " + mytrace);

//		seg.addStringElements(mytrace);

		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(7)
	public void testRuleOnce() {
		final List<String> mytrace = new ArrayList<>();
		final Sequitur seg = new Sequitur();

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				mytrace.add("A");
				mytrace.add("B");
				mytrace.add("C");

				seg.addStringElement("A");
				seg.addStringElement("B");
				seg.addStringElement("C");
			}

			mytrace.add("D");
			mytrace.add("E");
			mytrace.add("F");

			seg.addStringElement("D");
			seg.addStringElement("E");
			seg.addStringElement("F");
		}

		mytrace.add("A");
		mytrace.add("B");
		mytrace.add("C");

		seg.addStringElement("A");
		seg.addStringElement("B");
		seg.addStringElement("C");

//		System.out.println("trace: " + mytrace);

//		seg.addStringElements(mytrace);

		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> unexpandedTrace = seg.getUncompressedTrace();
		System.out.println("unexpandedTrace: " + unexpandedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(unexpandedTrace, seg.getRules());
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}



	@Test
	@Order(8)
	public void testOverlappingSuccessor() {
		final List<String> mytrace = new ArrayList<>();
		Collections.addAll(mytrace, "D", "E", "G", "K", "I", "J", "I", "J", "I", "J", "X", "M", "L", "N", "O", "P", "T", "Q", "R", "S", "R", "S", "R", "S", "U", "V", "W", "V", "X", "M", "L", "N", "O", "P", "T", "Q", "R", "S");

		final Sequitur seg = new Sequitur();
		seg.addStringElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(15, uncompressedTrace.size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(9)
	public void testUselessRulesThatOnlyOccurOnce() {
		final List<String> mytrace = new ArrayList<>();
		Collections.addAll(mytrace, "A","A","B","B","C","C","B","D","D","D","E","D","E","E","D","E","E","B","C","C","B","C","C","B","C","C","B","C","C");

		final Sequitur seg = new Sequitur();
		seg.addStringElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seg);
		runLengthEncodingSequitur.reduce();

		final List<ReducedTraceElement> trace = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace);

		final List<ReducedTraceElement> trace2 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace2: " + trace2);

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(15, uncompressedTrace.size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, SequiturTests.contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(10)
	public void testUnitRules() {
		// AABBCCBDDDEDEEDEEBCCBCCBCCBCC
		String content[] = new String[] {"A","A","B","B","C","C","B","D","D","D","E","D","E","E","D","E","E","B","C","C","B","C","C","B","C","C","B","C","C" };
		System.out.println(Arrays.toString(content));
		List<String> trace = Arrays.asList(content);
		final Sequitur seq = new Sequitur();
		seq.addStringElements(trace);

		for (Map.Entry<String, Rule> rule : seq.getRules().entrySet()) {
			System.out.println(rule.getValue());
		}

		System.out.println(seq.getUncompressedTrace());
		System.out.println(seq.getTrace());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(seq.getUncompressedTrace(), seq.getRules());
		assertEquals(trace, SequiturTests.contentToStringTrace(expandedTrace));

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seq);
		runLengthEncodingSequitur.reduce();

		System.out.println(seq.getUncompressedTrace());
		System.out.println(seq.getTrace());

		final List<ReducedTraceElement> trace1 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace1);

		final List<ReducedTraceElement> trace2 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace without unit rules: " + trace2);
	}

	@Test
	@Order(11)
	public void testUnitRules2() {
		// AABBCCBDDDEDEEDEEBCCBCCBCCBCC
		String content[] = new String[] {"A","A","A","B","B","B","B","B","B","B","A","C","A","A","B","B","B","B","B","A","D","D","D","D","D","D","D","A","B","B","B","B","B","A","D","D","A","B","B","B","B","B","A","D","D","D","C","D","D","D","A","D","D","C","D","C","D","D","E","E","E","C","D","D","E","E","E","C","D","D","E","E","E","C","D","D","E","E","E","C","C" };
		System.out.println(Arrays.toString(content));
		List<String> trace = Arrays.asList(content);
		final Sequitur seq = new Sequitur();
		seq.addStringElements(trace);

		for (Map.Entry<String, Rule> rule : seq.getRules().entrySet()) {
			System.out.println(rule.getValue());
		}

		System.out.println(seq.getUncompressedTrace());
		System.out.println(seq.getTrace());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(seq.getUncompressedTrace(), seq.getRules());
		assertEquals(trace, SequiturTests.contentToStringTrace(expandedTrace));

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seq);
		runLengthEncodingSequitur.reduce();

		System.out.println(seq.getUncompressedTrace());
		System.out.println(seq.getTrace());

		final List<ReducedTraceElement> trace1 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace1);

		final List<ReducedTraceElement> trace2 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace without unit rules: " + trace2);
	}

	@Test
	@Order(12)
	public void testUnitRules3() {
		String[] content = new String[] {"A","A","A","A","B","A","C","A","D","D","D","A","C","A","D","D","D","D","A","C","A","D","D","A","C","A","D","D","D","D","D","B","C","B","E","E","B","C","C" };
		System.out.println(Arrays.toString(content));
		List<String> trace = Arrays.asList(content);
		final Sequitur seq = new Sequitur();
		seq.addStringElements(trace);

		for (Map.Entry<String, Rule> rule : seq.getRules().entrySet()) {
			System.out.println(rule.getValue());
		}

		System.out.println(seq.getUncompressedTrace());
		System.out.println(seq.getTrace());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(seq.getUncompressedTrace(), seq.getRules());
		assertEquals(trace, SequiturTests.contentToStringTrace(expandedTrace));

		final RunLengthEncodingSequitur runLengthEncodingSequitur = new RunLengthEncodingSequitur(seq);
		runLengthEncodingSequitur.reduce();

		System.out.println(seq.getUncompressedTrace());
		System.out.println(seq.getTrace());

		final List<ReducedTraceElement> trace1 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace: " + trace1);

		final List<ReducedTraceElement> trace2 = runLengthEncodingSequitur.getReadableRLETrace();
		System.out.println("Readable trace without unit rules: " + trace2);
	}
}
