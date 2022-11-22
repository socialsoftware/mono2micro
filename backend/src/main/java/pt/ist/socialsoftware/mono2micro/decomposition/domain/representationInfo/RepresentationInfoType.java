package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

public class RepresentationInfoType {
    public static final Map<String, List<String>> representationInfoTypeToFiles = new HashMap<String, List<String>>() {
        {
            put(ACCESSES_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY);}});
            put(REPOSITORY_INFO, new ArrayList<String>() {{add(ID_TO_ENTITY); add(AUTHOR); add(COMMIT);}});
            //ADD MORE TYPES HERE
        }
    };
}
