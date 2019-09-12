package pt.ist.socialsoftware.mono2micro.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;

public class Expert {
	private String codebaseName;
	private String name;
	private Map<String,List<String>> clusters = new HashMap<>();

	public Expert() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodebaseName() {
		return this.codebaseName;
	}

	public void setCodebaseName(String codebaseName) {
		this.codebaseName = codebaseName;
	}

	public Map<String,List<String>> getClusters() {
		return this.clusters;
	}
	
	public void setClusters(Map<String,List<String>> clusters) {
		this.clusters = clusters;
    }
    
    public List<String> getCluster(String cluster) {
        return this.clusters.get(cluster);
    }
    
    public void addCluster(String name, List<String> entities) {
        if (this.clusters.containsKey(name)) {
			throw new KeyAlreadyExistsException();
		}
        this.clusters.put(name, entities);
    }

	public void moveEntities(String[] entities, String targetCluster) {
        for (String cluster : this.clusters.keySet()) {
			for (String entity : entities) {
				if (this.clusters.get(cluster).contains(entity)) {
					this.clusters.get(cluster).remove(entity);
				}
			}
		}
		for (String entity : entities)
        	this.clusters.get(targetCluster).add(entity);
	}

	public void deleteCluster(String clusterName) {
        this.clusters.remove(clusterName);
	}
}
