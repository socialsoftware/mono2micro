package pt.ist.socialsoftware.mono2micro.fileManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.accessesSciPyDtos.CutInfoDto;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.*;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

public class FileManager {

	private static FileManager instance = null;

    private final ObjectMapper objectMapper;

	private FileManager() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	public static FileManager getInstance() {
        if (instance == null)
        	instance = new FileManager();
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

	public void writeStrategyDecomposition(String codebaseName, String strategyFolder, String strategyName, Decomposition decomposition) throws IOException {
		objectMapper.writeValue(
				new File(CODEBASES_PATH + codebaseName + strategyFolder + strategyName + "/decompositions/" + decomposition.getName() + "/decomposition.json"),
				decomposition
		);
	}

	public InputStream getFunctionalityRedesignAsJSON(FunctionalityRedesign functionalityRedesign) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		objectMapper.writeValue(outputStream, functionalityRedesign);
		return new ByteArrayInputStream(outputStream.toByteArray());
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

	public String writeSourceFile(String codebaseName, String sourceId, Object sourceFile) throws IOException {
		InputStream sourceFileInputStream = ((MultipartFile) sourceFile).getInputStream();
		HashMap sourceFileJSON = objectMapper.readValue(sourceFileInputStream, HashMap.class);
		sourceFileInputStream.close();

		File folder = new File(CODEBASES_PATH); if (!folder.exists()) folder.mkdir();
		folder = new File(CODEBASES_PATH + codebaseName); if (!folder.exists()) folder.mkdir();
		folder = new File(CODEBASES_PATH + codebaseName + "/sources/"); if (!folder.exists()) folder.mkdir();

		File sourceFileDestination = new File(CODEBASES_PATH + codebaseName + "/sources/" + sourceId + ".json");
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
				sourceFileDestination,
				sourceFileJSON
		);
		return sourceFileDestination.getAbsolutePath();
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
}