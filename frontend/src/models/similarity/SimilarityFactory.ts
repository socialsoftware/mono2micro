import Similarity from "./Similarity";
import SimilarityScipyAccessesAndRepository, {SIMILARITY_SCIPY_ACCESSES_REPOSITORY} from "./SimilarityScipyAccessesAndRepository";
import SimilarityScipyEntityVectorization, {SIMILARITY_SCIPY_ENTITY_VECTORIZATION} from "./SimilarityScipyEntityVectorization";
import SimilarityScipyClassVectorization, {SIMILARITY_SCIPY_CLASS_VECTORIZATION} from "./SimilarityScipyClassVectorization";
import SimilarityScipyFunctionalityVectorizationByCallGraph, {SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH} from "./SimilarityScipyFunctionalityVectorizationByCallGraph";
import SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses, {SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES} from "./SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses";

export abstract class SimilarityFactory {
    static getSimilarity(similarity: any) : Similarity {
        switch (similarity.type) {
            case SIMILARITY_SCIPY_ACCESSES_REPOSITORY:
                return new SimilarityScipyAccessesAndRepository(similarity);
            case SIMILARITY_SCIPY_ENTITY_VECTORIZATION:
                return new SimilarityScipyEntityVectorization(similarity);
            case SIMILARITY_SCIPY_CLASS_VECTORIZATION:
                return new SimilarityScipyClassVectorization(similarity);
            case SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
                return new SimilarityScipyFunctionalityVectorizationByCallGraph(similarity);
            case SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(similarity);
            default:
                throw new Error('Type ' + similarity.type + ' unknown.');
        }
    }
}
