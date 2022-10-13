package pt.ist.socialsoftware.mono2micro.history.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Document("history")
public class PositionHistory extends History {
    public static final String POSITION_HISTORY = "POSITION_HISTORY";

    private Map<Long, String> depthGraphPositions;

    public PositionHistory() {}

    public PositionHistory(Decomposition decomposition) {
        this.name = decomposition.getName();
        this.historyOperationList = new ArrayList<>();
        this.currentHistoryOperationDepth = 0L;
        this.depthGraphPositions = new HashMap<>();
    }

    public String getType() {
        return POSITION_HISTORY;
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

    public void removeOverriddenOperations(Long newHistoryOperationDepth) {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        setHistoryOperationsList(getHistoryOperationList().stream().filter(op ->
                op.getHistoryDepth() < newHistoryOperationDepth).collect(Collectors.toList()));

        getDepthGraphPositions().entrySet().removeIf(entry -> {
            if (entry.getKey() >= newHistoryOperationDepth) {
                gridFsService.deleteFile(entry.getValue());
                return true;
            }
            else return false;
        });
    }

    public void deleteProperties() {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        getDepthGraphPositions().values().forEach(gridFsService::deleteFile);
    }
}
