package pt.ist.socialsoftware.mono2micro.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.*;
import pt.ist.socialsoftware.mono2micro.dto.ControllerDto;
import pt.ist.socialsoftware.mono2micro.dto.CutInfoDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.*;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

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

	public List<Codebase> getCodebasesWithFields(Set<String> deserializableFields) throws IOException {
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
	) throws IOException {
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
	) throws IOException {
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
	) throws Exception {

		return getCodebaseDendrogramsWithFields(
			codebaseName,
			dendrogramDeserializableFields
		)
			.stream()
			.filter(dendrogram -> dendrogram.getName().equals(dendrogramName))
			.findFirst()
			.orElseThrow(() -> new Exception("Dendrogram " + dendrogramName + " not found"));
	}

	public List<Graph> getCodebaseGraphsWithFields(
		String codebaseName,
		Set<String> graphDeserializableFields
	) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", new HashSet<String>() {{ add("graphs"); }})
				.addValue("graphDeserializableFields", graphDeserializableFields)
		);

		File codebaseJSONFile = new File(CODEBASES_PATH + codebaseName + "/codebase.json");

		if (!codebaseJSONFile.exists())
			return null;

		ObjectReader reader = objectMapper.readerFor(Codebase.class);
		Codebase cb = reader.readValue(codebaseJSONFile);

		return cb.getDendrograms()
			.stream()
			.flatMap(dendrogram -> dendrogram.getGraphs().stream())
			.collect(Collectors.toList());
	}

	public List<Graph> getDendrogramGraphsWithFields(
		String codebaseName,
		String dendrogramName,
		Set<String> graphDeserializableFields
	) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		graphDeserializableFields.add("name");

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", new HashSet<String>() {{ add("name"); add("graphs"); }})
				.addValue("graphDeserializableFields", graphDeserializableFields)
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

		return d.getGraphs();
	}

	public Graph getDendrogramGraphWithFields(
		String codebaseName,
		String dendrogramName,
		String graphName,
		Set<String> graphDeserializableFields
	) throws Exception {

		return getDendrogramGraphsWithFields(
			codebaseName,
			dendrogramName,
			graphDeserializableFields
		)
			.stream()
			.filter(graph -> graph.getName().equals(graphName))
			.findFirst()
			.orElseThrow(() -> new Exception("Graph " + graphName + " not found"));
	}

	public Graph getGraphWithControllersAndClustersWithFields(
		String codebaseName,
		String dendrogramName,
		String graphName,
		Set<String> controllerDeserializableFields,
		Set<String> clusterDeserializableFields
	) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setInjectableValues(
			new InjectableValues.Std()
				.addValue("codebaseDeserializableFields", new HashSet<String>() {{ add("dendrograms"); }})
				.addValue("dendrogramDeserializableFields", new HashSet<String>() {{ add("name"); add("graphs"); }})
				.addValue("graphDeserializableFields", new HashSet<String>() {{ add("name"); add("controllers"); add("clusters"); }})
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

		return d.getGraphs().stream()
				.filter(graph -> graph.getName().equals(graphName))
				.findFirst()
				.orElseThrow(() -> new Exception("Graph " + graphName + " not found"));
	}

	public void deleteCodebase(String codebaseName) throws IOException {
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName));
	}

	public Codebase createCodebase(
		String codebaseName,
		Object datafile,
		String analysisType
	) throws IOException {
		
		if (getCodebase(codebaseName) != null)
			throw new KeyAlreadyExistsException();

		File codebasesPath = new File(CODEBASES_PATH);
		if (!codebasesPath.exists()) {
			codebasesPath.mkdir();
		}

		File codebasePath = new File(CODEBASES_PATH + codebaseName);
		if (!codebasePath.exists()) {
			codebasePath.mkdir();
		}

		Codebase codebase = new Codebase(codebaseName, analysisType);

		HashMap datafileJSON;

		InputStream datafileInputStream = null;

		if (datafile instanceof MultipartFile) {
			// read datafile
			datafileInputStream = ((MultipartFile) datafile).getInputStream();
			datafileJSON = objectMapper.readValue(datafileInputStream, HashMap.class);
			datafileInputStream.close();
			this.writeDatafile(codebaseName, datafileJSON);
			File datafileFile = new File(CODEBASES_PATH + codebaseName + "/datafile.json");
			codebase.setDatafilePath(datafileFile.getAbsolutePath());
			datafileInputStream = new FileInputStream(datafileFile);
		}
		else if (datafile instanceof String) {
			File localDatafile = new File((String) datafile);
			if (!localDatafile.exists())
				throw new FileNotFoundException();

			datafileInputStream = new FileInputStream(localDatafile);
			codebase.setDatafilePath((String) datafile);
		}

		codebase.addProfile("Generic", Utils.getJsonFileKeys(datafileInputStream));

		return codebase;
	}

	public Codebase getCodebase(String codebaseName) throws IOException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/codebase.json");

		return objectMapper.readerFor(Codebase.class).readValue(is);
	}

	public void writeCodebase(Codebase codebase) throws IOException {
		objectMapper.writeValue(new File(CODEBASES_PATH + codebase.getName() + "/codebase.json"), codebase);
	}

	public HashMap<String, ControllerDto> getDatafile(String codebaseName) throws IOException {
		Codebase codebase = getCodebaseWithFields(
			codebaseName,
			new HashSet<String>() {{ add("datafilePath"); }}
		);

		InputStream is = new FileInputStream(codebase.getDatafilePath());
		return objectMapper.readerFor(new TypeReference<HashMap<String, ControllerDto>>() {}).readValue(is);
	}

	public HashMap<String, ControllerDto> getDatafile(Codebase codebase) throws IOException {
		InputStream is = new FileInputStream(codebase.getDatafilePath());
		return objectMapper.readerFor(new TypeReference<HashMap<String, ControllerDto>>() {}).readValue(is);
	}

	public void writeDatafile(String codebaseName, HashMap datafile) throws IOException {
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(
			new File(CODEBASES_PATH + codebaseName + "/datafile.json"),
			datafile
		);
	}

	public JSONObject getSimilarityMatrix(String codebaseName, String dendrogramName) throws IOException, JSONException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/similarityMatrix.json");
		JSONObject similarityMatrixJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();
		return similarityMatrixJSON;
	}

	public void writeSimilarityMatrix(String codebaseName, String dendrogramName, JSONObject similarityMatrix) throws IOException, JSONException {
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/similarityMatrix.json");
		file.write(similarityMatrix.toString(4));
		file.close();
	}

	public byte[] getDendrogramImage(String codebaseName, String dendrogramName) throws IOException {
		return Files.readAllBytes(Paths.get(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/dendrogramImage.png"));
	}

	public JSONObject getClusters(String codebaseName, String dendrogramName, String graphName) throws IOException, JSONException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/" + dendrogramName + "/" + graphName + "/clusters.json");
		JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();
		return clustersJSON;
	}

	public HashMap<String, CutInfoDto> getAnalyserResults(String codebaseName) throws IOException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");
		return objectMapper.readValue(is, new TypeReference<HashMap<String, CutInfoDto>>() {});
	}

	public void writeAnalyserResults(String codebaseName, HashMap analyserJSON) throws IOException {
		DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
		ObjectWriter writer = objectMapper.writer(pp);
		writer.writeValue(new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json"), analyserJSON);
	}

	public HashMap<String, HashMap<String, Set<String>>> getAnalyserCut(String codebaseName, String cutName) throws IOException, JSONException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/cuts/" + cutName + ".json");
		return objectMapper.readValue(is, new TypeReference<HashMap<String, HashMap<String, Set<String>>>>() {});
	}

	public void writeAnalyserSimilarityMatrix(String codebaseName, JSONObject similarityMatrix) throws IOException, JSONException {
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/analyser/similarityMatrix.json");
		file.write(similarityMatrix.toString(4));
		file.close();
	}
}
