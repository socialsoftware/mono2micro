package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import java.util.Map;

public interface SciPyDecomposition {
    String SCIPY_DECOMPOSITION = "SCIPY_DECOMPOSITION";
    Map<Short, String> getEntityIDToClusterName();
    void setSilhouetteScore(double silhouetteScore);
}
