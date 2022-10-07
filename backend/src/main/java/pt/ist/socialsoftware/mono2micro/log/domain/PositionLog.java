package pt.ist.socialsoftware.mono2micro.log.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Document("log")
public class PositionLog extends Log {
    public static final String POSITION_LOG = "POSITION_LOG";

    private Map<Long, String> depthGraphPositions;

    public PositionLog() {}

    public PositionLog(LogDecomposition decomposition) {
        this.name = decomposition.getName();
        this.logOperationList = new ArrayList<>();
        this.currentLogOperationDepth = 0L;
        this.depthGraphPositions = new HashMap<>();
    }

    public String getType() {
        return POSITION_LOG;
    }

    public Map<Long, String> getDepthGraphPositions() {
        return this.depthGraphPositions;
    }

    public String getDepthGraphPosition(Long depth) {
        return this.depthGraphPositions.get(depth);
    }

    public void setDepthGraphPositions(Map<Long, String> depthGraphPositions) {
        this.depthGraphPositions = depthGraphPositions;
    }

    public void putDepthGraphPosition(Long depth, String fileName) {
        this.depthGraphPositions.put(depth, fileName);
    }

    public void removeDepthGraphPosition(Long depth) {
        this.depthGraphPositions.remove(depth);
    }
}
