import Similarity from "./Similarity";
import SimilaritySciPyWeights, {SIMILARITY_SCIPY_WEIGHTS} from "./SimilaritySciPyWeights";
import SimilarityMatrixEntityVectorization, {SIMILARITY_SCIPY_ENTITY_VECTORIZATION} from "./SimilarityMatrixEntityVectorization";

export abstract class SimilarityFactory {
    static getSimilarity(similarity: any) : Similarity {
        switch (similarity.type) {
            case SIMILARITY_SCIPY_WEIGHTS:
                return new SimilaritySciPyWeights(similarity);
            case SIMILARITY_SCIPY_ENTITY_VECTORIZATION:
                return new SimilarityMatrixEntityVectorization(similarity);
            default:
                throw new Error('Type ' + similarity.type + ' unknown.');
        }
    }
}
