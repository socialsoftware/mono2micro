package pt.ist.socialsoftware.mono2micro.manager;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.dto.*;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

public class CodebaseManager {

	private static CodebaseManager instance = null;

    private ObjectMapper objectMapper = null;

	private CodebaseManager() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	public static CodebaseManager getInstance() {
        if (instance == null)
        	instance = new CodebaseManager(); 
        return instance; 
	}

	public List<Codebase> getCodebasesWithFields(
		Set<String> deserializableFields
	)
		throws IOException
	{
		List<Codebase> codebases = new ArrayList<>();

		File codebasesPath = new File(CODEBASES_PATH);
		if (!codebasesPath.exists()) {
			codebasesPath.mkdir();
			return codebases;
		}

		File[] files = codebasesPath.listFiles();

		if (files != null) {
			Arrays.sort(files, Comparator.comparingLong(File::lastModified));

			for (File file : files) {
				if (file.isDirectory()) {
					Codebase cb = getCodebaseWithFields(file.getName(), deserializableFields);

					if (cb != null)
						codebases.add(cb);
				}
			}
		}

		return codebases;
	}

	public Codebase getCodebaseWithFields(
		String codebaseName,
		Set<String> deserializableFields
	)
		throws IOException
	{
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setInjectableValues(
			new InjectableValues.Std().addValue("codebaseDeserializableFields", deserializableFields)
		);

		ObjectReader reader = objectMapper.readerFor(Codebase.class);

		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (!codebaseJSONFile.exists())
			return null;

		return reader.readValue(codebaseJSONFile);
	}

