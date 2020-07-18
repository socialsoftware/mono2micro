package pt.ist.socialsoftware.mono2micro.domain;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.PYTHON;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RESOURCES_PATH;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.apache.commons.io.FileUtils;

public class Codebase {
	private String name;
	private Map<String,List<String>> profiles = new HashMap<>();
	private List<Dendrogram> dendrograms = new ArrayList<>();
	private String analysisType;

	public Codebase() {
	}

	public Codebase(String name) {
        this.name = name;
	}

	public Codebase(String name, String analysisType) {
		if (!analysisType.equals("static") && !analysisType.equals("dynamic")) {
			throw new Error("Unknown analysis type: Please choose either 'static' or 'dynamic'");
		}

		this.name = name;
		this.analysisType = analysisType;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnalysisType() { return this.analysisType; }

	public void setAnalysisType(String analysisType) {
		this.analysisType = analysisType;
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
		if (this.profiles.containsKey(profileName)) {
			throw new KeyAlreadyExistsException();
		}
		this.profiles.put(profileName, controllers);
	}
	
	public void deleteProfile(String profileName) {
		this.profiles.remove(profileName);
	}

	public void moveControllers(String[] controllers, String targetProfile) {
        for (String profile : this.profiles.keySet()) {
			for (String controller : controllers) {
				this.profiles.get(profile).remove(controller);
			}
		}
		for (String controller : controllers)
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
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + this.name + "/" + dendrogramName));
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

	public void createDendrogram(Dendrogram dendrogram) throws Exception {
		if (getDendrogram(dendrogram.getName()) != null)
			throw new KeyAlreadyExistsException();

		File dendrogramPath = new File(CODEBASES_PATH + this.name + "/" + dendrogram.getName());
		if (!dendrogramPath.exists()) {
			dendrogramPath.mkdir();
		}

		this.addDendrogram(dendrogram);

		dendrogram.calculateSimilarityMatrix();

		//run python script to generate dendrogram image
		Runtime r = Runtime.getRuntime();
		String pythonScriptPath = RESOURCES_PATH + "createDendrogram.py";
		String[] cmd = new String[5];
		cmd[0] = PYTHON;
		cmd[1] = pythonScriptPath;
		cmd[2] = CODEBASES_PATH;
		cmd[3] = this.name;
		cmd[4] = dendrogram.getName();

		Process p = r.exec(cmd);
		p.waitFor();
	}
}
