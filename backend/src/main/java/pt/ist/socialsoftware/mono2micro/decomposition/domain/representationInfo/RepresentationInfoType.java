package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final Map<String, List<String>> representationInfoTypeToFiles = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(ACCESSES_INFO, new ArrayList<>(Arrays.asList(ACCESSES, ID_TO_ENTITY))),
            new AbstractMap.SimpleImmutableEntry<>(REPOSITORY_INFO, new ArrayList<>(Arrays.asList(ACCESSES, ID_TO_ENTITY, AUTHOR, COMMIT))),
            new AbstractMap.SimpleImmutableEntry<>(CLASS_VECTORIZATION_INFO, new ArrayList<>(Arrays.asList(ACCESSES, ID_TO_ENTITY, ENTITY_TO_ID, CODE_EMBEDDINGS))),
            new AbstractMap.SimpleImmutableEntry<>(ENTITY_VECTORIZATION_INFO, new ArrayList<>(Arrays.asList(ACCESSES, ID_TO_ENTITY, ENTITY_TO_ID, CODE_EMBEDDINGS))),
            new AbstractMap.SimpleImmutableEntry<>(FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO, new ArrayList<>(Arrays.asList(ACCESSES, ID_TO_ENTITY, ENTITY_TO_ID, CODE_EMBEDDINGS))),
            new AbstractMap.SimpleImmutableEntry<>(FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO, new ArrayList<>(Arrays.asList(ACCESSES, ID_TO_ENTITY, ENTITY_TO_ID, CODE_EMBEDDINGS)))
    ).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
}
