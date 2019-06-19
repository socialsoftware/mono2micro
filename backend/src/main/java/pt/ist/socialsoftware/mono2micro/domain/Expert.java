package pt.ist.socialsoftware.mono2micro.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expert {
	private String name;
	private String codebase;
	private Map<String,List<String>> clusters = new HashMap<>();

	public Expert() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodebase() {
		return this.codebase;
	}

	public void setCodebase(String codebase) {
		this.codebase = codebase;
	}

	public Map<String,List<String>> getClusters() {
		return this.clusters;
    }
    
    public List<String> getCluster(String cluster) {
        return this.clusters.get(cluster);
    }

	public void setClusters(Map<String,List<String>> clusters) {
		this.clusters = clusters;
    }
    
    public void addCluster(String name, List<String> entities) {
        if (!this.clusters.containsKey(name))
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
