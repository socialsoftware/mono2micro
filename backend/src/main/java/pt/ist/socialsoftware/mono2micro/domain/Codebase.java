package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
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

	public JSONObject getMethodCall(JSONArray packages, String callPackage, String callClass, String callSignature)
		throws JSONException
	{
		for (int i = 0; i < packages.length(); i++) {
			JSONObject pack = packages.getJSONObject(i);

			if (pack.getString("name").equals(callPackage)) {
				JSONArray classes = pack.optJSONArray("classes");

				for (int j = 0; j < classes.length(); j++) {
					JSONObject cls = classes.getJSONObject(j);
					String classType = cls.getString("type");

					if (cls.getString("name").equals(callClass)) {
						JSONArray methods = cls.optJSONArray("methods");

						for (int k = 0; k < methods.length(); k++) {
							JSONObject method = methods.getJSONObject(k);

							if (method.getString("signature").equals(callSignature)) {
								method.put("classType", classType);
								return method;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void vectorSum(ArrayList<Double> vector, JSONArray array)
		throws JSONException
	{
		for (int i = 0; i < array.length(); i++) {
			vector.set(i, vector.get(i) + array.getDouble(i));
		}
	}

	public void vectorSum(ArrayList<Double> vector, ArrayList<Double> array)
	{
		for (int i = 0; i < array.size(); i++) {
			vector.set(i, vector.get(i) + array.get(i));
		}
	}

	public void vectorDivision(ArrayList<Double> vector, float count) {
		for (int i = 0; i < vector.size(); i++) {
			vector.set(i, vector.get(i) / count);
		}
	}

	class Acumulator {
		ArrayList<Double> sum;
		float count;
		// HashMap<String, Integer> usedEntities;

		Acumulator(ArrayList<Double> sum, float count) {
			this.sum = sum;
			this.count = count;
			// this.usedEntities = usedEntities;
		}
	};

	public Acumulator getMethodCallsVectors(
		Dendrogram dendrogram,
		JSONArray packages,
		String className,
		String classType,
		JSONObject method,
		int maxDepth
	)
		throws JSONException
	{
		float count = 0;
		ArrayList<Double> vector = new ArrayList<Double>();
		JSONArray code_vector = method.getJSONArray("codeVector");
		JSONArray methodCalls = method.optJSONArray("methodCalls");
		String methodType = method.getString("type");
		// HashMap<String, Integer> usedEntities = new HashMap<>();

		if (methodType.equals("Controller")) {
			count = dendrogram.getControllersWeight();
		} else if (classType.equals("Service") || methodType.equals("Service")) {
			count = dendrogram.getServicesWeight();
		} else if (classType.equals("Entity")) {
			count = dendrogram.getEntitiesWeight();
		} else {
			count = dendrogram.getIntermediateMethodsWeight();
		}

		for (int idx = 0; idx < 384; idx++) {
			vector.add(count * code_vector.getDouble(idx));
		}

		if (maxDepth == 0 || methodCalls.length() == 0) {
			// if (classType.equals("Entity")) {
			// 	usedEntities = new HashMap<>();
			// 	usedEntities.put(className, 1);
			// }
			return new Acumulator(vector, count);
		}

		for (int l = 0; l < methodCalls.length(); l++) {
			JSONObject methodCall = methodCalls.getJSONObject(l);

			try {
				JSONObject met = getMethodCall(
					packages,
					methodCall.getString("packageName"),
					methodCall.getString("className"),
					methodCall.getString("signature")
				);

				if (met != null) {

					Acumulator acum = getMethodCallsVectors(
						dendrogram,
						packages,
						methodCall.getString("className"),
						met.getString("classType"),
						met,
						maxDepth - 1
					);
				
					vectorSum(vector, acum.sum);
					count += acum.count;
					// usedEntities = acum.usedEntities;

				} else {
					System.err.println("[ - ] Cannot get method call for method: " + methodCall.getString("signature"));
				}
			} catch (JSONException je) {
				System.err.println("[ - ] Cannot get method call for method: " + methodCall.getString("signature"));
			}
		}

		// if (classType.equals("Entity")) {
		// 	if (usedEntities.containsKey(className)) {
		// 		usedEntities.put(className, usedEntities.get(className) + 1);
		// 	} else {
		// 		usedEntities.put(className, 1);
		//	}
		// }

		return new Acumulator(vector, count);
	}

	public void methodCallsFeaturesAnalysis(Dendrogram dendrogram, Boolean analysisMode)
		throws JSONException, IOException
	{
		List<HashMap> featuresVectors = new ArrayList<>();
		Integer numberOfEntities = 0;
		HashMap<String, Object> featuresJson = new HashMap<String, Object>();
		JSONObject codeEmbeddings = CodebaseManager.getInstance().getCodeEmbeddings(this.name);
		JSONArray packages = codeEmbeddings.getJSONArray("packages");
		featuresJson.put("name", codeEmbeddings.getString("name"));

		for (int i = 0; i < packages.length(); i++) {
			JSONObject pack = packages.getJSONObject(i);
			JSONArray classes = pack.optJSONArray("classes");

			for (int j = 0; j < classes.length(); j++) {
				JSONObject cls = classes.getJSONObject(j);
				JSONArray methods = cls.optJSONArray("methods");
				String className = cls.getString("name");
				String classType = cls.getString("type");

				if (classType.equals("Entity")) {
					numberOfEntities++;
				}

				for (int k = 0; k < methods.length(); k++) {
					JSONObject method = methods.getJSONObject(k);

					if (method.getString("type").equals("Controller")) {
						System.out.println("Controller: " + method.getString("signature"));

						Acumulator acumulator = getMethodCallsVectors(dendrogram, packages, className, classType, method, dendrogram.getMaxDepth());

						if (acumulator.count > 0) {
							vectorDivision(acumulator.sum, acumulator.count);

							HashMap<String, Object> featureEmbeddings = new HashMap<String, Object>();
							featureEmbeddings.put("package", pack.getString("name"));
							featureEmbeddings.put("class", cls.getString("name"));
							featureEmbeddings.put("signature", method.getString("signature"));
							// featureEmbeddings.put("usedEntities", acumulator.usedEntities);
							featureEmbeddings.put("codeVector", acumulator.sum);
							featuresVectors.add(featureEmbeddings);
						}

					}

				}

			}

		}

		featuresJson.put("maxDepth", dendrogram.getMaxDepth());
		featuresJson.put("controllersWeight", dendrogram.getControllersWeight());
		featuresJson.put("servicesWeight", dendrogram.getServicesWeight());
		featuresJson.put("intermediateMethodsWeight", dendrogram.getIntermediateMethodsWeight());
		featuresJson.put("entitiesWeight", dendrogram.getEntitiesWeight());
		featuresJson.put("linkageType", dendrogram.getLinkageType());
		featuresJson.put("numberOfEntities", numberOfEntities);
		featuresJson.put("features", featuresVectors);

		CodebaseManager.getInstance().writeFeaturesCodeVectorsFile(this.name, featuresJson);

		if (!analysisMode) {
			this.addDendrogram(dendrogram);
			executeCreateDendrogram(dendrogram, "/features/methodsCalls");
		}
	}

	public void entitiesTracesFeaturesAnalysis(Dendrogram dendrogram)
		throws JSONException, IOException
	{
		List<HashMap> entitiesVectors = new ArrayList<>();
		HashMap<String, Object> entitiesJson = new HashMap<String, Object>();
		JSONObject codeEmbeddings = CodebaseManager.getInstance().getCodeEmbeddings(this.name);
		JSONArray packages = codeEmbeddings.getJSONArray("packages");
		entitiesJson.put("name", codeEmbeddings.getString("name"));
		entitiesJson.put("linkageType", dendrogram.getLinkageType());

		for (int i = 0; i < packages.length(); i++) {
			JSONObject pack = packages.getJSONObject(i);
			JSONArray classes = pack.optJSONArray("classes");

			for (int j = 0; j < classes.length(); j++) {
				JSONObject cls = classes.getJSONObject(j);
				JSONArray methods = cls.optJSONArray("methods");
				String classType = cls.getString("type");

				if (classType.equals("Entity")) {
					float count = 0;
					ArrayList<Double> vector = new ArrayList<Double>();
					for (int idx = 0; idx < 384; idx++) {
						vector.add(0.0);
					}

					for (int k = 0; k < methods.length(); k++) {
						JSONObject method = methods.getJSONObject(k);
						JSONArray code_vector = method.getJSONArray("codeVector");
						for (int idx = 0; idx < 384; idx++) {
							vector.set(idx, vector.get(idx) + code_vector.getDouble(idx));
						}
						count += 1;
					}

					if (count > 0) {
						vectorDivision(vector, count);

						HashMap<String, Object> entityEmbeddings = new HashMap<String, Object>();
						entityEmbeddings.put("package", pack.getString("name"));
						entityEmbeddings.put("name", cls.getString("name"));
						entityEmbeddings.put("translationID", cls.getInt("translationID"));
						entityEmbeddings.put("codeVector", vector);
						entitiesVectors.add(entityEmbeddings);
					}

				}

			}

		}

		entitiesJson.put("entities", entitiesVectors);

		CodebaseManager.getInstance().writeEntitiesCodeVectorsFile(this.name, entitiesJson);

		List<HashMap> tracesVectors = new ArrayList<>();
		HashMap<String, Object> tracesJson = new HashMap<String, Object>();
		tracesJson.put("name", codeEmbeddings.getString("name"));
		tracesJson.put("linkageType", dendrogram.getLinkageType());
		JSONObject functionalityTraces = CodebaseManager.getInstance().getFunctionalityTraces(this.name);
		Iterator<String> keys = functionalityTraces.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			if (functionalityTraces.get(key) instanceof JSONObject) {
				JSONObject traces = (JSONObject) functionalityTraces.get(key);
				JSONArray trace = traces.getJSONArray("t");
				JSONObject trace_obj  = trace.getJSONObject(0);
				JSONArray accesses = trace_obj.getJSONArray("a");

				float count = 0;
				ArrayList<Double> vector = new ArrayList<Double>();
				for (int idx = 0; idx < 384; idx++) {
					vector.add(0.0);
				}

				for (int i = 0; i < accesses.length(); i++) {
					JSONArray access = accesses.getJSONArray(i);
					String accessType = access.getString(0);
					Integer entityId = access.getInt(1);
					float weight = 0;

					if (accessType.equals("R")) {
						weight = dendrogram.getReadMetricWeight();
					} else {
						weight = dendrogram.getWriteMetricWeight();
					}

					for (HashMap entity : entitiesVectors) {
						if (entity.get("translationID") == entityId) {
							ArrayList<Double> code_vector = (ArrayList<Double>) entity.get("codeVector");
							for (int idx = 0; idx < 384; idx++) {
								vector.set(idx, vector.get(idx) + weight * code_vector.get(idx));
							}

							count += weight;
							break;
						}
					}
				}
				if (count > 0) {
					vectorDivision(vector, count);
					HashMap<String, Object> traceEmbeddings = new HashMap<String, Object>();
					traceEmbeddings.put("name", key);
					traceEmbeddings.put("codeVector", vector);
					tracesVectors.add(traceEmbeddings);
				}
			}
		}

		tracesJson.put("traces", tracesVectors);

		CodebaseManager.getInstance().writeEntitiesTracesCodeVectorsFile(this.name, tracesJson);

		this.addDendrogram(dendrogram);

		executeCreateDendrogram(dendrogram, "/features/entitiesTraces");
	}

	public void mixedFeaturesAnalysis(Dendrogram dendrogram)
		throws JSONException, IOException
	{

	}

	public void createDendrogramByFeatures(
		Dendrogram dendrogram,
		Boolean analysisMode
	)
		throws Exception
	{
		if (getDendrogram(dendrogram.getName()) != null)
			throw new KeyAlreadyExistsException();

		File dendrogramPath = new File(CODEBASES_PATH + this.name + "/" + dendrogram.getName());
		if (!dendrogramPath.exists()) {
			dendrogramPath.mkdir();
		}

		if (dendrogram.getFeatureVectorizationStrategy().equals("methodCalls")) {
			this.methodCallsFeaturesAnalysis(dendrogram, analysisMode);
		} else if (dendrogram.getFeatureVectorizationStrategy().equals("entitiesTraces")) {
			this.entitiesTracesFeaturesAnalysis(dendrogram);
		} else {
			this.mixedFeaturesAnalysis(dendrogram);
		}

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

	public void createDendrogramByEntities(
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

		List<HashMap> entitiesVectors = new ArrayList<>();
		HashMap<String, Object> entitiesJson = new HashMap<String, Object>();
		JSONObject codeEmbeddings = CodebaseManager.getInstance().getCodeEmbeddings(this.name);
		JSONArray packages = codeEmbeddings.getJSONArray("packages");
		entitiesJson.put("name", codeEmbeddings.getString("name"));
		entitiesJson.put("linkageType", dendrogram.getLinkageType());

		for (int i = 0; i < packages.length(); i++) {
			JSONObject pack = packages.getJSONObject(i);
			JSONArray classes = pack.optJSONArray("classes");

			for (int j = 0; j < classes.length(); j++) {
				JSONObject cls = classes.getJSONObject(j);
				JSONArray methods = cls.optJSONArray("methods");
				String classType = cls.getString("type");

				if (classType.equals("Entity")) {
					float count = 0;
					ArrayList<Double> vector = new ArrayList<Double>();
					for (int idx = 0; idx < 384; idx++) {
						vector.add(0.0);
					}

					for (int k = 0; k < methods.length(); k++) {
						JSONObject method = methods.getJSONObject(k);
						JSONArray code_vector = method.getJSONArray("codeVector");
						for (int idx = 0; idx < 384; idx++) {
							vector.set(idx, vector.get(idx) + code_vector.getDouble(idx));
						}
						count += 1;
					}

					if (count > 0) {
						vectorDivision(vector, count);

						HashMap<String, Object> entityEmbeddings = new HashMap<String, Object>();
						entityEmbeddings.put("package", pack.getString("name"));
						entityEmbeddings.put("name", cls.getString("name"));
						entityEmbeddings.put("translationID", cls.getInt("translationID"));
						entityEmbeddings.put("codeVector", vector);
						entitiesVectors.add(entityEmbeddings);
					}

				}

			}

		}

		entitiesJson.put("entities", entitiesVectors);

		CodebaseManager.getInstance().writeEntitiesCodeVectorsFile(this.name, entitiesJson);

		this.addDendrogram(dendrogram);

		executeCreateDendrogram(dendrogram, "/entities");
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

				if (!class_methods_vectors.isEmpty()) {
					List<Double> class_vector = calculateClassVector(class_methods_vectors);
					HashMap<String, Object> classEmbeddings = new HashMap<String, Object>();
					classEmbeddings.put("package", pack.getString("name"));
					classEmbeddings.put("name", cls.getString("name"));
					classEmbeddings.put("type", cls.getString("type"));
					if (cls.has("translationID")) classEmbeddings.put("translationID", cls.getInt("translationID"));
					classEmbeddings.put("codeVector", class_vector);
					classesVectors.add(classEmbeddings);
				}
			}
		}

		classes_json.put("linkageType", dendrogram.getLinkageType());
		classes_json.put("classes", classesVectors);

		CodebaseManager.getInstance().writeClassesCodeVectorsFile(this.name, classes_json);

		this.addDendrogram(dendrogram);

		executeCreateDendrogram(dendrogram, "/classes");
	}

}
