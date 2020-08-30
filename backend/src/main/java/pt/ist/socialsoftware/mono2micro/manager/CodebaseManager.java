package pt.ist.socialsoftware.mono2micro.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.dto.ControllerDto;
import pt.ist.socialsoftware.mono2micro.dto.CutInfoDto;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

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

	public List<Codebase> getCodebases() throws IOException {
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
		ObjectMapper mapper = new ObjectMapper();
		InputStream datafileInputStream = null;

		if (datafile instanceof MultipartFile) {
			// read datafile
			datafileInputStream = ((MultipartFile) datafile).getInputStream();
			datafileJSON = mapper.readValue(datafileInputStream, HashMap.class);
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
		return objectMapper.readValue(new File(CODEBASES_PATH + codebaseName + "/codebase.json"), Codebase.class);
	}

	public void writeCodebase(Codebase codebase) throws IOException {
		objectMapper.writeValue(new File(CODEBASES_PATH + codebase.getName() + "/codebase.json"), codebase);
	}

	public HashMap<String, ControllerDto> getDatafile(String codebaseName) throws IOException {
		Codebase codebase = getCodebase(codebaseName);
		InputStream is = new FileInputStream(codebase.getDatafilePath());

		return objectMapper.readValue(is, new TypeReference<HashMap<String, ControllerDto>>(){});
	}

	public HashMap<String, ControllerDto> getDatafile(Codebase codebase) throws IOException {
		InputStream is = new FileInputStream(codebase.getDatafilePath());

		return objectMapper.readValue(is, new TypeReference<HashMap<String, ControllerDto>>(){});
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
		ObjectMapper mapper = new ObjectMapper();
		DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
		pp.indentArraysWith( DefaultIndenter.SYSTEM_LINEFEED_INSTANCE );
		ObjectWriter writer = mapper.writer(pp);
		writer.writeValue(new File(CODEBASES_PATH + codebaseName + "/analyser/analyserResult.json"), analyserJSON);
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
}
