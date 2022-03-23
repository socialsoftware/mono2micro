package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.CodebaseDeserializer;

import java.util.ArrayList;
import java.util.List;


@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = CodebaseDeserializer.class)
public class Codebase {
	private String name;
	private List<String> collectors = new ArrayList<>();

	public Codebase() {}

	public Codebase(String name) {
        this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getCollectors() {
		return collectors;
	}

	public void setCollectors(List<String> collectors) {
		this.collectors = collectors;
	}

	public void addCollector(String collector) {
		this.collectors.add(collector);
	}

	public void removeCollector(String collector) {
		this.collectors.remove(collector);
	}
}
