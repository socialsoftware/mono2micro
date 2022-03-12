package pt.ist.socialsoftware.mono2micro.controller;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping(value = "/mono2micro")
public class CodebaseController {

    private static Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/codebases", method = RequestMethod.GET)
	public ResponseEntity<List<Codebase>> getCodebases(
		@RequestParam(required = false, defaultValue = "") List<String> fieldNames
	) {
		logger.debug("getCodebases");

		try {
			return new ResponseEntity<>(
				codebaseManager.getCodebasesWithFields(new HashSet<>(fieldNames)),
				HttpStatus.OK
			);
		}

		catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}", method = RequestMethod.GET)
	public ResponseEntity<Codebase> getCodebase(
		@PathVariable String codebaseName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getCodebase");

		try {
			return new ResponseEntity<>(
				codebaseManager.getCodebaseWithFields(
					codebaseName,
					new HashSet<>(fieldNames)
				),
				HttpStatus.OK
			);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }

	@RequestMapping(value = "/codebase/{codebaseName}/decompositions", method = RequestMethod.GET)
	public ResponseEntity<List<Decomposition>> getCodebaseDecompositions(
		@PathVariable String codebaseName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getCodebaseDecompositions");

		try {
			return new ResponseEntity<>(
				codebaseManager.getCodebaseDecompositionsWithFields(
					codebaseName,
					new HashSet<>(fieldNames)
				),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		}
	}

    @RequestMapping(value = "/codebase/{codebaseName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCodebase(@PathVariable String codebaseName) {
		logger.debug("deleteCodebase");

        try {
            codebaseManager.deleteCodebase(codebaseName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/codebase/{codebaseName}/addProfile", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> addProfile(
		@PathVariable String codebaseName,
		@RequestParam String profile
	) {
        logger.debug("addProfile");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.addProfile(profile, new HashSet<>());
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/codebase/{codebaseName}/moveControllers", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> moveControllers(
		@PathVariable String codebaseName,
		@RequestBody String[] controllers,
		@RequestParam String targetProfile
	) {
		logger.debug("moveControllers");
        
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.moveControllers(controllers, targetProfile);
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/codebase/{codebaseName}/deleteProfile", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteProfile(
		@PathVariable String codebaseName,
		@RequestParam String profile
	) {
		logger.debug("deleteProfile");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.deleteProfile(profile);
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/codebase/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createCodebase(
        @RequestParam String codebaseName,
        @RequestParam Object datafile,
        @RequestParam Object translationFile,
		@RequestParam Object codeEmbeddingsFile
    ){
        logger.debug("createCodebase");

        try {
            Codebase codebase = codebaseManager.createCodebase(
            	codebaseName,
				datafile,
				translationFile,
				codeEmbeddingsFile
			);

            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (KeyAlreadyExistsException e) {
        	e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

	@RequestMapping(value = "/codebase/{codebaseName}/translation", method = RequestMethod.GET)
	public ResponseEntity<String> getTranslation(
			@PathVariable String codebaseName
	) {
		logger.debug("getTranslation");

		try {
			return new ResponseEntity<>(codebaseManager.getTranslation(codebaseName), HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}