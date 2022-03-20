package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.IOException;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}/strategy/")
public class DendrogramController {

	private static final Logger logger = LoggerFactory.getLogger(DendrogramController.class);

	private final CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "{strategyName}/image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getDendrogramImage(
		@PathVariable String codebaseName,
		@PathVariable String strategyName
	) {
		logger.debug("getDendrogramImage");

		try {
			return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.body(codebaseManager.getDendrogramImage(codebaseName, strategyName));

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}