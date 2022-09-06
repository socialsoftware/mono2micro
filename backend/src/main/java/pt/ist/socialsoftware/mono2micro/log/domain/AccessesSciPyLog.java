package pt.ist.socialsoftware.mono2micro.log.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Document("log")
public class AccessesSciPyLog extends Log {

    private Map<Long, String> depthGraphPositions;

    public AccessesSciPyLog() {}

    public AccessesSciPyLog(Decomposition decomposition) {
        this.name = decomposition.getName();
        this.decomposition = decomposition;
        this.logOperationList = new ArrayList<>();
        this.currentLogOperationDepth = 0L;
        this.depthGraphPositions = new HashMap<>();
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
