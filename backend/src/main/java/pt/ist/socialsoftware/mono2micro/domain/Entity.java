package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;

public class Entity {
	private String name;
	private List<String> controllers;

	public Entity() {

	}

	public Entity(String name) {
        this.name = name;
        this.controllers = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getControllers() {
		return this.controllers;
	}

	public void setControllers(List<String> controllers) {
		this.controllers = controllers;
	}

	public void addController(String controller) {
		this.controllers.add(controller);
	}

}
