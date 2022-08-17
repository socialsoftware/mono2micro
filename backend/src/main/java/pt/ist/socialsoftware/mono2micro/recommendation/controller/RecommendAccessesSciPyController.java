package pt.ist.socialsoftware.mono2micro.recommendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendAccessesSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendAccessesSciPyService;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendationService;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.*;

@RestController
@RequestMapping(value = "/mono2micro")
public class RecommendAccessesSciPyController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendAccessesSciPyController.class);

	@Autowired
	RecommendAccessesSciPyService recommendAccessesSciPyService;

	@Autowired
	RecommendationService recommendationService;

	@RequestMapping(value = "/recommendation/createRecommendAccessesSciPy", method = RequestMethod.PUT)
	public ResponseEntity<RecommendAccessesSciPyDto> createRecommendAccessesSciPy(
			@RequestBody RecommendAccessesSciPyDto recommendationDto
	) {
		logger.debug("Accesses SciPy Recommendation");

		try {
			return new ResponseEntity<>(new RecommendAccessesSciPyDto(recommendAccessesSciPyService.recommendAccessesSciPy(recommendationDto)), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/recommendAccessesSciPy/{recommendationName}/getRecommendationResult", method = RequestMethod.GET)
	public ResponseEntity<String> getRecommendationResult(
			@PathVariable String recommendationName
	) {
		logger.debug("get Accesses SciPy recommendation result");

		try {
			return new ResponseEntity<>(recommendAccessesSciPyService.getRecommendationResultByName(recommendationName), HttpStatus.OK);

		} catch (NoSuchFileException e) { // Since it is an asynchronous call, the file might not be created yet
			return new ResponseEntity<>(null, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/recommendAccessesSciPy/{recommendationName}/createDecompositions")
	public ResponseEntity<HttpStatus> createDecompositions(
			@PathVariable String recommendationName,
			@RequestParam List<String> decompositionNames
	) {
		try {
			logger.debug("createDecompositions");

			recommendAccessesSciPyService.createDecompositions(recommendationName, decompositionNames);
			logger.debug("decomposition creation ended");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}