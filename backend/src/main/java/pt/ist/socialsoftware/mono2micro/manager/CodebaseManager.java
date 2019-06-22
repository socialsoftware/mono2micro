package pt.ist.socialsoftware.mono2micro.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ist.socialsoftware.mono2micro.domain.Codebase;

public class CodebaseManager {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String codebaseFolder = "src/main/resources/codebases/";

	public CodebaseManager() {
    }
    
    public void writeCodebase(String name, Codebase codebase) {
		try {
			objectMapper.writeValue(new File(codebaseFolder + name + ".json"), codebase);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Codebase getCodebase(String name) {
		try {
			return objectMapper.readValue(new File(codebaseFolder + name + ".json"), Codebase.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Codebase> getCodebases() {
		List<Codebase> codebases = new ArrayList<>();
		File codeFolder = new File(codebaseFolder);
		if (!codeFolder.exists()) {
			codeFolder.mkdir();
			return codebases;
		}

		File[] files = codeFolder.listFiles();
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
		File codeFolder = new File(codebaseFolder);
		if (!codeFolder.exists()) {
			codeFolder.mkdir();
			return codebaseNames;
		}

		File[] files = codeFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				codebaseNames.add(filename.substring(0, filename.length()-5));
		}
        return codebaseNames;
	}

	public boolean deleteCodebase(String name) {
		try {
			Files.deleteIfExists(Paths.get(codebaseFolder + name + ".json"));
			Files.deleteIfExists(Paths.get(codebaseFolder + name + ".txt"));
			Files.deleteIfExists(Paths.get(codebaseFolder + name));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean deleteDendrogram(String codebaseName, String dendrogramName) {
		try {
			Codebase codebase = getCodebase(codebaseName);
			Files.deleteIfExists(Paths.get(codebaseFolder + codebaseName + "/" + dendrogramName + ".png"));
			Files.deleteIfExists(Paths.get(codebaseFolder + codebaseName + "/" + dendrogramName + ".txt"));
			boolean deleted = codebase.deleteDendrogram(dendrogramName);
			if (deleted) {
				writeCodebase(codebaseName, codebase);
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	} 
}
