package pt.ist.socialsoftware.mono2micro.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro")
public class CodebaseController {

    private static Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();


	@RequestMapping(value = "/codebases", method = RequestMethod.GET)
	public ResponseEntity<List<Codebase>> getCodebases() {
		logger.debug("getCodebases");

		try {
			return new ResponseEntity<>(codebaseManager.getCodebases(), HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}", method = RequestMethod.GET)
	public ResponseEntity<Codebase> getCodebase(@PathVariable String codebaseName) {
		logger.debug("getCodebase");

		try {
			return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName), HttpStatus.OK);
		} catch (IOException e) {
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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/codebase/{codebaseName}/addProfile", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> addProfile(@PathVariable String codebaseName, @RequestParam String profile) {
        logger.debug("addProfile");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.addProfile(profile, new ArrayList<>());
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/codebase/{codebaseName}/moveControllers", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> moveControllers(@PathVariable String codebaseName, @RequestBody String[] controllers, @RequestParam String targetProfile) {
		logger.debug("moveControllers");
        
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.moveControllers(controllers, targetProfile);
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/codebase/{codebaseName}/deleteProfile", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteProfile(@PathVariable String codebaseName, @RequestParam String profile) {
		logger.debug("deleteProfile");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.deleteProfile(profile);
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/codebase/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createCodebase(
        @RequestParam String codebaseName,
        @RequestParam Object datafile,
        @RequestParam String analysisType
    ){
        logger.debug("createCodebase");

        try {
            Codebase codebase = codebaseManager.createCodebase(codebaseName, datafile, analysisType);
            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (FileNotFoundException e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}