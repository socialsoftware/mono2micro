package pt.ist.socialsoftware.mono2micro.dendrogram.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.DendrogramDto;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.DendrogramDtoFactory;
import pt.ist.socialsoftware.mono2micro.dendrogram.service.DendrogramService;

import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro")
public class DendrogramController {

    private static final Logger logger = LoggerFactory.getLogger(Dendrogram.class);

	@Autowired
	DendrogramService dendrogramService;

	@RequestMapping(value = "/dendrogram/{dendrogramName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDendrogram(
			@PathVariable String dendrogramName
	) {
		logger.debug("Delete Dendrogram");

		try {
			dendrogramService.deleteSingleDendrogram(dendrogramName);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/dendrogram/{dendrogramName}/decompositions", method = RequestMethod.GET)
	public ResponseEntity<List<DecompositionDto>> getDecompositions(
			@PathVariable String dendrogramName
	) {
		logger.debug("getDecompositions");

		try {
			return new ResponseEntity<>(
					DecompositionDtoFactory.getFactory().getDecompositionDtos(dendrogramService.getDecompositions(dendrogramName)),
					HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/dendrogram/{dendrogramName}/getDendrogram", method = RequestMethod.GET)
	public ResponseEntity<DendrogramDto> getDendrogram(
			@PathVariable String dendrogramName
	) {
		logger.debug("getDendrogram");

		try {
			return new ResponseEntity<>(
					DendrogramDtoFactory.getFactory().getDendrogramDto(dendrogramService.getDendrogram(dendrogramName)),
					HttpStatus.OK
			);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}