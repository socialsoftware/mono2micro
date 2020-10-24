package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.CodebaseDeserializer;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
@JsonDeserialize(using = CodebaseDeserializer.class)
public class Codebase {
	private String name;
	private Map<String, Set<String>> profiles = new HashMap<>(); // e.g <Generic, ControllerNamesList> change to Set
	private Map<String, Controller> controllers = new HashMap<>(); // <controllerName, Controller>
	private List<Dendrogram> dendrograms = new ArrayList<>();
	private String datafilePath;

	public Codebase() {}

	public Codebase(String name) {
        this.name = name;
	}

	public Codebase(
		String name,
		String analysisType
	) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Controller> getControllers() { return controllers; }

	public void setControllers(Map<String, Controller> controllers) { this.controllers = controllers; }

	public String getDatafilePath() {
		return datafilePath;
	}

	public void setDatafilePath(String datafilePath) {
		this.datafilePath = datafilePath;
	}

	public Map<String, Set<String>> getProfiles() {
		return this.profiles;
    }

    public Set<String> getProfile(String profileName) { return this.profiles.get(profileName); }

	public void setProfiles(Map<String, Set<String>> profiles) {
		this.profiles = profiles;
	}
	
	public void addProfile(
		String profileName,
		Set<String> controllers
	) {
		if (this.profiles.containsKey(profileName)) {
			throw new KeyAlreadyExistsException();
		}

		this.profiles.put(profileName, controllers);
	}
	
	public void deleteProfile(String profileName) {
		this.profiles.remove(profileName);
	}

	public void moveControllers(
		String[] controllers,
		String targetProfile
	) {
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

	@JsonIgnore
	public List<String> getDendrogramNames() {
		List<String> dendrogramNames = new ArrayList<>();

		for (Dendrogram dendrogram : this.dendrograms)
			dendrogramNames.add(dendrogram.getName());

		return dendrogramNames;
	}

	public void addDendrogram(Dendrogram dendrogram) {
		this.dendrograms.add(dendrogram);
	}

	public void executeCreateDendrogramPythonScript(
		Dendrogram dendrogram
	)
		throws InterruptedException, IOException
	{
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

	public void createDendrogram(
		Dendrogram dendrogram
	)
		throws Exception
	{
		if (getDendrogram(dendrogram.getName()) != null)
			throw new KeyAlreadyExistsException();

		File dendrogramPath = new File(CODEBASES_PATH + this.name + "/" + dendrogram.getName());
		if (!dendrogramPath.exists()) {
			dendrogramPath.mkdir();
		}

		this.addDendrogram(dendrogram);

		Utils.GetDataToBuildSimilarityMatrixResult result = Utils.getDataToBuildSimilarityMatrix(
			this,
			dendrogram.getProfile(),
			dendrogram.getTracesMaxLimit(),
			dendrogram.getTypeOfTraces()
		);

		CodebaseManager.getInstance().writeDendrogramSimilarityMatrix(
			this.name,
			dendrogram.getName(),
			dendrogram.getMatrixData(
				result.entities,
				result.e1e2PairCount,
				result.entityControllers
			)
		);

		executeCreateDendrogramPythonScript(dendrogram);
	}


}
