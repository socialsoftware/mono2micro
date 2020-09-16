package requitur;

import org.junit.jupiter.api.*;
import requitur.content.Content;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 3; i++) {
			mytrace.add("A");
		}

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

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
		final List<String> mytrace = new LinkedList<>();

		mytrace.add("A");
		mytrace.add("B");

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				mytrace.add("C");
				mytrace.add("D");
			}
			mytrace.add("E");
		}

		mytrace.add("E");
		System.out.println("trace: " + mytrace);

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

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
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("B");
		}

		System.out.println("trace: " + mytrace);

		final Sequitur seg = new Sequitur();

		seg.addElements(mytrace);
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
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 1; i++) {
			mytrace.add("A");
			mytrace.add("B");
			mytrace.add("C");
		}

		for (int i = 0; i < 2; i++) {
			mytrace.add("D");
			mytrace.add("E");
			mytrace.add("F");
		}

		System.out.println("trace: " + mytrace);

		final Sequitur seg = new Sequitur();

		seg.addElements(mytrace);
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
		final List<String> mytrace = new LinkedList<>();
		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("A");
			mytrace.add("C");
		}

		System.out.println("trace: " + mytrace);

		final Sequitur seg = new Sequitur();

		seg.addElements(mytrace);
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
		final List<String> mytrace = new LinkedList<>();

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				mytrace.add("A");
				mytrace.add("A");
			}
			mytrace.add("B");
			mytrace.add("B");
			mytrace.add("C");
		}

		System.out.println("trace: " + mytrace);

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

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
		final List<String> mytrace = new LinkedList<>();

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				mytrace.add("A");
				mytrace.add("B");
				mytrace.add("C");
			}
			mytrace.add("D");
			mytrace.add("E");
			mytrace.add("F");
		}
		mytrace.add("A");
		mytrace.add("B");
		mytrace.add("C");

		System.out.println("trace: " + mytrace);

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

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
	public void testOverlappingSuccessor() {
		final List<String> mytrace = new LinkedList<>();
		Collections.addAll(mytrace, "D", "E", "G", "K", "I", "J", "I", "J", "I", "J", "X", "M", "L", "N", "O", "P", "T", "Q", "R", "S", "R", "S", "R", "S", "U", "V", "W", "V", "X", "M", "L", "N", "O", "P", "T", "Q", "R", "S");

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

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
}
