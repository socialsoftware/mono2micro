package pt.ist.socialsoftware.mono2micro.log.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.log.domain.Log;
import pt.ist.socialsoftware.mono2micro.log.domain.PositionLog;
import pt.ist.socialsoftware.mono2micro.log.repository.LogRepository;

import static pt.ist.socialsoftware.mono2micro.log.domain.PositionLog.POSITION_LOG;

@Service
public class LogService {
    @Autowired
    LogRepository logRepository;

    @Autowired
    GridFsService gridFsService;

    public void deleteDecompositionLog(Log log) {
        if (log == null) {
            return;
        }
        if (log.getType().equals(POSITION_LOG)) {
            PositionLog positionLog = (PositionLog) log;
            positionLog.getDepthGraphPositions().values().forEach(graphPositions -> gridFsService.deleteFile(graphPositions));
            logRepository.deleteByName(log.getName());
        }
    }
}
