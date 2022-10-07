package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import pt.ist.socialsoftware.mono2micro.log.domain.Log;

public interface LogDecomposition {
    String LOG_DECOMPOSITION = "LOG_DECOMPOSITION";
    String getName();
    Log getLog();
    void setLog(Log log);
}
