package pt.ist.socialsoftware.mono2micro.history.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.history.repository.HistoryRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PositionHistoryService {
    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    GridFsService gridFsService;

    public void saveGraphPositions(Decomposition decomposition, String graphPositions) {
        PositionHistory history = (PositionHistory) decomposition.getHistory();
        Long depth = history.getCurrentHistoryOperationDepth();
        String fileName = history.getName() + "_depth_" + depth;

        gridFsService.replaceFile(new ByteArrayInputStream(graphPositions.getBytes(StandardCharsets.UTF_8)), fileName);
        history.putDepthGraphPosition(depth, fileName);
        historyRepository.save(history);
    }

    public String getGraphPositions(Decomposition decomposition) throws IOException {
        PositionHistory history = (PositionHistory) decomposition.getHistory();
        String fileName = history.getDepthGraphPosition(history.getCurrentHistoryOperationDepth());
        if (fileName == null)
            return null;
        InputStream file = gridFsService.getFile(fileName);
        return IOUtils.toString(file, StandardCharsets.UTF_8);
    }
}
