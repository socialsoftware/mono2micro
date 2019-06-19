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
@RequestMapping(value = "/mono2micro")
public class ExpertController {

    private static Logger logger = LoggerFactory.getLogger(ExpertController.class);

    private String expertFolder = "src/main/resources/experts/";

    private String codebaseFolder = "src/main/resources/codebases/";

    private ExpertManager expertManager = new ExpertManager();


    @RequestMapping(value = "/expertNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getExpertNames() {
		logger.debug("getExpertNames");

		return new ResponseEntity<>(expertManager.getExpertNames(), HttpStatus.OK);
	}


	@RequestMapping(value = "/experts", method = RequestMethod.GET)
	public ResponseEntity<List<Expert>> getExperts() {
		logger.debug("getExperts");

		return new ResponseEntity<>(expertManager.getExperts(), HttpStatus.OK);
	}


	@RequestMapping(value = "/expert/{name}", method = RequestMethod.GET)
	public ResponseEntity<Expert> getExpert(@PathVariable String name) {
		logger.debug("getExpert");

		return new ResponseEntity<>(expertManager.getExpert(name), HttpStatus.OK);
    }
    

    @RequestMapping(value = "/expert/{name}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteExpert(@PathVariable String name) {
		logger.debug("deleteExpert");

		boolean deleted = expertManager.deleteExpert(name);
		if (deleted)
			return new ResponseEntity<>(HttpStatus.OK);
		else
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    

    @RequestMapping(value = "/expert/{name}/addCluster", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> addCluster(@PathVariable String name, @RequestParam String cluster) {
		logger.debug("addCluster");

        Expert expert = expertManager.getExpert(name);
        expert.addCluster(cluster, new ArrayList<>());
        expertManager.writeExpert(name, expert);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/expert/{name}/moveEntities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> moveEntities(@PathVariable String name, @RequestBody String[] entities, @RequestParam String cluster) {
		logger.debug("moveEntities");

        Expert expert = expertManager.getExpert(name);
        expert.moveEntities(entities, cluster);
        expertManager.writeExpert(name, expert);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/expert/{name}/deleteCluster", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCluster(@PathVariable String name, @RequestParam String cluster) {
		logger.debug("deleteCluster");

        Expert expert = expertManager.getExpert(name);
        expert.deleteCluster(cluster);
        expertManager.writeExpert(name, expert);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/expert/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createExpert(@RequestBody Expert expert) {

        logger.debug("createExpert");

        File directory = new File(expertFolder);
        if (!directory.exists())
            directory.mkdir();

        for (String expertName : expertManager.getExpertNames()) {
            if (expert.getName().toUpperCase().equals(expertName.toUpperCase()))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        try {

            // read datafile
            InputStream is = new FileInputStream(codebaseFolder + expert.getCodebase() + ".txt");
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

            expertManager.writeExpert(expert.getName(), expert);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}