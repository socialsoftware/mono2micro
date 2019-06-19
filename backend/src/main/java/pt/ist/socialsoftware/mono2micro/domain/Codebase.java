package pt.ist.socialsoftware.mono2micro.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Codebase {
	private String name;
	private Map<String,List<String>> profiles = new HashMap<>();

	public Codebase() {
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
}
