package pt.ist.socialsoftware.mono2micro.manager;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.KeyAlreadyExistsException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.Codebase;

public class CodebaseManager {

	private static CodebaseManager instance = null; 

    private ObjectMapper objectMapper;

	private CodebaseManager() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	public static CodebaseManager getInstance() { 
        if (instance == null) 
        	instance = new CodebaseManager(); 
        return instance; 
	}

	public List<Codebase> getCodebases() {
		List<Codebase> codebases = new ArrayList<>();
		File codebasesPath = new File(CODEBASES_PATH);
		if (!codebasesPath.exists()) {
			codebasesPath.mkdir();
			return codebases;
		}

		File[] files = codebasesPath.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			codebases.add(getCodebase(filename));
		}
        return codebases;
	}

	public void deleteCodebase(String codebaseName) throws IOException {
		FileUtils.deleteDirectory(new File(CODEBASES_PATH + codebaseName));
	}

	public Codebase createCodebase(String codebaseName, MultipartFile datafile) throws IOException, JSONException {
		
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

		Codebase codebase = new Codebase(codebaseName);

		// read datafile
		InputStream is = new BufferedInputStream(datafile.getInputStream());
		JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();

		this.writeDatafile(codebaseName, datafileJSON);

		Iterator<String> controllerNames = datafileJSON.sortedKeys();
		List<String> controllers = new ArrayList<>();
		while (controllerNames.hasNext()) {
			controllers.add(controllerNames.next());
		}
		codebase.addProfile("Generic", controllers);

		return codebase;
	}


	public Codebase getCodebase(String codebaseName) {
		try {
			return objectMapper.readValue(new File(CODEBASES_PATH + codebaseName + "/codebase.json"), Codebase.class);
		} catch(IOException e) {
			return null;
		}
	}

	public void writeCodebase(String codebaseName, Codebase codebase) throws IOException {
		objectMapper.writeValue(new File(CODEBASES_PATH + codebaseName + "/codebase.json"), codebase);
	}

	public JSONObject getDatafile(String codebaseName) throws IOException, JSONException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/datafile.json");
		JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();
		return datafileJSON;
	}

	public void writeDatafile(String codebaseName, JSONObject datafile) throws IOException, JSONException {
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/datafile.json");
		file.write(datafile.toString(4));
		file.close();
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

	public JSONObject getAnalyserResults(String codebaseName) throws IOException, JSONException {		
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");
		JSONObject analyserJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();
		return analyserJSON;
	}

	public void writeAnalyserResults(String codebaseName, JSONObject analyser) throws IOException, JSONException {
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json");
		file.write(analyser.toString(4));
		file.close();
	}

	public JSONObject getAnalyserCut(String codebaseName, String cutName) throws IOException, JSONException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/analyser/cuts/" + cutName + ".json");
		JSONObject analyserCutJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();
		return analyserCutJSON;
	}

	public void writeAnalyserSimilarityMatrix(String codebaseName, JSONObject similarityMatrix) throws IOException, JSONException {
		FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/analyser/similarityMatrix.json");
		file.write(similarityMatrix.toString(4));
		file.close();
	}

	public JSONObject getExpertCut(String codebaseName) throws IOException, JSONException {
		InputStream is = new FileInputStream(CODEBASES_PATH + codebaseName + "/expertCut.json");
		JSONObject expertCutJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();
		return expertCutJSON;
	}
}
