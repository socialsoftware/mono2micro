package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
	private String id;
	private String label;
	private float level;

	public Node() {
	}

	public Node(String id, float level) {
		this.id = id;
		this.label = id;
		this.level = level;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float getLevel() {
		return this.level;
	}

	public void setLevel(float level) {
		this.level = level;
	}

}
