package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
	private String id;
	private String label;
	private String title;

	public Node() {
	}

	public Node(String id, String label, String title) {
		this.id = id;
		this.label = label;
		this.title = title;
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

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
