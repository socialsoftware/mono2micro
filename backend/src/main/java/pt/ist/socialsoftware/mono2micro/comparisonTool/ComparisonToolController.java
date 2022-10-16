package pt.ist.socialsoftware.mono2micro.comparisonTool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.socialsoftware.mono2micro.comparisonTool.dto.ComparisonToolDtoFactory;
import pt.ist.socialsoftware.mono2micro.comparisonTool.domain.MoJoCalculations;
import pt.ist.socialsoftware.mono2micro.comparisonTool.dto.interfaces.MoJoProperties;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.comparisonTool.dto.ComparisonToolDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;

@RestController
@RequestMapping(value = "/mono2micro")
public class ComparisonToolController {
    private static final Logger logger = LoggerFactory.getLogger(ComparisonToolController.class);

    @Autowired
    DecompositionService decompositionService;

    @RequestMapping(value = "/comparison/{decomposition1Name}/{decomposition2Name}", method = RequestMethod.POST)
    public ResponseEntity<ComparisonToolDto> getAnalysis(
            @PathVariable String decomposition1Name,
            @PathVariable String decomposition2Name
    ) {
        logger.debug("getAnalysis");

        try {
            Decomposition decomposition1 = decompositionService.updateDecomposition(decomposition1Name);
            Decomposition decomposition2 = decompositionService.updateDecomposition(decomposition2Name);

            ComparisonToolDto comparisonToolDto = ComparisonToolDtoFactory.getComparisonToolDto(decomposition1, decomposition2);

            MoJoCalculations.getAnalysis((MoJoProperties) comparisonToolDto, decomposition1, decomposition2);
            comparisonToolDto.setDecomposition1(DecompositionDtoFactory.getDecompositionDto(decomposition1));
            comparisonToolDto.setDecomposition2(DecompositionDtoFactory.getDecompositionDto(decomposition2));
            return new ResponseEntity<>(comparisonToolDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
