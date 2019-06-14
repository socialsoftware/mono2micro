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

import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;

public class DendrogramManager {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String dendrogramsFolder = "src/main/resources/dendrograms/";

	public DendrogramManager() {
    }
    
    public void writeDendrogram(String name, Dendrogram dendrogram) {
		try {
			objectMapper.writeValue(new File(dendrogramsFolder + name + ".json"), dendrogram);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Dendrogram getDendrogram(String name) {
		try {
			return objectMapper.readValue(new File(dendrogramsFolder + name + ".json"), Dendrogram.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Dendrogram> getDendrograms() {
		List<Dendrogram> dendrograms = new ArrayList<>();
		File dendFolder = new File(dendrogramsFolder);
		if (!dendFolder.exists()) {
			dendFolder.mkdir();
			return dendrograms;
		}

		File[] files = dendFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				dendrograms.add(getDendrogram(filename.substring(0, filename.length()-5)));
		}
        return dendrograms;
	}

	public List<String> getDendrogramNames() {
		List<String> dendrogramNames = new ArrayList<>();
		File dendFolder = new File(dendrogramsFolder);
		if (!dendFolder.exists()) {
			dendFolder.mkdir();
			return dendrogramNames;
		}

		File[] files = dendFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				dendrogramNames.add(filename.substring(0, filename.length()-5));
		}
        return dendrogramNames;
	}

	public boolean deleteDendrogram(String name) {
		try {
			Files.deleteIfExists(Paths.get(dendrogramsFolder + name + ".json"));
			Files.deleteIfExists(Paths.get(dendrogramsFolder + name + ".png"));
			Files.deleteIfExists(Paths.get(dendrogramsFolder + name + ".txt"));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	} 
}
