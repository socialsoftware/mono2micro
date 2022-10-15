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
import pt.ist.socialsoftware.mono2micro.analysis.dto.AnalysisDtoFactory;
import pt.ist.socialsoftware.mono2micro.analysis.domain.MoJoCalculations;
import pt.ist.socialsoftware.mono2micro.analysis.dto.interfaces.MoJoProperties;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.analysis.dto.AnalysisDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;

@RestController
@RequestMapping(value = "/mono2micro")
public class AnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    DecompositionRepository decompositionRepository;

    @RequestMapping(value = "/analysis/{decomposition1Name}/{decomposition2Name}", method = RequestMethod.POST)
    public ResponseEntity<AnalysisDto> getAnalysis(
            @PathVariable String decomposition1Name,
            @PathVariable String decomposition2Name
    ) {
        logger.debug("getAnalysis");

        try {
            Decomposition decomposition1 = decompositionRepository.findByName(decomposition1Name);
            Decomposition decomposition2 = decompositionRepository.findByName(decomposition2Name);

            AnalysisDto analysisDto = AnalysisDtoFactory.getAnalysisDto(decomposition1, decomposition2);

            MoJoCalculations.getAnalysis((MoJoProperties) analysisDto, decomposition1, decomposition2);
            analysisDto.setDecomposition1(DecompositionDtoFactory.getDecompositionDto(decomposition1));
            analysisDto.setDecomposition2(DecompositionDtoFactory.getDecompositionDto(decomposition2));
            return new ResponseEntity<>(analysisDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
