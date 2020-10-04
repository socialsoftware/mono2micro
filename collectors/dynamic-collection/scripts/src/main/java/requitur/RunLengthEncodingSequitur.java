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

		removeSingleUsageRules(sequitur.getStartSymbol().getSuccessor());

		reduce(sequitur.getStartSymbol());

		markExistingRules();
		reduce(sequitur.getStartSymbol());
		removeSingleUsageRules(sequitur.getStartSymbol().getSuccessor());
	}

	private void markExistingRules() {
		final ExistingRuleMarker marker = new ExistingRuleMarker(sequitur);
		marker.mark();
	}

	private void removeSingleUsageRules(Symbol iterator) {
		while (iterator != null && iterator.getValue() != null && iterator.getSuccessor() != null) {
			if (iterator.getValue() instanceof RuleContent) {
				RuleContent ruleName = (RuleContent) iterator.getValue();
				Rule rule = sequitur.getRules().get(ruleName.getValue());

				removeSingleUsageRules(rule.getAnchor().getSuccessor());

				if (iterator.getOccurrences() == 1) {
					removeSingleOccurrenceRule(iterator, rule);
				}
			}

			iterator = iterator.getSuccessor();
			// TraceStateTester.assureCorrectState(sequitur);
		}
	}

	private void removeSingleOccurrenceRule(Symbol iterator, Rule rule) {
		Symbol currentPredecessor = iterator.getPredecessor();
		Symbol ruleIterator = rule.getAnchor().getSuccessor();

		while (ruleIterator.getSuccessor() != rule.getAnchor()) {
			currentPredecessor = copySymbol(currentPredecessor, ruleIterator);
			ruleIterator = ruleIterator.getSuccessor();
		}

		Symbol copied = copySymbol(currentPredecessor, ruleIterator);

		copied.setSuccessor(iterator.getSuccessor());
		iterator.getSuccessor().setPredecessor(copied);
	}

	private Symbol copySymbol(Symbol currentPredecessor, Symbol ruleIterator) {
		Symbol copied = new Symbol(sequitur, ruleIterator.getValue(), ruleIterator.getRule());
		copied.setOccurrences(ruleIterator.getOccurrences());
		currentPredecessor.setSuccessor(copied);
		copied.setPredecessor(currentPredecessor);
		return copied;
	}

	private void reduce(final Symbol start) {
		Symbol iterator = start.getSuccessor();
		reduceRule(iterator);

		while (
			iterator != null &&
			iterator.getValue() != null &&
			iterator.getSuccessor() != null &&
			iterator.getSuccessor().getValue() != null
		) {
			final Symbol successor = iterator.getSuccessor();
			reduceRule(successor);

			if (iterator.valueEqual(successor)) {
				mergeOccurrences(iterator, successor);

			} else {
				iterator = iterator.getSuccessor();
			}
		}
	}

	private void mergeOccurrences(Symbol iterator, final Symbol successor) {
		if (successor.getSuccessor() != null) {
			iterator.setSuccessor(successor.getSuccessor());
			successor.getSuccessor().setPredecessor(iterator);
		} else {
			iterator.setSuccessor(null);
		}

		iterator.setOccurrences(iterator.getOccurrences() + successor.getOccurrences());
	}

	private void reduceRule(final Symbol containingSymbol) {
		if (containingSymbol.isRule()) {
			LOG.trace("Reduce: {}", containingSymbol);

			final Rule rule = containingSymbol.getRule();
			final Symbol ruleAnchor = rule.getAnchor();

			reduce(ruleAnchor);

			final Symbol firstSymbolOfRule = ruleAnchor.getSuccessor();
			LOG.trace("Reduced: {}", rule.getName());
			LOG.trace("Rule-Length: {}", rule.getElements().size() + " " + (firstSymbolOfRule.getSuccessor() == ruleAnchor));

			if (firstSymbolOfRule.getSuccessor() == ruleAnchor) {
				removeRuleUsage(
					containingSymbol,
					rule,
					firstSymbolOfRule
				);
			}
			// TraceStateTester.testTrace(sequitur);
		}
	}

	private void removeRuleUsage(
		final Symbol containingSymbol,
		final Rule rule,
		final Symbol firstSymbolOfRule
	) {
		containingSymbol.setValue(firstSymbolOfRule.getValue());
		containingSymbol.setOccurrences(containingSymbol.getOccurrences() * firstSymbolOfRule.getOccurrences());
		containingSymbol.decrementUsage(rule);

		if (firstSymbolOfRule.getRule() != null) {
			containingSymbol.setRule(firstSymbolOfRule.getRule());
		} else {
			firstSymbolOfRule.setRule(null);
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

	public List<ReducedTraceElement> getTopLevelTrace() {
		Symbol iterator = sequitur.getStartSymbol().getSuccessor();
		final List<ReducedTraceElement> trace = new ArrayList<>();
		while (iterator != null) {
			final ReducedTraceElement newElement = new ReducedTraceElement(iterator.getValue(), iterator.getOccurrences());
			trace.add(newElement);
			iterator = iterator.getSuccessor();
		}
		return trace;
	}

	/**
	* Adds the symbols in the current iterator to the given list
	*
	* @param iterator
	* @param trace
	* @return The count of elements that where added
	*/
	private int addReadableElement(final Symbol iterator, final List<ReducedTraceElement> trace) {
		final Content content = iterator.getValue();
		LOG.trace("Add: {} {}", content, content.getClass());

		final ReducedTraceElement newElement = new ReducedTraceElement(content, iterator.getOccurrences());

		if (content instanceof RuleContent) {
			return addRuleContent(
				iterator,
				trace,
				content,
				newElement
			);

		} else {
			trace.add(newElement);

			return 1;
		}
	}

	private int addRuleContent(
		final Symbol iterator,
		final List<ReducedTraceElement> trace,
		final Content content,
		final ReducedTraceElement newElement
	) {
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
	}


}
