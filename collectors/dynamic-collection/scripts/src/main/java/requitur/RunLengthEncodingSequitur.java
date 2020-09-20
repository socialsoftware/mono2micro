package requitur;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requitur.content.Content;
import requitur.content.RuleContent;

public class RunLengthEncodingSequitur {

	private static final Logger LOG = LoggerFactory.getLogger(RunLengthEncodingSequitur.class);

	private Sequitur sequitur;

	public RunLengthEncodingSequitur() {}

	public RunLengthEncodingSequitur(final Sequitur sequitur) {
		this.sequitur = sequitur;
	}


	public void setSequitur(Sequitur seq) {
		this.sequitur = seq;
	}

	public void reduce() {
		reduce(sequitur.getStartSymbol());
	}

	private void reduce(final Symbol start) {
		Symbol iterator = start.getSuccessor();
		subReduce(iterator);

		while (
			iterator != null &&
			iterator.getValue() != null &&
			iterator.getSuccessor() != null &&
			iterator.getSuccessor().getValue() != null
		) {
			final Symbol successor = iterator.getSuccessor();
			subReduce(successor);

			if (iterator.valueEqual(successor)) {
				if (successor.getSuccessor() != null) { // similar to linkedList deletion process
					iterator.setSuccessor(successor.getSuccessor());
					successor.getSuccessor().setPredecessor(iterator);

				} else {
					iterator.setSuccessor(null);
				}

				iterator.setOccurrences(iterator.getOccurrences() + successor.getOccurrences());

			} else {
				iterator = iterator.getSuccessor();
			}
		}
	}

	private void subReduce(final Symbol containingSymbol) {
		if (containingSymbol.isRule()) {
			LOG.trace("Reduce: {}", containingSymbol);

			final Rule rule = containingSymbol.getRule();
			final Symbol iterator = rule.getAnchor();

			reduce(iterator);

			final Symbol firstSymbolOfRule = iterator.getSuccessor();
			LOG.trace("Reduced: {}", rule.getName());
			LOG.trace("Rule-Length: {}", rule.getElements().size() + " " + (firstSymbolOfRule.getSuccessor() == iterator));

			if (firstSymbolOfRule.getSuccessor() == iterator) { // Irgendwie entsteht hier die Zuordnung #1 auf Regel #0
				containingSymbol.setValue(firstSymbolOfRule.getValue());
				containingSymbol.setOccurrences(containingSymbol.getOccurrences() * firstSymbolOfRule.getOccurrences());
				containingSymbol.decrementUsage(rule);

				if (firstSymbolOfRule.getRule() != null) {
					containingSymbol.setRule(firstSymbolOfRule.getRule());

				} else {
					firstSymbolOfRule.setRule(null);
				}

			}
			// TraceStateTester.testTrace(sequitur);
		}
	}

	public List<ReducedTraceElement> getReadableRLETrace() {
		Symbol iterator = sequitur.getStartSymbol().getSuccessor();
		final List<ReducedTraceElement> trace = new ArrayList<>();

		while (iterator != null) {
			addReadableElement(iterator, trace);
			iterator = iterator.getSuccessor();
		}

		return trace;
	}

	public List<ReducedTraceElement> getReadableRLETraceImproved() {
		Symbol iterator = sequitur.getStartSymbol().getSuccessor();
		final List<ReducedTraceElement> trace = new ArrayList<>();

		while (iterator != null) {
			addReadableElementImproved(iterator, trace);
			iterator = iterator.getSuccessor();
		}

		return trace;
	}

	private int addReadableElement(final Symbol iterator, final List<ReducedTraceElement> trace) {
		final Content content = iterator.getValue();
		LOG.trace("Add: {} {}", content, content.getClass());

		final ReducedTraceElement newElement = new ReducedTraceElement(content, iterator.getOccurrences());

		if (content instanceof RuleContent) {
			final RuleContent currentContent = (RuleContent) content;

            trace.add(newElement);

			final Symbol anchor = iterator.getRule().getAnchor();
			Symbol ruleIterator = anchor.getSuccessor();

			int subElements = 1;

			while (ruleIterator != anchor) {
				subElements += addReadableElement(ruleIterator, trace);
				ruleIterator = ruleIterator.getSuccessor();
			}

			currentContent.setCount(subElements - 1);

			return subElements;

		} else {
			trace.add(newElement);

			return 1;
		}
	}

	private int addReadableElementImproved(final Symbol iterator, final List<ReducedTraceElement> trace) {
		final Content content = iterator.getValue();
		LOG.trace("Add: {} {}", content, content.getClass());

		final ReducedTraceElement newElement = new ReducedTraceElement(content, iterator.getOccurrences());

		if (content instanceof RuleContent) {
			final RuleContent currentContent = (RuleContent) content;

			if (newElement.getOccurrences() > 1)
				trace.add(newElement);

			final Symbol anchor = iterator.getRule().getAnchor();
			Symbol ruleIterator = anchor.getSuccessor();

			int subElements = 1;

			while (ruleIterator != anchor) {
				subElements += addReadableElementImproved(ruleIterator, trace);
				ruleIterator = ruleIterator.getSuccessor();
			}

			currentContent.setCount(subElements - 1);

			return newElement.getOccurrences() > 1 ? subElements : subElements - 1;

		} else {
			trace.add(newElement);

			return 1;
		}
	}

}
