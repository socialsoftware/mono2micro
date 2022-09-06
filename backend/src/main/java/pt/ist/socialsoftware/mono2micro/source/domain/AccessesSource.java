package pt.ist.socialsoftware.mono2micro.source.domain;

import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

@Document("source")
public class AccessesSource extends Source {

    public static final String ACCESSES = "Accesses";
    private Map<String, Set<String>> profiles = new HashMap<>(); // e.g <Generic, FunctionalityNamesList>

    public AccessesSource() {}

    @Override
    public String init(Codebase codebase, byte[] sourceFile) throws Exception {
        this.name = codebase.getName() + " & " + getType();
        this.codebase = codebase;
        addProfile("Generic", getFunctionalitiesNamesFromSourceFile(sourceFile));
        return name;
    }

    @Override
    public String getType() {
        return ACCESSES;
    }


    public Map<String, Set<String>> getProfiles() {
        return this.profiles;
    }

    public Set<String> getProfile(String profileName) { return this.profiles.get(profileName); }

    public void setProfiles(Map<String, Set<String>> profiles) {
        this.profiles = profiles;
    }

    public void addProfile(String profileName, Set<String> functionalities) {
        if (this.profiles.containsKey(profileName))
            throw new KeyAlreadyExistsException();

        this.profiles.put(profileName, functionalities);
    }

    public void deleteProfile(String profileName) {
        this.profiles.remove(profileName);
    }

    public void moveFunctionalities(String[] functionalities, String targetProfile) {
        for (String profile : this.profiles.keySet())
            for (String functionality : functionalities)
                this.profiles.get(profile).remove(functionality);

        for (String functionality : functionalities)
            this.profiles.get(targetProfile).add(functionality);
    }

    private Set<String> getFunctionalitiesNamesFromSourceFile(byte[] sourceFile) throws Exception {
        JSONObject sourceFileJSON = new JSONObject(new String(sourceFile));
        Set<String> functionalitiesNames = new HashSet<>();
        for (Iterator<String> it = sourceFileJSON.keys(); it.hasNext(); )
            functionalitiesNames.add(it.next());
        return functionalitiesNames;
    }
}