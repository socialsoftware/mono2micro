package pt.ist.socialsoftware.mono2micro.codebase.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Document("codebase")
public class Codebase {
	@Id
	private String name;
	@DBRef(lazy = true)
	private List<Source> sources;
	@DBRef(lazy = true)
	private List<Strategy> strategies;

	public Codebase() {}

	public Codebase(String name) {
        this.name = name;
		sources = new ArrayList<>();
		strategies = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Source> getSources() {
		return sources;
	}

	public Source getSourceByType(String type) {
		return this.sources.stream().filter(source -> source.getType().equals(type)).findFirst().orElse(null);
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}

	public void addSource(Source source) {
		this.sources.add(source);
	}

	public void removeSource(String sourceId) {
		this.sources = this.sources.stream().filter(source -> !source.getName().equals(sourceId)).collect(Collectors.toList());
	}

	public List<Strategy> getStrategies() {
		return strategies;
	}

	public Strategy getStrategyByType(String strategyType) {
		return strategies.stream().filter(strategy -> strategy.getType().equals(strategyType)).findFirst().orElse(null);
	}

	public void setStrategies(List<Strategy> strategies) {
		this.strategies = strategies;
	}

	public void addStrategy(Strategy strategy) {
		this.strategies.add(strategy);
	}

	public void removeStrategy(String strategyName) {
		this.strategies = this.strategies.stream().filter(strategy -> !strategy.getName().equals(strategyName)).collect(Collectors.toList());
	}
}