package pt.ist.socialsoftware.mono2micro.source.dto;

import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;

import java.util.Map;
import java.util.Set;

public class AccessesSourceDto extends SourceDto {
    private Map<String, Set<String>> profiles;

    public AccessesSourceDto(AccessesSource source) {
        super(source);
        this.profiles = source.getProfiles();
    }

    public Map<String, Set<String>> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, Set<String>> profiles) {
        this.profiles = profiles;
    }
}
