package pt.ist.socialsoftware.mono2micro.domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_FOLDER;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;

public class Codebase {
	private String name;
	private Map<String,List<String>> profiles = new HashMap<>();
	private List<Dendrogram> dendrograms = new ArrayList<>();
	private List<Graph> experts = new ArrayList<>();

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
    
    public List<String> getProfile(String profileName) {
		return this.profiles.get(profileName);
    }

	public void setProfiles(Map<String,List<String>> profiles) {
		this.profiles = profiles;
	}
	
	public void addProfile(String profileName, List<String> controllers) {
		if (this.getProfile(profileName) != null) {
			throw new KeyAlreadyExistsException();
		}
		this.profiles.put(profileName, controllers);
	}
	
	public void deleteProfile(String profileName) {
		this.profiles.remove(profileName);
	}

	public void moveControllers(String[] controllers, String targetProfile) {
		List<String> removedControllers = new ArrayList<>();
        for (String profile : this.profiles.keySet()) {
			for (String controller : controllers) {
				if (this.profiles.get(profile).contains(controller) && !profile.equals(targetProfile)) {
					this.profiles.get(profile).remove(controller);
					removedControllers.add(controller);
				}
			}
		}
		for (String controller : removedControllers)
			this.profiles.get(targetProfile).add(controller);
	}


	public List<Dendrogram> getDendrograms() {
		return dendrograms;
	}

	public void setDendrograms(List<Dendrogram> dendrograms) {
		this.dendrograms = dendrograms;
	}

	public Dendrogram getDendrogram(String dendrogramName) {
		for (Dendrogram dendrogram : this.dendrograms)
			if (dendrogram.getName().equals(dendrogramName))
				return dendrogram;
		return null;
	}

	public void deleteDendrogram(String dendrogramName) throws IOException {
		for (int i = 0; i < dendrograms.size(); i++) {
			if (dendrograms.get(i).getName().equals(dendrogramName)) {
				dendrograms.remove(i);
				break;
			}
		}
		Files.deleteIfExists(Paths.get(CODEBASES_FOLDER + this.name + "/" + dendrogramName + ".png"));
		Files.deleteIfExists(Paths.get(CODEBASES_FOLDER + this.name + "/" + dendrogramName + ".txt"));
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

	
	public List<Graph> getExperts() {
		return experts;
	}

	public void setExperts(List<Graph> experts) {
		this.experts = experts;
	}

	public Graph getExpert(String expertName) {
		for (Graph expert : this.experts)
			if (expert.getName().equals(expertName))
				return expert;
		return null;
	}

	public void deleteExpert(String expertName) {
		for (int i = 0; i < experts.size(); i++) {
			if (experts.get(i).getName().equals(expertName)) {
				experts.remove(i);
				break;
			}
		}
	}

	public List<String> getExpertNames() {
		List<String> expertNames = new ArrayList<>();
		for (Graph expert : this.experts)
			expertNames.add(expert.getName());
		return expertNames;
	}

	public void addExpert(Graph expert) {
		this.experts.add(expert);
	}

	public void createDendrogram(Dendrogram dendrogram) throws Exception {
		if (getDendrogram(dendrogram.getName()) != null)
			throw new KeyAlreadyExistsException();
		
		dendrogram.calculateSimilarityMatrix(this.profiles);

		this.addDendrogram(dendrogram);

		//run python script to generate dendrogram image
		Runtime r = Runtime.getRuntime();
		String pythonScriptPath = RESOURCES_PATH + "createDendrogram.py";
		String[] cmd = new String[6];
		cmd[0] = PYTHON;
		cmd[1] = pythonScriptPath;
		cmd[2] = CODEBASES_FOLDER;
		cmd[3] = this.name;
		cmd[4] = dendrogram.getName();
		cmd[5] = dendrogram.getLinkageType();
		
		Process p = r.exec(cmd);
		p.waitFor();
	}

	public void createExpert(Graph expert) throws IOException, JSONException {
		if (getExpert(expert.getName()) != null)
			throw new KeyAlreadyExistsException();

		InputStream is = new FileInputStream(CODEBASES_FOLDER + this.name + ".txt");
		JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();

		Iterator<String> controllers = datafileJSON.sortedKeys();
		Cluster cluster = new Cluster("Generic");
		while (controllers.hasNext()) {
			JSONArray entitiesArray = datafileJSON.getJSONArray(controllers.next());
			for (int i = 0; i < entitiesArray.length(); i++) {
				JSONArray entityArray = entitiesArray.getJSONArray(i);
				String entity = entityArray.getString(0);
				if (!cluster.containsEntity(entity))
					cluster.addEntity(new Entity(entity));
			}
		}
		expert.addCluster(cluster);

		this.addExpert(expert);
	}
}
