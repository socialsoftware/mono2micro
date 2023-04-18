package pt.ist.socialsoftware.mono2micro.recommendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendationService;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.*;

@RestController
@RequestMapping(value = "/mono2micro")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

	@Autowired
	RecommendationService recommendationService;

	@PutMapping(value = "/recommendation/createRecommendation")
	public ResponseEntity<RecommendationDto> createRecommendation(@RequestBody RecommendationDto recommendationDto) {
		logger.debug("Create recommendation");

		try {
			return new ResponseEntity<>(recommendationService.createRecommendation(recommendationDto), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/recommendation/{recommendationName}/getRecommendationResult")
	public ResponseEntity<String> getRecommendationResult(
			@PathVariable String recommendationName
	) {
		logger.debug("Get recommendation result");

		try {
			return new ResponseEntity<>(recommendationService.getRecommendationResultFromName(recommendationName), HttpStatus.OK);

		} catch (NoSuchFileException e) { // Since it is an asynchronous call, the file might not be created yet
			return new ResponseEntity<>(null, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/recommendation/{recommendationName}/createDecompositions")
	public ResponseEntity<HttpStatus> createDecompositions(
			@PathVariable String recommendationName,
			@RequestParam List<String> decompositionNames
	) {
		try {
			logger.debug("createDecompositions");

			recommendationService.createDecompositions(recommendationName, decompositionNames);
			logger.debug("decomposition creation ended");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}