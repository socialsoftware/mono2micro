package pt.ist.socialsoftware.mono2micro.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_FOLDER;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class ExpertController {

    private static Logger logger = LoggerFactory.getLogger(ExpertController.class);

    private CodebaseManager codebaseManager = CodebaseManager.getInstance();


    @RequestMapping(value = "/expertNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getExpertNames(@PathVariable String codebaseName) {
		logger.debug("getExpertNames");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getExpertNames(), HttpStatus.OK);
	}


	@RequestMapping(value = "/experts", method = RequestMethod.GET)
	public ResponseEntity<List<Graph>> getExperts(@PathVariable String codebaseName) {
		logger.debug("getExperts");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getExperts(), HttpStatus.OK);
	}


	@RequestMapping(value = "/expert/{expertName}", method = RequestMethod.GET)
	public ResponseEntity<Graph> getExpert(@PathVariable String codebaseName, @PathVariable String expertName) {
		logger.debug("getExpert");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getExpert(expertName), HttpStatus.OK);
    }
    

    @RequestMapping(value = "/expert/{expertName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteExpert(@PathVariable String codebaseName, @PathVariable String expertName) {
		logger.debug("deleteExpert");

        try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.deleteExpert(expertName);
			codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }
    

    @RequestMapping(value = "/expert/{expertName}/addCluster", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> addCluster(@PathVariable String codebaseName, @PathVariable String expertName, @RequestParam String cluster) {
		logger.debug("addCluster");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.getExpert(expertName).addCluster(new Cluster(cluster));
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
		} catch (KeyAlreadyExistsException e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }


    @RequestMapping(value = "/expert/{expertName}/moveEntities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> moveEntities(@PathVariable String codebaseName, @PathVariable String expertName, @RequestBody String[] entities, @RequestParam String cluster) {
		logger.debug("moveEntities");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.getExpert(expertName).moveEntities(entities, cluster);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }


    @RequestMapping(value = "/expert/{expertName}/deleteCluster", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCluster(@PathVariable String codebaseName, @PathVariable String expertName, @RequestParam String cluster) {
		logger.debug("deleteCluster");

        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.getExpert(expertName).deleteCluster(cluster);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }


    @RequestMapping(value = "/expert/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createExpert(@PathVariable String codebaseName, @RequestBody Graph expert) {
        logger.debug("createExpert");

        try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.createExpert(expert);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (IOException | JSONException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}