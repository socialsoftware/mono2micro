package pt.ist.socialsoftware.mono2micro.domain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProfileManager {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String profilesFolder = "src/main/resources/profiles/";

	public ProfileManager() {
    }
    
    public void writeProfileGroup(String name, ProfileGroup profileGroup) {
		try {
			objectMapper.writeValue(new File(profilesFolder + name + ".json"), profileGroup);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ProfileGroup getProfileGroup(String name) {
		try {
			return objectMapper.readValue(new File(profilesFolder + name + ".json"), ProfileGroup.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ProfileGroup> getProfileGroups() {
		List<ProfileGroup> profileGroups = new ArrayList<>();
		File profFolder = new File(profilesFolder);
		if (!profFolder.exists()) {
			profFolder.mkdir();
			return profileGroups;
		}

		File[] files = profFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				profileGroups.add(getProfileGroup(filename.substring(0, filename.length()-5)));
		}
        return profileGroups;
	}

	public List<String> getProfileGroupNames() {
		List<String> profileGroupNames = new ArrayList<>();
		File profFolder = new File(profilesFolder);
		if (!profFolder.exists()) {
			profFolder.mkdir();
			return profileGroupNames;
		}

		File[] files = profFolder.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified));
		
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".json"))
				profileGroupNames.add(filename.substring(0, filename.length()-5));
		}
        return profileGroupNames;
	}

	public boolean deleteProfileGroup(String name) {
		try {
			Files.deleteIfExists(Paths.get(profilesFolder + name + ".json"));
			Files.deleteIfExists(Paths.get(profilesFolder + name + ".txt"));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	} 
}
