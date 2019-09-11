package pt.ist.socialsoftware.mono2micro.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro")
public class CodebaseController {

    private static Logger logger = LoggerFactory.getLogger(CodebaseController.class);

    private String codebaseFolder = "src/main/resources/codebases/";

    private CodebaseManager codebaseManager = new CodebaseManager();


    @RequestMapping(value = "/codebaseNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getCodebaseNames() {
		logger.debug("getCodebaseNames");

		return new ResponseEntity<>(codebaseManager.getCodebaseNames(), HttpStatus.OK);
	}


	@RequestMapping(value = "/codebases", method = RequestMethod.GET)
	public ResponseEntity<List<Codebase>> getCodebases() {
		logger.debug("getCodebases");

		return new ResponseEntity<>(codebaseManager.getCodebases(), HttpStatus.OK);
	}


	@RequestMapping(value = "/codebase/{name}", method = RequestMethod.GET)
	public ResponseEntity<Codebase> getCodebase(@PathVariable String codebaseName) {
		logger.debug("getCodebase");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName), HttpStatus.OK);
    }
    

    @RequestMapping(value = "/codebase/{name}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCodebase(@PathVariable String codebaseName) {
		logger.debug("deleteCodebase");

		boolean deleted = codebaseManager.deleteCodebase(codebaseName);
		if (deleted)
			return new ResponseEntity<>(HttpStatus.OK);
		else
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/codebase/{name}/addProfile", method = RequestMethod.GET)
	public ResponseEntity<HttpStatus> addProfile(@PathVariable String codebaseName, @RequestParam String profile) {
		logger.debug("addProfile");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);
        codebase.addProfile(profile, new ArrayList<>());
        codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @RequestMapping(value = "/codebase/{name}/moveControllers", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> moveControllers(@PathVariable String codebaseName, @RequestBody String[] controllers, @RequestParam String toProfile) {
		logger.debug("moveControllers");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);
        codebase.moveControllers(controllers, toProfile);
        codebaseManager.writeCodebase(codebaseName, codebase);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/codebase/{name}/deleteProfile", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteProfile(@PathVariable String codebaseName, @RequestParam String profile) {
		logger.debug("deleteProfile");

        Codebase codebase = codebaseManager.getCodebase(codebaseName);
        codebase.deleteProfile(profile);
        codebaseManager.writeCodebase(codebaseName, codebase);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/codebase/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createCodebase(@RequestParam String codebaseName, @RequestParam MultipartFile datafile) {
        logger.debug("createCodebase");

        //check for an existent codebase with the same name
        for (String existentCodebaseName : codebaseManager.getCodebaseNames()) {
            if (codebaseName.toUpperCase().equals(existentCodebaseName.toUpperCase()))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //create codebase directory if it doesnt exist
        File codebaseDir = new File(codebaseFolder + codebaseName);
        if (!codebaseDir.exists())
            codebaseDir.mkdir();

        Codebase codebase = new Codebase(codebaseName);
        
        try {
            //store datafile
            FileOutputStream outputStream = new FileOutputStream(codebaseFolder + codebaseName + ".txt");
			outputStream.write(datafile.getBytes());
			outputStream.close();

            // read datafile
            InputStream is = new BufferedInputStream(datafile.getInputStream());
            JSONObject datafileJSON = new JSONObject(IOUtils.toString(is, "UTF-8"));
            is.close();

            Iterator<String> controllerNames = datafileJSON.sortedKeys();
            List<String> controllers = new ArrayList<>();
            while (controllerNames.hasNext()) {
                controllers.add(controllerNames.next());
            }
            codebase.addProfile("Generic", controllers);

            codebaseManager.writeCodebase(codebaseName, codebase);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}