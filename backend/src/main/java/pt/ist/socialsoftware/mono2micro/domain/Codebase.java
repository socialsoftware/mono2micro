package pt.ist.socialsoftware.mono2micro.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Codebase {
	private String name;
	private Map<String,List<String>> profiles = new HashMap<>();
	private List<Dendrogram> dendrograms = new ArrayList<>();
	private List<Expert> experts = new ArrayList<>();

	public Codebase() {
	}

	public List<Expert> getExperts() {
		return experts;
	}

	public void setExperts(List<Expert> experts) {
		this.experts = experts;
	}

	public List<Dendrogram> getDendrograms() {
		return dendrograms;
	}

	public void setDendrograms(List<Dendrogram> dendrograms) {
		this.dendrograms = dendrograms;
	}

	public Codebase(String name) {
        this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String,List<String>> getProfiles() {
		return this.profiles;
    }
    
    public List<String> getProfile(String profile) {
        return this.profiles.get(profile);
    }

	public void setProfiles(Map<String,List<String>> profiles) {
		this.profiles = profiles;
    }
    
    public void addProfile(String name, List<String> controllers) {
        if (!this.profiles.containsKey(name))
            this.profiles.put(name, controllers);
    }

	public void moveControllers(String[] controllers, String targetProfile) {
        for (String profile : this.profiles.keySet()) {
			for (String controller : controllers) {
				if (this.profiles.get(profile).contains(controller)) {
					this.profiles.get(profile).remove(controller);
				}
			}
		}
		for (String controller : controllers)
        	this.profiles.get(targetProfile).add(controller);
	}

	public void deleteProfile(String profileName) {
        this.profiles.remove(profileName);
	}

	public Dendrogram getDendrogram(String dendrogramName) {
		for (Dendrogram dendrogram : this.dendrograms)
			if (dendrogram.getName().equals(dendrogramName))
				return dendrogram;
		return null;
	}

	public boolean deleteDendrogram(String dendrogramName) {
		for (int i = 0; i < dendrograms.size(); i++) {
			if (dendrograms.get(i).getName().equals(dendrogramName)) {
				dendrograms.remove(i);
				return true;
			}
		}
		return false;
	}

	public List<String> getDendrogramNames() {
		List<String> dendrogramNames = new ArrayList<>();
		for (Dendrogram dendrogram : this.dendrograms)
			dendrogramNames.add(dendrogram.getName());
		return dendrogramNames;
	}

	public void addDendrogram(Dendrogram dendrogram) {
		this.dendrograms.add(dendrogram);
	}

	public Expert getExpert(String expertName) {
		for (Expert expert : this.experts)
			if (expert.getName().equals(expertName))
				return expert;
		return null;
	}

	public boolean deleteExpert(String expertName) {
		for (int i = 0; i < experts.size(); i++) {
			if (experts.get(i).getName().equals(expertName)) {
				experts.remove(i);
				return true;
			}
		}
		return false;
	}

	public List<String> getExpertNames() {
		List<String> expertNames = new ArrayList<>();
		for (Expert expert : this.experts)
			expertNames.add(expert.getName());
		return expertNames;
	}

	public void addExpert(Expert expert) {
		this.experts.add(expert);
	}
}
