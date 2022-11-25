package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.ClassVectorizationInfo.CLASS_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.EntityVectorizationInfo.ENTITY_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationByCallGraphInfo.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationBySequenceOfAccessesInfo.FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

public class RepresentationInfoType {
    public static final Map<String, List<String>> representationInfoTypeToFiles = new HashMap<String, List<String>>() {
        {
            put(ACCESSES_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY);}});
            put(REPOSITORY_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY); add(AUTHOR); add(COMMIT);}});
            put(CLASS_VECTORIZATION_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY); add(ENTITY_TO_ID); add(CODE_EMBEDDINGS);}});
            put(ENTITY_VECTORIZATION_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY); add(ENTITY_TO_ID); add(CODE_EMBEDDINGS);}});
            put(FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY); add(ENTITY_TO_ID); add(CODE_EMBEDDINGS);}});
            put(FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO, new ArrayList<String>() {{add(ACCESSES); add(ID_TO_ENTITY); add(ENTITY_TO_ID); add(CODE_EMBEDDINGS);}});
            //ADD MORE TYPES HERE
        }
    };
}
