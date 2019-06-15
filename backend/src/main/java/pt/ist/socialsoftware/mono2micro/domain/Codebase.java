package pt.ist.socialsoftware.mono2micro.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Codebase {
	private String name;
	private Map<String,List<String>> profiles;

	public Codebase() {

	}

	public Codebase(String name) {
        this.name = name;
		this.profiles = new HashMap<>();
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

	public void moveController(String moveController, String moveToProfile) {
        for (String profileName : this.profiles.keySet()) {
            if (this.profiles.get(profileName).contains(moveController)) {
                this.profiles.get(profileName).remove(moveController);
                break;
            }
        }
        this.profiles.get(moveToProfile).add(moveController);
	}

	public void deleteProfile(String profileName) {
        this.profiles.remove(profileName);
	}
}
