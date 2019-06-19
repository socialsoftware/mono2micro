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
import pt.ist.socialsoftware.mono2micro.domain.Expert;

public class ExpertManager {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String expertFolder = "src/main/resources/experts/";

	public ExpertManager() {
    }
    
    public void writeExpert(String name, Expert expert) {
		try {
			objectMapper.writeValue(new File(expertFolder + name + ".json"), expert);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Expert getExpert(String name) {
		try {
			return objectMapper.readValue(new File(expertFolder + name + ".json"), Expert.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Expert> getExperts() {
		List<Expert> experts = new ArrayList<>();
		File expFolder = new File(expertFolder);
		if (!expFolder.exists()) {
			expFolder.mkdir();
			return experts;
		}

		File[] files = expFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				experts.add(getExpert(filename.substring(0, filename.length()-5)));
		}
        return experts;
	}

	public List<String> getExpertNames() {
		List<String> expertNames = new ArrayList<>();
		File expFolder = new File(expertFolder);
		if (!expFolder.exists()) {
			expFolder.mkdir();
			return expertNames;
		}

		File[] files = expFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				expertNames.add(filename.substring(0, filename.length()-5));
		}
        return expertNames;
	}

	public boolean deleteExpert(String name) {
		try {
			Files.deleteIfExists(Paths.get(expertFolder + name + ".json"));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	} 
}
