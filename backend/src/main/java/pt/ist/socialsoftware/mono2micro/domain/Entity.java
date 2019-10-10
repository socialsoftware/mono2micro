package pt.ist.socialsoftware.mono2micro.domain;

public class Entity {
	private String name;

	public Entity() {

	}

	public Entity(String name) {
        this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
    }
}
