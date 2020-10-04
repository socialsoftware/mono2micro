package requitur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import requitur.content.Content;
import requitur.content.RuleContent;

class Symbol {
	private static final Logger LOG = LoggerFactory.getLogger(Symbol.class);
	private final Sequitur sequitur;
	private Symbol predecessor;
	private Symbol successor;
	private Content value;
	private RuleContent name; // if this is a rule-symbol
	private Rule rule;
	private int occurrences = 1;

	public Symbol(final Sequitur sequitur, final Content value) {
		this.sequitur = sequitur;
		this.value = value;
		rule = null;
		name = null;
	}

	public Symbol(final Sequitur sequitur, final Symbol other) {
		this.sequitur = sequitur;
		this.value = other.value;
		this.name = other.name;
		this.occurrences = other.getOccurrences();

		if (other.rule != null) {
			rule = other.rule;
			rule.usage++;

		} else {
			rule = null;
		}
	}

	public Symbol(final Sequitur sequitur, final Content value, final Rule rule) {
		this.sequitur = sequitur;
		this.value = value;
		this.rule = rule;
		name = null;
	}

	public Symbol(final Sequitur sequitur, final Rule rule) {
		this.sequitur = sequitur;
		this.rule = rule;
		this.name = new RuleContent(rule.getName());
		this.value = null;
	}

	public Symbol getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(final Symbol predecessor) { this.predecessor = predecessor; }

	public Symbol getSuccessor() {
		return successor;
	}

	public void setSuccessor(final Symbol successor) { this.successor = successor; }

	public int getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(final int occurrences) {
		this.occurrences = occurrences;
	}

	public Rule getRule() {
		return rule;
	}

	public Content getValue() {
		return value != null ? value : name;
	}

	public boolean valueEqual(final Symbol other) {
		return other.getValue().equals(getValue());
	}

	@Override
	public String toString() {
		final Content value = getValue();

		if (value == null) return null;

		return occurrences == 1 ? value.toString() : occurrences + " x " + value.toString();
	}

	@Override
	public int hashCode() {
		if (value != null) return value.hashCode();
		if (name != null) return name.hashCode();

		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Symbol) {
			final Symbol other = (Symbol) obj;
			final Content otherContent = other.getValue();

			return otherContent.equals(getValue()) && other.getOccurrences() == occurrences;
		}

		return false;
	}

	public void decrementUsage(final Rule usingRule) {
		if (rule != null) {
			rule.decrementUsage();
			// System.out.println("Decrement usage: " + rule.value + " " + rule.usage);
			if (rule.usage == 1) {
				removeSingleUsageRule(usingRule);
			}
		}
	}

	public void removeSingleUsageRule(final Rule usingRule) {
		if (rule.getAnchor().successor.successor == rule.getAnchor().predecessor) {
			replaceTraceDigram();
		}
		final Symbol firstElement = rule.getAnchor().successor;
		final Symbol lastElement = rule.getAnchor().predecessor;
		sequitur.rules.remove(rule.getName());
		sequitur.unusedRules.add(rule);

		if (usingRule.getAnchor().successor.equals(this)) {
			replaceStartRule(usingRule, firstElement, lastElement);
		} else if (usingRule.getAnchor().predecessor.equals(this)) {
			replaceEndRule(usingRule, firstElement, lastElement);
		} else {
			throw new RuntimeException("Unexpected: Rule-Symbol " + getValue() + " is deleted, but deleted symbol is neither start nor end of rule " + usingRule);
		}
		rule.usage = 0;
	}

	private void replaceEndRule(
		final Rule usingRule,
		final Symbol firstElement,
		final Symbol lastElement
	) {
		final Symbol temp = usingRule.getAnchor().predecessor.predecessor;
		sequitur.digrams.remove(new Digram(usingRule.getAnchor().predecessor, usingRule.getAnchor().predecessor.predecessor));
		sequitur.link(usingRule.getAnchor(), lastElement);
		sequitur.link(firstElement, temp);
	}

	private void replaceStartRule(
		final Rule usingRule,
		final Symbol firstElement,
		final Symbol lastElement
	) {
		final Symbol temp = usingRule.getAnchor().successor.successor;
		sequitur.digrams.remove(new Digram(usingRule.getAnchor().successor, usingRule.getAnchor().successor.successor));
		sequitur.link(usingRule.getAnchor(), firstElement);
		sequitur.link(lastElement, temp);
	}

	/**
	 * The digram, originally saved in the trace, is replaced by the digram in the rule.
	 */
	private void replaceTraceDigram() {
		final Digram ruleDigram = new Digram(rule.getAnchor().successor, rule.getAnchor().successor.successor);
		final Digram savedDigram = this.sequitur.digrams.get(ruleDigram);
		savedDigram.rule = null;
	}

	public void setValue(final Content value) {
		this.value = value;
		this.name = null;

	}

	public boolean isRule() { return name != null; }
	public void setRule(Rule rule) { this.rule = rule; }
}