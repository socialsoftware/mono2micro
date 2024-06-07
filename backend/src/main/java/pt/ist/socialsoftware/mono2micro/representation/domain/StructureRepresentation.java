package pt.ist.socialsoftware.mono2micro.representation.domain;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;

@Document("representation")
public class StructureRepresentation extends Representation {
    public static final String STRUCTURE = "Structure";
    private Map<String, Set<String>> profiles = new HashMap<>();

    public StructureRepresentation() {}

    @Override
    public String init(Codebase codebase, byte[] representationFile) throws Exception {
        this.name = codebase.getName() + " & " + getType();
        this.codebase = codebase;
        addProfile("Generic", getEntitiesNamesFromRepresentationFile(representationFile));
        return name;
    }

    @Override
    public String getType() {
        return STRUCTURE;
    }

    public Map<String, Set<String>> getProfiles() {
        return this.profiles;
    }

    public Set<String> getProfile(String profileName) { return this.profiles.get(profileName); }

    public void setProfiles(Map<String, Set<String>> profiles) {
        this.profiles = profiles;
    }

    public void addProfile(String profileName, Set<String> entities) {
        if (this.profiles.containsKey(profileName))
            throw new KeyAlreadyExistsException();

        this.profiles.put(profileName, entities);
    }

    public void deleteProfile(String profileName) {
        this.profiles.remove(profileName);
    }

    private Set<String> getEntitiesNamesFromRepresentationFile(byte[] representationFile) throws Exception {
        JSONObject representationFileJSON = new JSONObject(new String(representationFile));
        Set<String> entitiesNames = new HashSet<>();

        JSONArray entitiesArray = representationFileJSON.getJSONArray("entities");
        for (int i = 0; i < entitiesArray.length(); i++) {
            JSONObject entityObject = entitiesArray.getJSONObject(i);
            String entityName = entityObject.getString("name");
            entitiesNames.add(entityName);
        }

        return entitiesNames;
    }
}
