package pt.ist.socialsoftware.mono2micro.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.source.Source;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.*;

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

    private final ObjectMapper objectMapper;

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

	public List<Strategy> getCodebaseStrategies(
			String codebaseName,
			String strategyFolder,
			List<String> strategyTypes			// Use null when specifying all strategy types
	)
			throws IOException
	{
		List<Strategy> strategies = new ArrayList<>();

		File strategiesPath = new File(CODEBASES_PATH + codebaseName + strategyFolder);

		File[] files = strategiesPath.listFiles();

		if (files != null) {
			Arrays.sort(files, Comparator.comparingLong(File::lastModified));

			for (File file : files) {
				if (file.isDirectory() && sameType(file.getName(), strategyTypes)) {
					Strategy strategy = getCodebaseStrategy(codebaseName, strategyFolder, file.getName());

					if (strategy != null)
						strategies.add(strategy);
				}
			}
		}

		return strategies;
	}

	public boolean sameType(String strategyName, List<String> strategyTypes) {
		if (strategyTypes == null) return true;

		for (String strategyType: strategyTypes)
			if (strategyName.startsWith(strategyType))
				return true;
		return false;
	}

	public void createDecompositionDirectory(String codebaseName, String strategyFolder, String strategyName, String decompositionName) {
		new File(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/decompositions/" + decompositionName).mkdir();
	}

	public void deleteCodebaseStrategy(String codebaseName, String strategyName) throws IOException {
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + strategyName));
	}

	public void deleteCodebaseStrategies(String codebaseName, List<String> possibleStrategies) throws IOException {

		File strategiesPath = new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER);

		File[] files = strategiesPath.listFiles();

		if (files != null) {
			Arrays.sort(files, Comparator.comparingLong(File::lastModified));

			for (File file : files)
				if (file.isDirectory() && sameType(file.getName(), possibleStrategies))
					FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + file.getName()));
		}
	}

	public void deleteStrategyDecomposition(String codebaseName, String strategyName, String decompositionName) throws IOException {
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + strategyName + "/decompositions/" + decompositionName));
	}

	public List<Decomposition> getCodebaseDecompositions(
		String codebaseName,
		String strategyType			// Use "" when specifying all strategy types
	)
		throws Exception
	{
		List<Decomposition> decompositions = new ArrayList<>();

		File strategiesPath = new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER);

		File[] files = strategiesPath.listFiles();

		if (files != null) {
			Arrays.sort(files, Comparator.comparingLong(File::lastModified));

			for (File file : files) {
				if (file.isDirectory() && file.getName().startsWith(strategyType)) {
					List<Decomposition> strategyDecompositions = getStrategyDecompositions(codebaseName, file.getName());

					if (!strategyDecompositions.isEmpty())
						decompositions.addAll(strategyDecompositions);
				}
			}
		}

		return decompositions;
	}

	public Decomposition getStrategyDecomposition(
		String codebaseName,
		String strategyFolder,
		String strategyName,
		String decompositionName
	)
		throws IOException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/decompositions/" + decompositionName + "/decomposition.json");

		Decomposition decomposition = objectMapper.readerFor(Decomposition.class).readValue(is);
		is.close();

		return decomposition;
	}

	public List<Decomposition> getStrategyDecompositions(
		String codebaseName,
		String strategyName
	)
		throws Exception
	{
		Strategy strategy = getCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyName);

		return strategy.getDecompositionsNames().stream()
				.map(decompositionName -> {
					try {
						return getStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void writeStrategyDecomposition(String codebaseName, String strategyFolder, String strategyName, Decomposition decomposition) throws IOException {
		objectMapper.writeValue(
				new File(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/decompositions/" + decomposition.getName() + "/decomposition.json"),
				decomposition
		);
	}

	public Decomposition getDecompositionWithFunctionalitiesAndClustersWithFields(
		String codebaseName,
		String decompositionName,
		Set<String> functionalityDeserializableFields,
		Set<String> clusterDeserializableFields
	)
		throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setInjectableValues( new InjectableValues.Std()
				.addValue("functionalityDeserializableFields", functionalityDeserializableFields)
				.addValue("clusterDeserializableFields", clusterDeserializableFields)
		);

		File decompositionJSONFile = new File(CODEBASES_PATH + codebaseName + "/decompositions/" + decompositionName + "/decomposition.json");

		if (!decompositionJSONFile.exists())
			return null;

		ObjectReader reader = objectMapper.readerFor(Decomposition.class);
		return reader.readValue(decompositionJSONFile);
	}

	public void deleteCodebase(String codebaseName) throws IOException {
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName));
	}

	public Codebase createCodebase(String codebaseName) {
		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (codebaseJSONFile.exists())
			throw new KeyAlreadyExistsException();

		new File(CODEBASES_PATH).mkdir();

		new File(CODEBASES_PATH + codebaseName).mkdir();

		new File(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER).mkdir();

		new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER).mkdir();

		new File(CODEBASES_PATH + codebaseName + "/sources").mkdir();

		Codebase codebase = new Codebase(codebaseName);

		return codebase;
	}

	public String writeInputFile(String codebaseName, String sourceType, Object inputFile) throws IOException {
		InputStream inputFileInputStream = ((MultipartFile) inputFile).getInputStream();
		HashMap inputFileJSON = objectMapper.readValue(inputFileInputStream, HashMap.class);
		inputFileInputStream.close();
		File sourcePath = new File(CODEBASES_PATH + codebaseName + "/sources/" + sourceType);
		if (!sourcePath.exists())
			sourcePath.mkdir();

		File inputFileDestination = new File(CODEBASES_PATH + codebaseName + "/sources/" + sourceType + "/" + sourceType + ".json");
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				inputFileDestination,
				inputFileJSON
		);
		return inputFileDestination.getAbsolutePath();
	}

	public void writeSource(String codebaseName, String sourceType, Source source) throws IOException {
		objectMapper.writeValue( new File(CODEBASES_PATH + codebaseName + "/sources/" + sourceType + "/source.json"), source);
	}

	public void deleteSources(String codebaseName, List<String> sources) throws IOException {
		for (String source : sources)
			FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName + "/sources/" + source));
	}

	public List<Source> getCodebaseSources(String codebaseName) throws IOException {
		List<Source> sources = new ArrayList<>();

		File sourcesPath = new File(CODEBASES_PATH + codebaseName + "/sources");

		File[] files = sourcesPath.listFiles();

		if (files != null) {
			Arrays.sort(files, Comparator.comparingLong(File::lastModified));

			for (File file : files) {
				if (file.isDirectory()) {
					Source source = getCodebaseSource(codebaseName, file.getName());

					if (source != null)
						sources.add(source);
				}
			}
		}
		return sources;
	}


	public Source getCodebaseSource(String codebaseName, String sourceType) throws IOException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/sources/" + sourceType + "/source.json");

		Source source = objectMapper.readerFor(Source.class).readValue(is);
		is.close();

		return source;
	}

	public List<String> getSimilarityMatricesNames(String codebaseName, String strategyFolder, String strategyName) {
		String[] names = new File(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/similarityMatrices").list();
		if (names != null)
			return Arrays.asList(names);
		throw new RuntimeException("No similarity matrices found.");
	}

	public Strategy createCodebaseStrategy(String codebaseName, String strategyFolder, Strategy strategy) throws IOException {

		File path;
		int id = 0;

		do {
			path = new File(CODEBASES_PATH + codebaseName + strategyFolder + strategy.getType() + ++id);
		} while (path.exists() && path.isDirectory());

		strategy.setName(strategy.getType() + id);

		new File(CODEBASES_PATH + codebaseName + strategyFolder + strategy.getName()).mkdir();
		new File(CODEBASES_PATH + codebaseName + strategyFolder + strategy.getName() + "/decompositions").mkdir();
		new File(CODEBASES_PATH + codebaseName + strategyFolder + strategy.getName() + "/similarityMatrices").mkdir();
		strategy.setDecompositionsNames(new ArrayList<>());

		objectMapper.writeValue(new File(CODEBASES_PATH + codebaseName + strategyFolder + strategy.getName() + "/strategy.json"), strategy);

		return strategy;
	}

	public void writeCodebaseStrategy(String codebaseName, String strategyFolder, Strategy strategy) throws IOException {
		objectMapper.writeValue(new File(CODEBASES_PATH + codebaseName + strategyFolder + strategy.getName() + "/strategy.json"), strategy);
	}

	public Strategy getCodebaseStrategy(String codebaseName, String strategyFolder, String strategyName) throws IOException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/strategy.json");

		Strategy strategy = objectMapper.readerFor(Strategy.class).readValue(is);
		is.close();

		return strategy;
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

	public JSONObject getSimilarityMatrix(
		String codebaseName,
		String strategyName,
		String similarityMatrixName
	)
		throws IOException, JSONException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + strategyName + "/similarityMatrices" + similarityMatrixName);

		JSONObject similarityMatrixJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));

		is.close();

		return similarityMatrixJSON;
	}

	public String getInputFile(
			String codebaseName,
			String sourceType
	)
		throws IOException
	{
		Source source = getCodebaseSource(codebaseName, sourceType);

		InputStream is = new FileInputStream(source.getInputFilePath());

		String inputFileContent = IOUtils.toString(is, "UTF-8");

		is.close();

		return inputFileContent;
	}

	public void writeSimilarityMatrix(
		String codebaseName,
		String strategyFolder,
		String strategyName,
		String similarityMatrixName,
		JSONObject similarityMatrix
	)
		throws IOException, JSONException
	{
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/similarityMatrices/" + similarityMatrixName);
		file.write(similarityMatrix.toString(4));
		file.close();
	}

	public byte[] getDendrogramImage(
		String codebaseName,
		String strategyName
	)
		throws IOException
	{
		String filePathname = CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + strategyName + "/dendrogramImage.png";
		Path filePath = Paths.get(filePathname);

		if (Files.exists(filePath)) return Files.readAllBytes(filePath);

		throw new FileNotFoundException("File: " + filePathname + " not found");

	}

	public JSONObject getClusters(
		String codebaseName,
		String strategyFolder,
		String strategyName,
		String decompositionName
	)
		throws IOException, JSONException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/decompositions/" + decompositionName + "/clusters.json");

		JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

		is.close();

		return clustersJSON;
	}

	public HashMap<String, CutInfoDto> getAnalyserResults(
		String codebaseName
	)
		throws IOException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");

		HashMap<String, CutInfoDto> analyserResults = objectMapper.readValue(
			is,
			new TypeReference<HashMap<String, CutInfoDto>>() {}
		);

		is.close();

		return analyserResults;
	}

	public boolean analyserResultFileAlreadyExists(
		String codebaseName
	) {
		return new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json").exists();
	}

	public synchronized void writeRecommendationResults(
		String codebaseName,
		String strategyName,
		JSONArray recommendationJSON
	)
		throws IOException, JSONException
	{
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER + strategyName + "/recommendationResult.json");
		file.write(recommendationJSON.toString(4));
		file.close();
	}

	public Decomposition transferDecompositionFromRecommendation(
		String codebaseName,
		String recommendationStrategyName,
		String recommendationDecompositionName,
		String finalDecompositionName,
		Strategy strategy
	)
		throws IOException
	{

		FileUtils.copyDirectory(
			new File(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER + recommendationStrategyName + "/decompositions/" + recommendationDecompositionName),
			new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + strategy.getName() + "/decompositions/" + finalDecompositionName)
		);

		Decomposition decomposition = getStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategy.getName(), finalDecompositionName);
		decomposition.setName(finalDecompositionName);
		decomposition.setStrategyName(strategy.getName());
		strategy.addDecompositionName(decomposition.getName());

		return decomposition;
	}

	public void transferSimilarityMatrixFromRecommendation(
		String codebaseName,
		String recommendationStrategyName,
		String similarityMatrixName,
		String finalSimilarityMatrixName,
		Strategy strategy
	)
		throws IOException
	{
		File destination = new File(CODEBASES_PATH + codebaseName + STRATEGIES_FOLDER + strategy.getName() + "/similarityMatrices/" + finalSimilarityMatrixName);

		if (destination.exists())
			return;

		FileUtils.copyFile(
			new File(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER + recommendationStrategyName + "/similarityMatrices/" + similarityMatrixName),
			destination
		);
	}
	public JSONArray getRecommendationResultAsJSON(
		String codebaseName,
		String strategyName
	)
		throws IOException, JSONException
	{
		File recommendationResult = new File(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER + strategyName + "/recommendationResult.json");
		if (recommendationResult.exists()) {
			InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER + strategyName + "/recommendationResult.json");

			return new JSONArray(IOUtils.toString(is, "UTF-8"));
		}
		else return new JSONArray();
	}

	public synchronized String getRecommendationResult(
		String codebaseName,
		String strategyName
	)
		throws IOException, JSONException
	{
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + RECOMMEND_FOLDER + strategyName + "/recommendationResult.json");

		String recommendationResultJSON = IOUtils.toString(is, "UTF-8");

		is.close();

		return recommendationResultJSON;
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

