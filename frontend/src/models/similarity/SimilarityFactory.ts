import Similarity from "./Similarity";
import SimilarityMatrixSciPy, {SIMILARITY_MATRIX_SCIPY} from "./SimilarityMatrixSciPy";

export abstract class SimilarityFactory {
    static getSimilarity(similarity: any) : Similarity {
        switch (similarity.type) {
            case SIMILARITY_MATRIX_SCIPY:
                return new SimilarityMatrixSciPy(similarity);
            default:
                throw new Error('Type ' + similarity.type + ' unknown.');
        }
    }
}
