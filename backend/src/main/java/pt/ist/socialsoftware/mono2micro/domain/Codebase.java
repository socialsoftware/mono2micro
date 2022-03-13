package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
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
	private List<Dendrogram> dendrograms = new ArrayList<>();
	private String datafilePath;

	public Codebase() {}

	public Codebase(String name) {
        this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public void executeCreateDendrogram(Dendrogram dendrogram, String type)
	{
		WebClient.create(SCRIPTS_ADDRESS)
				.get()
				.uri("/scipy/{codebaseName}/{dendrogramName}/createDendrogram" + type,this.name, dendrogram.getName())
				.exchange()
				.doOnSuccess(clientResponse -> {
					if (clientResponse.statusCode() != HttpStatus.OK)
						throw new RuntimeException("Error Code:" + clientResponse.statusCode());
				}).block();
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
			dendrogram.getTraceType()
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

		executeCreateDendrogram(dendrogram, "");
	}

	public void createDendrogramByFeatures(
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

		JSONObject codeEmbeddings = CodebaseManager.getInstance().getCodeEmbeddings(this.name);

		this.addDendrogram(dendrogram);

		System.out.println("Creating dendrogram by features...");
		// TODO: To implement
	}

	public List<Double> calculateClassVector(List<List<Double>> class_methods_vectors) {
		List<Double> class_vector = new ArrayList<Double>();

		int len = class_methods_vectors.get(0).size();

		Double sum = 0.0;

		for (int i = 0; i < len; i++) {
			for (int j = 0; j < class_methods_vectors.size(); j++) {
				sum += class_methods_vectors.get(j).get(i);
			}
			class_vector.add(sum / class_methods_vectors.size());
			sum = 0.0;
		}

		return class_vector;
	}

	public void createDendrogramByClass(
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

		JSONObject codeEmbeddings = CodebaseManager.getInstance().getCodeEmbeddings(this.name);
		HashMap<String, Object> classes_json = new HashMap<String, Object>();
		classes_json.put("name", codeEmbeddings.getString("name"));

		List<HashMap> classesVectors = new ArrayList<>();

		JSONArray packages = codeEmbeddings.getJSONArray("packages");

		for (int i = 0; i < packages.length(); i++) {
			JSONObject pack = packages.getJSONObject(i);
			JSONArray classes = pack.optJSONArray("classes");
			for (int j = 0; j < classes.length(); j++) {
				JSONObject cls = classes.getJSONObject(j);
				JSONArray methods = cls.optJSONArray("methods");
				List<List<Double>> class_methods_vectors = new ArrayList<List<Double>>();
				for (int k = 0; k < methods.length(); k++) {
					JSONObject method = methods.getJSONObject(k);
					JSONArray code_vector_array = method.optJSONArray("codeVector");
					List<Double> code_vector = new ArrayList<Double>();
					for (int l = 0; l < code_vector_array.length(); l++) {
						code_vector.add(code_vector_array.getDouble(l));
					}
					class_methods_vectors.add(code_vector);
				}
				List<Double> class_vector = calculateClassVector(class_methods_vectors);
				HashMap<String, Object> classEmbeddings = new HashMap<String, Object>();
				classEmbeddings.put("package", pack.getString("name"));
				classEmbeddings.put("name", cls.getString("name"));
				classEmbeddings.put("type", cls.getString("type"));
				classEmbeddings.put("codeVector", class_vector);
				classesVectors.add(classEmbeddings);
			}
		}

		classes_json.put("classes", classesVectors);

		CodebaseManager.getInstance().writeClassesCodeVectorsFile(this.name, classes_json);

		this.addDendrogram(dendrogram);

		executeCreateDendrogram(dendrogram, "/classes");
	}

}