//	FIXME CODE NO LONGER IN USE, FunctionalityTracesIterator is used now instead
//	public Map<String, Functionality> getFunctionalitiesWithCostlyAccesses(
//			String inputFilePath,
//			Set<String> profileFunctionalities,
//			Map<Short, Short> entityIDToClusterID
//	)
//			throws IOException
//	{
//		System.out.println("Getting functionalities with costly accesses...");
//
//		Map<String, Functionality> functionalities = new HashMap<>();
//
//		File jsonFile = new File(inputFilePath);
//
//		JsonFactory jsonfactory = objectMapper.getFactory();
//
//		JsonParser jsonParser = jsonfactory.createParser(jsonFile);
//		JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT
//
//		if (jsonToken != JsonToken.START_OBJECT) {
//			Utils.print("Json must start with a left curly brace", Utils.lineno());
//			System.exit(-1);
//		}
//
//		while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
//			if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
////				Utils.print("Functionality name: " + jsonParser.getCurrentName(), Utils.lineno());
//
//				if (!profileFunctionalities.contains(jsonParser.getCurrentName())) { // FIXME TEST ME
//					jsonParser.skipChildren();
//					continue;
//				}
//
//				Functionality functionality = new Functionality(jsonParser.getCurrentName());
//
//				while (jsonParser.nextValue() != JsonToken.END_OBJECT) {
////					Utils.print("field name: " + jsonParser.getCurrentName(), Utils.lineno());
//
//					switch (jsonParser.getCurrentName()) {
//						case "f":
//							break;
//						case "t": // array of traces
//
//							while (jsonParser.nextValue() != JsonToken.END_ARRAY) { // iterate over trace objects
//								while (jsonParser.nextValue() != JsonToken.END_OBJECT) { // iterate over trace object fields
//
//									switch (jsonParser.getCurrentName()) {
//										case "id":
//										case "f":
//											break;
//
//										case "a":
//											Map<Short, Byte> entityIDToMode = new HashMap<>();
//											short previousCluster = -2;
//											int i = 0;
//
//											while (jsonParser.nextValue() != JsonToken.END_ARRAY) {
//												ReducedTraceElementDto rte = jsonParser.readValueAs(
//													ReducedTraceElementDto.class
//												);
//
//												if (rte instanceof AccessDto) {
//													AccessDto access = (AccessDto) rte;
//													short entityID = access.getEntityID();
//													byte mode = access.getMode();
//													Short cluster;
//
//													cluster = entityIDToClusterID.get(entityID);
//
//													if (cluster == null) {
//														System.err.println("Entity " + entityID + " is not assign to a cluster.");
//														System.exit(-1);
//													}
//
//													if (i == 0) {
//														entityIDToMode.put(entityID, mode);
//														functionality.addEntity(entityID, mode);
//
//													} else {
//
//														if (cluster == previousCluster) {
//															boolean hasCost = false;
//															Byte savedMode = entityIDToMode.get(entityID);
//
//															if (savedMode == null) {
//																hasCost = true;
//
//															} else {
//																if (savedMode == 1 && mode == 2) // "R" -> 1, "W" -> 2
//																	hasCost = true;
//															}
//
//															if (hasCost) {
//																entityIDToMode.put(entityID, mode);
//																functionality.addEntity(entityID, mode);
//															}
//
//														} else {
//															functionality.addEntity(entityID, mode);
//
//															entityIDToMode.clear();
//															entityIDToMode.put(entityID, mode);
//
//														}
//													}
//
//													previousCluster = cluster;
//													i++;
//												}
//											}
//
//											break;
//
//										default:
//											Utils.print(
//												"Unexpected field name when parsing Trace: " + jsonParser.getCurrentName(),
//												Utils.lineno()
//											);
//
//											System.exit(-1);
//									}
//								}
//							}
//
//
//							break;
//
//						default:
//							Utils.print(
//								"Unexpected field name when parsing Functionality: " + jsonParser.getCurrentName(),
//								Utils.lineno()
//							);
//							System.exit(-1);
//					}
//				}
//
//				// only consider functionalities that touch domain entities
//				if (!functionality.getEntities().isEmpty()) {
//					functionalities.put(
//						functionality.getName(),
//							functionality
//					);
//				}
//			}
//		}
//
//		return functionalities;
//	}
}