package pt.ist.socialsoftware.mono2micro.cluster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Cluster {
	String name;
	Map<String, Object> metrics = new HashMap<>(); // Map<MetricCalculator type, MetricCalculator value>
	Set<Element> elements = new HashSet<>();

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Element> getElements() {
		return elements;
	}

	@JsonIgnore
	public Set<Short> getElementsIDs() {
		return elements.stream().map(Element::getId).collect(Collectors.toSet());
	}

	@JsonIgnore
	public Element getElementByID(short elementID) {
		return elements.stream().filter(element -> element.getId().equals(elementID)).findFirst().orElse(null);
	}

	public void setElements(Set<Element> elements) {
		this.elements = elements;
	}

	public void addElement(Element element) {
		this.elements.add(element);
	}

	public boolean containsElement(short elementID) {
		return this.elements.stream().anyMatch(e -> e.getId().equals(elementID));
	}

	public boolean containsElement(Element element) {
		return this.elements.stream().anyMatch(e -> e.getId().equals(element.getId()));
	}

	public void removeElement(short elementID) {
		this.elements = this.elements.stream().filter(e -> !e.getId().equals(elementID)).collect(Collectors.toSet());
	}

	public void removeElement(Element element) {
		this.elements = this.elements.stream().filter(e -> !e.getId().equals(element.getId())).collect(Collectors.toSet());
	}

	public Map<String, Object> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String, Object> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(String type, Object value) {
		this.metrics.put(type, value);
	}
}
