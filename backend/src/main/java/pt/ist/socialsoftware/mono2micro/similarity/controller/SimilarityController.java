package pt.ist.socialsoftware.mono2micro.similarity.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDtoFactory;
import pt.ist.socialsoftware.mono2micro.similarity.service.SimilarityService;

import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro")
public class SimilarityController {

    private static final Logger logger = LoggerFactory.getLogger(SimilarityController.class);

	@Autowired
	SimilarityService similarityService;

	@PostMapping(value = "/similarity/create")
	public ResponseEntity<HttpStatus> createSimilarity(
			@RequestBody SimilarityDto similarityDto
	) {
		logger.debug("Create Similarity Distances");

		try {
			similarityService.createSimilarity(similarityDto);

			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/similarity/{similarityName}/getSimilarity")
	public ResponseEntity<SimilarityDto> getSimilarity(
			@PathVariable String similarityName
	) {
		logger.debug("getSimilarity");

		try {
			return new ResponseEntity<>(
					SimilarityDtoFactory.getSimilarityDto(similarityService.getSimilarity(similarityName)),
					HttpStatus.OK
			);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/similarity/{similarityName}/decompositions")
	public ResponseEntity<List<DecompositionDto>> getDecompositions(
			@PathVariable String similarityName
	) {
		logger.debug("getDecompositions");

		try {
			return new ResponseEntity<>(
					DecompositionDtoFactory.getDecompositionDtos(similarityService.getDecompositions(similarityName)),
					HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping(value = "/similarity/{similarityName}/delete")
	public ResponseEntity<HttpStatus> deleteSimilarity(
			@PathVariable String similarityName
	) {
		logger.debug("Delete Similarity");

		try {
			similarityService.deleteSingleSimilarity(similarityName);

			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/similarity/{similarityName}/image")
	public ResponseEntity<byte[]> getDendrogramImage(
			@PathVariable String similarityName
	) {
		logger.debug("getDendrogramImage");

		try {
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_PNG)
					.body(similarityService.getDendrogramImage(similarityName));

		} catch (Exception e) {
			System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}