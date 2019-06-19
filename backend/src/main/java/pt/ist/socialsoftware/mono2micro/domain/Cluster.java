package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cluster {
	private String name;
	private List<String> entities = new ArrayList<>();
	private float complexity;
	private float complexityRW;
	private float complexitySeq;
	private float cohesion;
	private Map<String,Float> coupling;
	private Map<String,Float> couplingRW;
	private Map<String,Float> couplingSeq;

	public Cluster() {
	}

	public Cluster(String name) {
        this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getComplexity() {
		return complexity;
	}

	public void setComplexity(float complexity) {
		this.complexity = complexity;
	}

	public float getComplexityRW() {
		return complexityRW;
	}

	public void setComplexityRW(float complexityRW) {
		this.complexityRW = complexityRW;
	}

	public float getComplexitySeq() {
		return complexitySeq;
	}

	public void setComplexitySeq(float complexitySeq) {
		this.complexitySeq = complexitySeq;
	}

	public float getCohesion() {
		return cohesion;
	}

	public void setCohesion(float cohesion) {
		this.cohesion = cohesion;
	}

	public Map<String,Float> getCoupling() {
		return coupling;
	}

	public void setCoupling(Map<String,Float> coupling) {
		this.coupling = coupling;
	}

	public Map<String,Float> getCouplingRW() {
		return couplingRW;
	}

	public void setCouplingRW(Map<String,Float> couplingRW) {
		this.couplingRW = couplingRW;
	}

	public Map<String,Float> getCouplingSeq() {
		return couplingSeq;
	}

	public void setCouplingSeq(Map<String,Float> couplingSeq) {
		this.couplingSeq = couplingSeq;
	}

	public List<String> getEntities() {
		return this.entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public void addEntity(String entity) {
		this.entities.add(entity);
	}

	public boolean removeEntity(String entity) {
		return this.entities.remove(entity);
	}

	public boolean containsEntity(String entity) {
		return this.entities.contains(entity);
	}

	public void calculateCohesion(Map<String,List<Controller>> clusterControllers) {
		float cohesion = 0;
		for (Controller controller : clusterControllers.get(this.name)) {
			float numberEntitiesTouched = 0;
			for (String controllerEntity : controller.getEntities().keySet()) {
				if (this.containsEntity(controllerEntity))
					numberEntitiesTouched++;
			}
			cohesion = cohesion + (numberEntitiesTouched / this.entities.size());
		}
		cohesion /= clusterControllers.get(this.name).size();
		this.setCohesion(cohesion);
	}
}