	public List<Dendrogram> getCodebaseDendrogramsWithFields(
		String codebaseName,
		Set<String> dendrogramDeserializableFields
	)
		throws IOException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		dendrogramDeserializableFields.add("name");

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", dendrogramDeserializableFields)
		);

		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (!codebaseJSONFile.exists())
			return null;

		ObjectReader reader = objectMapper.readerFor(Codebase.class);
		Codebase cb = reader.readValue(codebaseJSONFile);

		return cb.getDendrograms();
	}

	public Dendrogram getCodebaseDendrogramWithFields(
		String codebaseName,
		String dendrogramName,
		Set<String> dendrogramDeserializableFields
	)
		throws Exception
	{
		return getCodebaseDendrogramsWithFields(
			codebaseName,
			dendrogramDeserializableFields
		)
			.stream()
			.filter(dendrogram -> dendrogram.getName().equals(dendrogramName))
			.findFirst()
			.orElseThrow(() -> new Exception("Dendrogram " + dendrogramName + " not found"));
	}

	public List<Decomposition> getCodebaseDecompositionsWithFields(
		String codebaseName,
		Set<String> decompositionDeserializableFields
	)
		throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", new HashSet<String>() {{ add("decompositions"); }})
				.addValue("decompositionDeserializableFields", decompositionDeserializableFields)
		);

		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (!codebaseJSONFile.exists())
			return null;

		ObjectReader reader = objectMapper.readerFor(Codebase.class);
		Codebase cb = reader.readValue(codebaseJSONFile);

		return cb.getDendrograms()
			.stream()
			.flatMap(dendrogram -> dendrogram.getDecompositions().stream())
			.collect(Collectors.toList());
	}

	public List<Decomposition> getDendrogramDecompositionsWithFields(
		String codebaseName,
		String dendrogramName,
		Set<String> decompositionDeserializableFields
	)
		throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();
		decompositionDeserializableFields.add("name");

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", new HashSet<String>() {{ add("name"); add("decompositions"); }})
				.addValue("decompositionDeserializableFields", decompositionDeserializableFields)
		);

		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (!codebaseJSONFile.exists())
			return null;

		ObjectReader reader = objectMapper.readerFor(Codebase.class);
		Codebase cb = reader.readValue(codebaseJSONFile);

		Dendrogram d = cb.getDendrograms()
							.stream()
							.filter(dendrogram -> dendrogram.getName().equals(dendrogramName))
							.findFirst()
							.orElseThrow(() -> new Exception("Dendrogram " + dendrogramName + " not found"));

		return d.getDecompositions();
	}

	public Decomposition getDendrogramDecompositionWithFields(
		String codebaseName,
		String dendrogramName,
		String decompositionName,
		Set<String> decompositionDeserializableFields
	)
		throws Exception
	{
		return getDendrogramDecompositionsWithFields(
			codebaseName,
			dendrogramName,
			decompositionDeserializableFields
		)
			.stream()
			.filter(decomposition -> decomposition.getName().equals(decompositionName))
			.findFirst()
			.orElseThrow(() -> new Exception("Decomposition " + decompositionName + " not found"));
	}

	public Decomposition getDecompositionWithControllersAndClustersWithFields(
		String codebaseName,
		String dendrogramName,
		String decompositionName,
		Set<String> controllerDeserializableFields,
		Set<String> clusterDeserializableFields
	)
		throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", new HashSet<String>() {{ add("name"); add("decompositions"); }})
				.addValue("decompositionDeserializableFields", new HashSet<String>() {{ add("name"); add("controllers"); add("clusters"); }})
				.addValue("controllerDeserializableFields", controllerDeserializableFields)
				.addValue("clusterDeserializableFields", clusterDeserializableFields)
		);

		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (!codebaseJSONFile.exists())
			return null;

		ObjectReader reader = objectMapper.readerFor(Codebase.class);
		Codebase cb = reader.readValue(codebaseJSONFile);

		Dendrogram d = cb.getDendrograms()
						 .stream()
						 .filter(dendrogram -> dendrogram.getName().equals(dendrogramName))
						 .findFirst()
						 .orElseThrow(() -> new Exception("Dendrogram " + dendrogramName + " not found"));

		return d.getDecompositions().stream()
				.filter(decomposition -> decomposition.getName().equals(decompositionName))
				.findFirst()
				.orElseThrow(() -> new Exception("Decomposition " + decompositionName + " not found"));
	}

	public void deleteCodebase(String codebaseName) throws IOException {
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName));
	}

	public Codebase createCodebase(
		String codebaseName,
		Object datafile,
		Object translationFile,
		Object translationEntityToIdFile,
		Object codeEmbeddingsFile
	)
		throws IOException, JSONException
	{
		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (codebaseJSONFile.exists())
			throw new KeyAlreadyExistsException();

		File codebasesPath = new File(CODEBASES_PATH);
		if (!codebasesPath.exists()) {
			codebasesPath.mkdir();
		}

		File codebasePath = new File(CODEBASES_PATH + codebaseName);
		if (!codebasePath.exists()) {
			codebasePath.mkdir();
		}

		Codebase codebase = new Codebase(codebaseName);

		HashMap datafileJSON;

		File datafileFile = null;

		if (datafile instanceof MultipartFile) {
			// read datafile
			InputStream datafileInputStream = ((MultipartFile) datafile).getInputStream();
			datafileJSON = objectMapper.readValue(datafileInputStream, HashMap.class);
			datafileInputStream.close();

			InputStream translationFileInputStream = ((MultipartFile) translationFile).getInputStream();
			HashMap translationFileJSON = objectMapper.readValue(translationFileInputStream, HashMap.class);
			translationFileInputStream.close();
			this.writeTranslationFile(codebaseName, translationFileJSON);

			if (translationEntityToIdFile instanceof MultipartFile) {
				InputStream translationEntityToIdFileInputStream = ((MultipartFile) translationEntityToIdFile).getInputStream();

				HashMap translationEntityToIdFileJSON = objectMapper.readValue(translationEntityToIdFileInputStream, HashMap.class);

				translationEntityToIdFileInputStream.close();
				this.writeTranslationEntityToIdFile(codebaseName, translationEntityToIdFileJSON);
			}

			if (codeEmbeddingsFile instanceof MultipartFile) {
				InputStream codeEmbeddingsFileInputStream = ((MultipartFile) codeEmbeddingsFile).getInputStream();
				
				JSONObject codeEmbeddings = new JSONObject(IOUtils.toString(codeEmbeddingsFileInputStream, StandardCharsets.UTF_8));
				
				matchEntitiesTranslationIds(codebaseName, codeEmbeddings);

				codeEmbeddingsFileInputStream.close();
				this.writeCodeEmbeddingsFile(codebaseName, objectMapper.readValue(codeEmbeddings.toString(), HashMap.class));
			}

			this.writeDatafile(codebaseName, datafileJSON);
			datafileFile = new File(CODEBASES_PATH + codebaseName + "/datafile.json");
			codebase.setDatafilePath(datafileFile.getAbsolutePath());
		}

		else if (datafile instanceof String) {
			datafileFile = new File((String) datafile);

			if (!datafileFile.exists())
				throw new FileNotFoundException();

			codebase.setDatafilePath((String) datafile);
		}

		codebase.addProfile("Generic", Utils.getJsonFileKeys(datafileFile));

		return codebase;
	}

	public Codebase getCodebase(
		String codebaseName
	)
		throws IOException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/codebase.json");

		Codebase codebase = objectMapper.readerFor(Codebase.class).readValue(is);
		is.close();

		return codebase;
	}

	public void writeCodebase(Codebase codebase) throws IOException {
		objectMapper.writeValue(
			new File(CODEBASES_PATH + codebase.getName() + "/codebase.json"),
			codebase
		);
	}

	public HashMap<String, ControllerDto> getDatafile(
		Codebase codebase
	)
		throws IOException
	{
		InputStream is = new FileInputStream(codebase.getDatafilePath());

		HashMap<String, ControllerDto> datafile = objectMapper.readerFor(
			new TypeReference<HashMap<String, ControllerDto>>() {}
		).readValue(is);

		is.close();

		return datafile;
	}

	public JSONObject getSimilarityMatrix(
		String codebaseName,
		String dendrogramName
	)
		throws IOException, JSONException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/similarityMatrix.json");

		JSONObject similarityMatrixJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));

		is.close();

		return similarityMatrixJSON;
	}

	public void writeTranslationFile(
			String codebaseName,
			HashMap translationFile
	)
			throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + "/translation.json"),
				translationFile
		);
	}

	public String getTranslation(
			String codebaseName
	)
		throws IOException
	{

		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/translation.json");

		String translation = IOUtils.toString(is, "UTF-8");

		is.close();

		return translation;
	}

	public void writeTranslationEntityToIdFile(
		String codebaseName,
		HashMap translationEntityToIdFile
	)
		throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
			new File(CODEBASES_PATH + codebaseName + "/translationEntityToId.json"),
			translationEntityToIdFile
		);
	}

	public String getTranslationEntityToId(
			String codebaseName
	)
		throws IOException
	{

		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/translationEntityToId.json");

		String translationEntityToId = IOUtils.toString(is, "UTF-8");

		is.close();

		return translationEntityToId;
	}

	public void writeCodeEmbeddingsFile(
		String codebaseName,
		HashMap codeEmbeddingsFileJSON
	)
		throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + "/code_embeddings.json"),
				codeEmbeddingsFileJSON
		);
	}

	public JSONObject getCodeEmbeddings(
			String codebaseName
	)
		throws IOException, JSONException
	{

		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/code_embeddings.json");

		JSONObject codeEmbeddings = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

		is.close();

		return codeEmbeddings;
	}

	public void writeFeaturesCodeVectorsFile(
		String codebaseName,
		HashMap featuresCodeVectorsFileJSON,
		Integer threadNumber
	)
		throws IOException
	{
		String featuresEmbeddingsFileName = "/features_embeddings";

		if (threadNumber != null) {
			featuresEmbeddingsFileName += "_t" + threadNumber.toString();
		}

		featuresEmbeddingsFileName += ".json";
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + featuresEmbeddingsFileName),
				featuresCodeVectorsFileJSON
		);
	}

	public void writeEntitiesCodeVectorsFile(
			String codebaseName,
			HashMap entitiesCodeVectorsFileJSON
	)
			throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + "/entities_embeddings.json"),
				entitiesCodeVectorsFileJSON
		);
	}

	public void writeEntitiesTracesCodeVectorsFile(
			String codebaseName,
			HashMap entitiesTracesCodeVectorsFileJSON
	)
			throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + "/entities_traces_embeddings.json"),
				entitiesTracesCodeVectorsFileJSON
		);
	}

	public void writeClassesCodeVectorsFile(
		String codebaseName,
		HashMap classesCodeVectorsFileJSON
	)
		throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + "/classes_embeddings.json"),
				classesCodeVectorsFileJSON
		);
	}

	public void writeMixedCodeVectorsFile(
		String codebaseName,
		HashMap mixedFileJSON,
		Integer threadNumber
	)
		throws IOException
	{
		String mixedEmbeddingsFileName = "/mixed_embeddings";

		if (threadNumber != null) {
			mixedEmbeddingsFileName += "_t" + threadNumber.toString();
		}

		mixedEmbeddingsFileName += ".json";
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				new File(CODEBASES_PATH + codebaseName + mixedEmbeddingsFileName),
				mixedFileJSON
		);
	}

	public void writeDatafile(
		String codebaseName,
		HashMap datafile
	)
		throws IOException
	{
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
			new File(CODEBASES_PATH + codebaseName + "/datafile.json"),
			datafile
		);
	}

	public JSONObject getFunctionalityTraces(
			String codebaseName
	)
			throws IOException, JSONException
	{

		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/datafile.json");

		JSONObject codeEmbeddings = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

		is.close();

		return codeEmbeddings;
	}

	public void writeDendrogramSimilarityMatrix(
		String codebaseName,
		String dendrogramName,
		JSONObject similarityMatrix
	)
		throws IOException, JSONException
	{
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/similarityMatrix.json");
		file.write(similarityMatrix.toString(4));
		file.close();
	}

	public byte[] getDendrogramImage(
		String codebaseName,
		String dendrogramName
	)
		throws IOException
	{
		String filePathname = CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/dendrogramImage.png";
		Path filePath = Paths.get(filePathname);

		if (Files.exists(filePath)) return Files.readAllBytes(filePath);

		throw new FileNotFoundException("File: " + filePathname + " not found");

	}

	public JSONObject getClusters(
		String codebaseName,
		String dendrogramName,
		String decompositionName
	)
		throws IOException, JSONException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/" + decompositionName + "/clusters.json");

		JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

		is.close();

		return clustersJSON;
	}

	public JSONObject getAnalyserResults(
		String codebaseName
	)
			throws IOException, JSONException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");

		JSONObject analyserResults = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

		is.close();

		return analyserResults;
	}

	public boolean analyserResultFileAlreadyExists(
		String codebaseName
	) {
		return new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json").exists();
	}

	public void writeAnalyserResults(
		String codebaseName,
		HashMap analyserJSON
	)
		throws IOException
	{
		DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
		ObjectWriter writer = objectMapper.writer(pp);
		writer.writeValue(new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json"), analyserJSON);
	}

	public HashMap<String, HashMap<String, Set<Short>>> getAnalyserCut(
		String codebaseName,
		String cutName
	)
		throws IOException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/cuts/" + cutName + ".json");

		HashMap<String, HashMap<String, Set<Short>>> value = objectMapper.readValue(
			is,
			new TypeReference<HashMap<String, HashMap<String, Set<Short>>>>() {}
		);

		is.close();
		return value;
	}

	public void writeAnalyserSimilarityMatrix(
		String codebaseName,
		SimilarityMatrixDto similarityMatrix
	)
		throws IOException
	{
		DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
		ObjectWriter writer = objectMapper.writer(pp);

		OutputStream os = new FileOutputStream(CODEBASES_PATH + codebaseName + "/analyser/similarityMatrix.json");

		writer.writeValue(os, similarityMatrix);
		os.close();
	}

	public boolean analyserSimilarityMatrixFileAlreadyExists(
		String codebaseName
	) {
		return new File(CODEBASES_PATH + codebaseName + "/analyser/similarityMatrix.json").exists();
	}

	public SimilarityMatrixDto getSimilarityMatrixDtoWithFields(
		String codebaseName,
		Set<String> deserializableFields
	)
		throws IOException
	{
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setInjectableValues(
			new InjectableValues.Std().addValue(
				"similarityMatrixDtoDeserializableFields",
				deserializableFields
			)
		);

		ObjectReader reader = objectMapper.readerFor(SimilarityMatrixDto.class);

		File similarityMatrixFile = new File(CODEBASES_PATH + codebaseName + "/analyser/similarityMatrix.json");

		if (!similarityMatrixFile.exists())
			return null;

		return reader.readValue(similarityMatrixFile);
	}

	public Map<String, Controller> getControllersWithCostlyAccesses(
		Codebase codebase,
		String profile,
		Map<Short, Short> entityIDToClusterID
	)
		throws IOException
	{
		// System.out.println("Getting controllers with costly accesses...");

		Map<String, Controller> controllers = new HashMap<>();

		File jsonFile = new File(codebase.getDatafilePath());

		Set<String> profileControllers = codebase.getProfile(profile);
		JsonFactory jsonfactory = objectMapper.getFactory();

		JsonParser jsonParser = jsonfactory.createParser(jsonFile);
		JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT

		if (jsonToken != JsonToken.START_OBJECT) {
			Utils.print("Json must start with a left curly brace", Utils.lineno());
			System.exit(-1);
		}

		while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
			if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
//				Utils.print("Controller name: " + jsonParser.getCurrentName(), Utils.lineno());

				if (!profileControllers.contains(jsonParser.getCurrentName())) { // FIXME TEST ME
					jsonParser.skipChildren();
					continue;
				}

				Controller controller = new Controller(jsonParser.getCurrentName());

				while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
//					Utils.print("field name: " + jsonParser.getCurrentName(), Utils.lineno());

					switch (jsonParser.getCurrentName()) {
						case "f":
							break;
						case "t": // array of traces

							while (jsonParser.nextValue() != JsonToken.END_ARRAY) { // iterate over trace objects
								while (jsonParser.nextValue() != JsonToken.END_OBJECT) { // iterate over trace object fields

									switch (jsonParser.getCurrentName()) {
										case "id":
										case "f":
											break;

										case "a":
											Map<Short, Byte> entityIDToMode = new HashMap<>();
											short previousCluster = -2;
											int i = 0;

											while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
												ReducedTraceElementDto rte = jsonParser.readValueAs(
													ReducedTraceElementDto.class
												);

												if (rte instanceof AccessDto) {
													AccessDto access = (AccessDto) rte;
													short entityID = access.getEntityID();
													byte mode = access.getMode();
													Short cluster;

													cluster = entityIDToClusterID.get(entityID);

													if (cluster == null) {
														System.err.println("Entity " + entityID + " is not assign to a cluster.");
														System.exit(-1);
													}

													if (i == 0) {
														entityIDToMode.put(entityID, mode);
														controller.addEntity(entityID, mode);

													} else {

														if (cluster == previousCluster) {
															boolean hasCost = false;
															Byte savedMode = entityIDToMode.get(entityID);

															if (savedMode == null) {
																hasCost = true;

															} else {
																if (savedMode == 1 && mode == 2) // "R" -> 1, "W" -> 2
																	hasCost = true;
															}

															if (hasCost) {
																entityIDToMode.put(entityID, mode);
																controller.addEntity(entityID, mode);
															}

														} else {
															controller.addEntity(entityID, mode);

															entityIDToMode.clear();
															entityIDToMode.put(entityID, mode);

														}
													}

													previousCluster = cluster;
													i++;
												}
											}

											break;

										default:
											Utils.print(
												"Unexpected field name when parsing Trace: " + jsonParser.getCurrentName(),
												Utils.lineno()
											);

											System.exit(-1);
									}
								}
							}


							break;

						default:
							Utils.print(
								"Unexpected field name when parsing Controller: " + jsonParser.getCurrentName(),
								Utils.lineno()
							);
							System.exit(-1);
					}
				}

				// only consider controllers that touch domain entities
				if (!controller.getEntities().isEmpty()) {
					controllers.put(
						controller.getName(),
						controller
					);
				}
			}
		}

		return controllers;
	}

	public void matchEntitiesTranslationIds(String codebaseName, JSONObject codeEmbeddings)
		throws JSONException, IOException
	{
		String translationEntityToIdFile = getTranslationEntityToId(codebaseName);
		JSONObject translationEntityToIdJson = new JSONObject(translationEntityToIdFile);
		JSONArray packages = codeEmbeddings.getJSONArray("packages");
		
		for (int i = 0; i < packages.length(); i++) {
			JSONObject pack = packages.getJSONObject(i);
			JSONArray classes = pack.optJSONArray("classes");

			for (int j = 0; j < classes.length(); j++) {
				JSONObject cls = classes.getJSONObject(j);
				String className = cls.getString("name");

				if (translationEntityToIdJson.has(className)) {
					int entityId = translationEntityToIdJson.getInt(className);
					cls.put("type", "Entity");
					cls.put("translationID", entityId);
				}
			}
		}
	} 
}
