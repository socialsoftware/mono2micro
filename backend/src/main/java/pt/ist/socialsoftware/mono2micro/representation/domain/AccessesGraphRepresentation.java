package pt.ist.socialsoftware.mono2micro.representation.domain;

import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

@Document("representation")
public class AccessesGraphRepresentation extends AccessesRepresentation {

    public static final String ACCESSES_GRAPH = "Accesses Graph";
    @Field("accesses_graph_profiles")
    private Map<String, Set<String>> profiles = new HashMap<>(); // e.g <Generic, FunctionalityNamesList>

    public AccessesGraphRepresentation() {}

    @Override
    public String init(Codebase codebase, byte[] representationFile) throws Exception {
        this.name = codebase.getName() + " & " + getType();
        this.codebase = codebase;
        addProfile("Generic", getFunctionalitiesNamesFromRepresentationFile(representationFile));
        return name;
    }

    @Override
    public String getType() {
        return ACCESSES_GRAPH;
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

    private Set<String> getFunctionalitiesNamesFromRepresentationFile(byte[] representationFile) throws Exception {
        JSONObject representationFileJSON = new JSONObject(new String(representationFile));
        Set<String> functionalitiesNames = new HashSet<>();
        for (Iterator<String> it = representationFileJSON.keys(); it.hasNext(); )
            functionalitiesNames.add(it.next());
        return functionalitiesNames;
    }
}