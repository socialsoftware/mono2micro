package pt.ist.socialsoftware.mono2micro.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Expert;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.manager.ExpertManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class ExpertController {

    private static Logger logger = LoggerFactory.getLogger(ExpertController.class);

    private String codebaseFolder = "src/main/resources/codebases/";

    private CodebaseManager codebaseManager = new CodebaseManager();


    @RequestMapping(value = "/expertNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getExpertNames(@PathVariable String codebaseName) {
		logger.debug("getExpertNames");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getExpertNames(), HttpStatus.OK);
	}


	@RequestMapping(value = "/experts", method = RequestMethod.GET)
	public ResponseEntity<List<Expert>> getExperts(@PathVariable String codebaseName) {
		logger.debug("getExperts");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getExperts(), HttpStatus.OK);
	}


	@RequestMapping(value = "/expert/{expertName}", method = RequestMethod.GET)
	public ResponseEntity<Expert> getExpert(@PathVariable String codebaseName, @PathVariable String expertName) {
		logger.debug("getExpert");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getExpert(expertName), HttpStatus.OK);
    }
    

    @RequestMapping(value = "/expert/{expertName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteExpert(@PathVariable String codebaseName, @PathVariable String expertName) {
		logger.debug("deleteExpert");

		Codebase codebase = codebaseManager.getCodebase(codebaseName);
		boolean deleted = codebase.deleteExpert(expertName);
		if (deleted) {
            codebaseManager.writeCodebase(codebaseName, codebase);
			return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    

    @RequestMapping(value = "/expert/{expertName}/addCluster", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> addCluster(@PathVariable String codebaseName, @PathVariable String expertName, @RequestParam String cluster) {
		logger.debug("addCluster");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);
        codebase.getExpert(expertName).addCluster(cluster, new ArrayList<>());
        codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/expert/{expertName}/moveEntities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> moveEntities(@PathVariable String codebaseName, @PathVariable String expertName, @RequestBody String[] entities, @RequestParam String cluster) {
		logger.debug("moveEntities");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);
        codebase.getExpert(expertName).moveEntities(entities, cluster);
        codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/expert/{expertName}/deleteCluster", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCluster(@PathVariable String codebaseName, @PathVariable String expertName, @RequestParam String cluster) {
		logger.debug("deleteCluster");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);
        codebase.getExpert(expertName).deleteCluster(cluster);
        codebaseManager.writeCodebase(codebaseName, codebase);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/expert/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createExpert(@PathVariable String codebaseName, @RequestBody Expert expert) {

        logger.debug("createExpert");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);

        for (String expertName : codebase.getExpertNames()) {
            if (expert.getName().toUpperCase().equals(expertName.toUpperCase()))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        try {

            // read datafile
            InputStream is = new FileInputStream(codebaseFolder + codebaseName + "/" + codebaseName + ".txt");
            JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
            is.close();

            Iterator<String> controllers = datafileJSON.sortedKeys();
            List<String> entities = new ArrayList<>();
            while (controllers.hasNext()) {
                JSONArray entitieArrays = datafileJSON.getJSONArray(controllers.next());
                for (int i = 0; i < entitieArrays.length(); i++) {
                    JSONArray entityArray = entitieArrays.getJSONArray(i);
                    String entity = entityArray.getString(0);
                    if (!entities.contains(entity))
                        entities.add(entity);
                }
            }
            expert.addCluster("Generic", entities);

            codebase.addExpert(expert);
            codebaseManager.writeCodebase(codebaseName, codebase);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}