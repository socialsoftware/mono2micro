package pt.ist.socialsoftware.mono2micro.comparisonTool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.comparisonTool.dto.ComparisonToolResponseFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.comparisonTool.dto.ComparisonToolResponse;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;

@RestController
@RequestMapping(value = "/mono2micro")
public class ComparisonToolController {
    private static final Logger logger = LoggerFactory.getLogger(ComparisonToolController.class);

    @Autowired
    DecompositionService decompositionService;

    @PostMapping(value = "/comparison/{decomposition1Name}/{decomposition2Name}")
    public ResponseEntity<ComparisonToolResponse> getAnalysis(
            @PathVariable String decomposition1Name,
            @PathVariable String decomposition2Name
    ) {
        logger.debug("getAnalysis");

        try {
            Decomposition decomposition1 = decompositionService.updateDecomposition(decomposition1Name);
            Decomposition decomposition2 = decompositionService.updateDecomposition(decomposition2Name);

            ComparisonToolResponse comparisonToolResponse = ComparisonToolResponseFactory.getComparisonToolResponse(decomposition1, decomposition2);
            return new ResponseEntity<>(comparisonToolResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
