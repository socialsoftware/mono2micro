package pt.ist.socialsoftware.mono2micro.domain.decomposition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "strategyType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AccessesSciPyDecomposition.class, name = Strategy.StrategyType.ACCESSES_SCIPY)
})
public abstract class Decomposition {
	private String name;
	private String codebaseName;
	private String strategyName;

	@JsonIgnore
	public abstract String getStrategyType();

	public String getCodebaseName() { return this.codebaseName; }

	public void setCodebaseName(String codebaseName) { this.codebaseName = codebaseName; }

	public String getStrategyName() { return this.strategyName; }

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getName() { return this.name; }

	public void setName(String name) {
		this.name = name;
	}
}