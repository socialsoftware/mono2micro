package pt.ist.socialsoftware.mono2micro.decomposition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;

@RestController
@RequestMapping(value = "/mono2micro")
public class DecompositionController {

	private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

	@Autowired
	DecompositionService decompositionService;

	@RequestMapping(value = "/decomposition/{decompositionName}", method = RequestMethod.GET)
	public ResponseEntity<DecompositionDto> getDecomposition(
		@PathVariable String decompositionName
	) {
		logger.debug("getDecomposition");

		try {
			return new ResponseEntity<>(
				DecompositionDtoFactory.getFactory().getDecompositionDto(decompositionService.getDecomposition(decompositionName)),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/decomposition/{decompositionName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDecomposition(
		@PathVariable String decompositionName
	) {
		logger.debug("deleteDecomposition");

		try {
			decompositionService.deleteSingleDecomposition(decompositionName);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}