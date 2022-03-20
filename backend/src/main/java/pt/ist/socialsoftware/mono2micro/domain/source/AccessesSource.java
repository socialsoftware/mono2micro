package pt.ist.socialsoftware.mono2micro.domain.source;

import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.ACCESSES;

public class AccessesSource extends Source {
    private Map<String, Set<String>> profiles = new HashMap<>(); // e.g <Generic, ControllerNamesList> change to Set

    @Override
    public void init(String codebaseName, Object inputFile) throws Exception {
        CodebaseManager codebaseManager = CodebaseManager.getInstance();
        this.codebaseName = codebaseName;
        this.inputFilePath = codebaseManager.writeInputFile(codebaseName, getType(), inputFile);
        addProfile("Generic", Utils.getJsonFileKeys(new File(inputFilePath)));
    }

    @Override
    public String getType() {
        return ACCESSES;
    }


    // GETTERS AND SETTERS
    public Map<String, Set<String>> getProfiles() {
        return this.profiles;
    }

    public Set<String> getProfile(String profileName) { return this.profiles.get(profileName); }

    public void setProfiles(Map<String, Set<String>> profiles) {
        this.profiles = profiles;
    }

    public void addProfile(String profileName, Set<String> controllers) {
        if (this.profiles.containsKey(profileName))
            throw new KeyAlreadyExistsException();

        this.profiles.put(profileName, controllers);
    }

    public void deleteProfile(String profileName) {
        this.profiles.remove(profileName);
    }

    public void moveControllers(String[] controllers, String targetProfile) {
        for (String profile : this.profiles.keySet())
            for (String controller : controllers)
                this.profiles.get(profile).remove(controller);

        for (String controller : controllers)
            this.profiles.get(targetProfile).add(controller);
    }
}