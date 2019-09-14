package pt.ist.socialsoftware.mono2micro.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_FOLDER;

public class CodebaseManager {

	private static CodebaseManager instance = null; 

    private ObjectMapper objectMapper;

	private CodebaseManager() {
		objectMapper = new ObjectMapper();

		File codebasesFolder = new File(CODEBASES_FOLDER);
		if (!codebasesFolder.exists()) {
			codebasesFolder.mkdir();
		}
	}
	
	public static CodebaseManager getInstance() { 
        if (instance == null) 
        	instance = new CodebaseManager(); 
        return instance; 
    } 
    
    public void writeCodebase(String name, Codebase codebase) throws IOException {
		objectMapper.writeValue(new File(CODEBASES_FOLDER + name + ".json"), codebase);
	}

	public Codebase getCodebase(String name) {
		try {
			return objectMapper.readValue(new File(CODEBASES_FOLDER + name + ".json"), Codebase.class);
		} catch (IOException e) {
			return null;
		}
	}

	public List<Codebase> getCodebases() {
		List<Codebase> codebases = new ArrayList<>();
		File codebasesFolder = new File(CODEBASES_FOLDER);
		if (!codebasesFolder.exists()) {
			codebasesFolder.mkdir();
		}

		File[] files = codebasesFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				codebases.add(getCodebase(filename.substring(0, filename.length()-5)));
		}
        return codebases;
	}

	public List<String> getCodebaseNames() {
		List<String> codebaseNames = new ArrayList<>();
		File codebasesFolder = new File(CODEBASES_FOLDER);
		if (!codebasesFolder.exists()) {
			codebasesFolder.mkdir();
		}

		File[] files = codebasesFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				codebaseNames.add(filename.substring(0, filename.length()-5));
		}
        return codebaseNames;
	}

	public void deleteCodebase(String name) throws IOException {
		Files.deleteIfExists(Paths.get(CODEBASES_FOLDER + name + ".json"));
		Files.deleteIfExists(Paths.get(CODEBASES_FOLDER + name + ".txt"));
		Files.deleteIfExists(Paths.get(CODEBASES_FOLDER + name));
	}

	public Codebase createCodebase(String codebaseName, MultipartFile datafile) throws IOException, JSONException {
		
		if (getCodebase(codebaseName) != null)
			throw new KeyAlreadyExistsException();

		File codebasesFolder = new File(CODEBASES_FOLDER);
		if (!codebasesFolder.exists()) {
			codebasesFolder.mkdir();
		}

		File codebaseFolder = new File(CODEBASES_FOLDER + codebaseName);
		if (!codebaseFolder.exists()) {
			codebaseFolder.mkdir();
		}

		Codebase codebase = new Codebase(codebaseName);

		//store datafile, needs to be read again when dendrogram is created
		FileOutputStream outputStream = new FileOutputStream(CODEBASES_FOLDER + codebaseName + ".txt");
		outputStream.write(datafile.getBytes());
		outputStream.close();

		// read datafile
		InputStream is = new BufferedInputStream(datafile.getInputStream());
		JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
		is.close();

		Iterator<String> controllerNames = datafileJSON.sortedKeys();
		List<String> controllers = new ArrayList<>();
		while (controllerNames.hasNext()) {
			controllers.add(controllerNames.next());
		}
		codebase.addProfile("Generic", controllers);

		return codebase;
	}
}
