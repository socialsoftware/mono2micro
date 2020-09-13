package requitur;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requitur.content.Content;
import requitur.content.RuleContent;
import requitur.content.StringContent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SequiturTests {
	private static final Logger LOG = LoggerFactory.getLogger(SequiturTests.class);

	public static List<String> contentToStringTrace(final List<Content> expandedTrace) {
		return expandedTrace.stream().map(value -> ((StringContent) value).getValue()).collect(Collectors.toList());
	}

	@BeforeEach
	void setUp(TestInfo testInfo) {
		LOG.info("STARTING TEST: " + testInfo.getDisplayName());
	}

	@Test
	@Order(1)
	public void testBasic() {
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("B");
		}

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		assertEquals(2, uncompressedTrace.size());
		assertEquals("#0", ((RuleContent) uncompressedTrace.get(0)).getValue());
		assertEquals("#0", ((RuleContent) uncompressedTrace.get(1)).getValue());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(2)
	public void testOverlappingPredecessor() {
		final List<String> mytrace = new LinkedList<>();
		Collections.addAll(mytrace, "F", "E", "F", "E", "F", "F", "F", "E", "F", "G", "H", "C", "D", "F", "E", "F", "F", "X");

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("trace: " + seg.getTrace());
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(9, uncompressedTrace.size());


		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(3)
	public void testCorrectNamesExtended() {
		final List<String> mytrace = new LinkedList<>();
		Collections.addAll(mytrace, "setUp", "test", "A", "B", "C", "A", "B", "C");

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(4, uncompressedTrace.size());
//      assertEquals("#0 (2)", uncompressedTrace.get(2)); // Could be tested sometimes..

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(4)
	public void testCorrectNamesSimple() {
		final List<String> mytrace = new LinkedList<>();
		Collections.addAll(mytrace, "setUp", "test", "A", "B", "A", "B");

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(4, uncompressedTrace.size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
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

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(15, uncompressedTrace.size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(6)
	public void testViewExample() {
		final List<String> mytrace = new LinkedList<>();

		mytrace.add("A");
		mytrace.add("B");

		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 5; i++) {
				mytrace.add("C");
				mytrace.add("D");
			}

			mytrace.add("E");
		}

		mytrace.add("E");

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);
		assertEquals(8, uncompressedTrace.size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(7)
	public void testTriple() {
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("B");
			mytrace.add("C");
		}

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		assertEquals(2, uncompressedTrace.size());
		assertEquals("#1", ((RuleContent) uncompressedTrace.get(0)).getValue());
		assertEquals("#1", ((RuleContent) uncompressedTrace.get(1)).getValue());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(8)
	public void testQuad() {
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("B");
			mytrace.add("C");
			mytrace.add("D");
		}

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		assertEquals(2, uncompressedTrace.size());
		assertEquals(1, seg.getRules().size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(9)
	public void test6() {
		final List<String> mytrace = new LinkedList<>();

		for (int i = 0; i < 2; i++) {
			mytrace.add("A");
			mytrace.add("B");
			mytrace.add("C");
			mytrace.add("D");
			mytrace.add("E");
			mytrace.add("F");
		}

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		assertEquals(2, uncompressedTrace.size());
		assertEquals(1, seg.getRules().size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(10)
	public void testNested() {
		final List<String> mytrace = new LinkedList<>();

		for (int j = 0; j < 3; j++) {
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
		mytrace.add("H");

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		assertEquals(5, uncompressedTrace.size());
		assertEquals(3, seg.getRules().size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(11)
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

		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}

	@Test
	@Order(12)
	public void testMultipleNest() {
		final List<String> mytrace = new LinkedList<>();

		for (int k = 0; k < 2; k++) {
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
			// mytrace.add("B");
			// mytrace.add("H");
		}
		final Sequitur seg = new Sequitur();
		seg.addElements(mytrace);

		System.out.println("trace: " + mytrace);
		System.out.println("Sequitur trace: " + seg.getTrace());
		System.out.println("Sequitur rules: " + seg.getRules());

		final List<Content> uncompressedTrace = seg.getUncompressedTrace();
		System.out.println("uncompressedTrace: " + uncompressedTrace);

		// assertEquals(10, uncompressedTrace.size());
		assertEquals(5, seg.getRules().size());

		final List<Content> expandedTrace = TraceStateTester.expandContentTrace(uncompressedTrace, seg.getRules());
		System.out.println("expandedTrace: " + expandedTrace);
		assertEquals(mytrace, contentToStringTrace(expandedTrace));
	}
}
