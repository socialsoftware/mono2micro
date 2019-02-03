package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private String name;
	private List<String> entities;

	public Cluster() {

	}

	public Cluster(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getEntities() {
		return this.entities;
	}

	public void addEntity(String entity) {
		this.entities.add(entity);
	}

}
