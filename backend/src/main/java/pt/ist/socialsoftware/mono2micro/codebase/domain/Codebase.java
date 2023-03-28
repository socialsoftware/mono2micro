package pt.ist.socialsoftware.mono2micro.codebase.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.*;
import java.util.stream.Collectors;


@Document("codebase")
public class Codebase {
	@Id
	private String name;
	@DBRef(lazy = true)
	private List<Representation> representations;
	@DBRef(lazy = true)
	private List<Strategy> strategies;

	public Codebase() {}

	public Codebase(String name) {
        this.name = name;
		representations = new ArrayList<>();
		strategies = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getRepresentationGroups() {
		Set<String> representationTypes = representations.stream()
				.map(representation -> representation.getType())
						.collect(Collectors.toSet());

		return Representation.representationGroupToRepresentations.entrySet().stream()
				.filter(entry -> representationTypes.containsAll(entry.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public List<Representation> getRepresentations() {
		return representations;
	}

	public Representation getRepresentationByFileType(String type) {
		return this.representations.stream().filter(representation -> representation.getType().equals(type)).findFirst().orElse(null);
	}

	public void setRepresentations(List<Representation> representations) {
		this.representations = representations;
	}

	public void addRepresentation(Representation representation) {
		this.representations.add(representation);
	}

	public void removeRepresentation(String representationId) {
		this.representations = this.representations.stream().filter(representation -> !representation.getName().equals(representationId)).collect(Collectors.toList());
	}

	public List<Strategy> getStrategies() {
		return strategies;
	}

	public boolean containsStrategyName(String name) {
		return this.strategies.stream().anyMatch(strategy -> strategy.getName().equals(name));
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