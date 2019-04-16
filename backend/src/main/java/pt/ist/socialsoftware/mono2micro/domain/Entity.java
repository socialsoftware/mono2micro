package pt.ist.socialsoftware.mono2micro.domain;

public class Entity {
	private String name;
	private float immutability;

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
    
    public float getImmutability() {
		return this.immutability;
	}

	public void setImmutability(float immutability) {
		this.immutability = immutability;
	}
}
