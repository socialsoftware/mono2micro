package pt.ist.socialsoftware.mono2micro.analysis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.socialsoftware.mono2micro.analysis.service.AccessesSciPyAnalysisService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPy;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.analysis.dto.AnalysisDto;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@RestController
@RequestMapping(value = "/mono2micro")
public class AnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    AccessesSciPyAnalysisService accessesSciPyAnalysisService;

    @Autowired
    DecompositionRepository decompositionRepository;

    @RequestMapping(value = "/analysis/{decomposition1Name}/{decomposition2Name}", method = RequestMethod.POST)
    public ResponseEntity<AnalysisDto> getAnalysis(@PathVariable String decomposition1Name, @PathVariable String decomposition2Name) {
        logger.debug("getAnalysis");

        try {
            Decomposition decomposition1 = decompositionRepository.findByName(decomposition1Name);
            Decomposition decomposition2 = decompositionRepository.findByName(decomposition2Name);

            switch (decomposition1.getStrategyType() + decomposition2.getStrategyType()) {
                case ACCESSES_SCIPY + ACCESSES_SCIPY:
                    return new ResponseEntity<>(
                            accessesSciPyAnalysisService.getAnalysis((AccessesSciPy) decomposition1, (AccessesSciPy) decomposition2),
                            HttpStatus.OK
                    );
                default:
                    throw new RuntimeException("No decomposition analysis for types: " + decomposition1.getStrategyType() + " and " + decomposition2.getStrategyType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
