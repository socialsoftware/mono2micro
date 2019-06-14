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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.socialsoftware.mono2micro.domain.ProfileGroup;
import pt.ist.socialsoftware.mono2micro.manager.ProfileManager;

@RestController
@RequestMapping(value = "/mono2micro")
public class ProfileController {

    private static Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private String profilesFolder = "src/main/resources/profiles/";

    private ProfileManager profileManager = new ProfileManager();


    @RequestMapping(value = "/profileGroupNames", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getProfileGroupNames() {
		logger.debug("getProfileGroupNames");

		return new ResponseEntity<>(profileManager.getProfileGroupNames(), HttpStatus.OK);
	}


	@RequestMapping(value = "/profileGroups", method = RequestMethod.GET)
	public ResponseEntity<List<ProfileGroup>> getProfileGroups() {
		logger.debug("getProfileGroups");

		return new ResponseEntity<>(profileManager.getProfileGroups(), HttpStatus.OK);
	}


	@RequestMapping(value = "/profileGroup", method = RequestMethod.GET)
	public ResponseEntity<ProfileGroup> getProfileGroup(@RequestParam("profileGroupName") String profileGroupName) {
		logger.debug("getProfileGroup");

		return new ResponseEntity<ProfileGroup>(profileManager.getProfileGroup(profileGroupName), HttpStatus.OK);
    }
    

    @RequestMapping(value = "/deleteProfileGroup", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteProfileGroup(@RequestParam("profileGroupName") String profileGroupName) {
		logger.debug("deleteProfileGroup");

		boolean deleted = profileManager.deleteProfileGroup(profileGroupName);
		if (deleted)
			return new ResponseEntity<>(HttpStatus.OK);
		else
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    

    @RequestMapping(value = "/addProfile", method = RequestMethod.GET)
	public ResponseEntity<ProfileGroup> addProfile(@RequestParam("profileGroupName") String profileGroupName, @RequestParam("profile") String profile) {
		logger.debug("addProfile");

        ProfileGroup profileGroup = profileManager.getProfileGroup(profileGroupName);
        profileGroup.addProfile(profile, new ArrayList<>());
        profileManager.writeProfileGroup(profileGroupName, profileGroup);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/moveController", method = RequestMethod.GET)
	public ResponseEntity<ProfileGroup> moveController(@RequestParam("profileGroupName") String profileGroupName, @RequestParam("moveController") String moveController, @RequestParam("moveToProfile") String moveToProfile) {
		logger.debug("moveController");

        ProfileGroup profileGroup = profileManager.getProfileGroup(profileGroupName);
        profileGroup.moveController(moveController, moveToProfile);
        profileManager.writeProfileGroup(profileGroupName, profileGroup);
		return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/deleteProfile", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteProfile(@RequestParam("profileGroupName") String profileGroupName, @RequestParam("profileName") String profileName) {
		logger.debug("deleteProfile");

        ProfileGroup profileGroup = profileManager.getProfileGroup(profileGroupName);
        profileGroup.deleteProfile(profileName);
        profileManager.writeProfileGroup(profileGroupName, profileGroup);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/createProfileGroup", method = RequestMethod.POST)
    public ResponseEntity<ProfileGroup> createProfileGroup(@RequestParam("profileGroupName") String profileGroupName,
            @RequestParam("file") MultipartFile datafile) {

        logger.debug("createDendrogram filename: {}", datafile.getOriginalFilename());

        File directory = new File(profilesFolder);
        if (!directory.exists())
            directory.mkdir();

        for (String name : profileManager.getProfileGroupNames()) {
            if (name.toUpperCase().equals(profileGroupName.toUpperCase()))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ProfileGroup profileGroup = new ProfileGroup(profileGroupName);

        
        try {
            //store datafile
            FileOutputStream outputStream = new FileOutputStream(profilesFolder + profileGroupName + ".txt");
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
            profileGroup.addProfile("Generic", controllers);

            profileManager.writeProfileGroup(profileGroupName, profileGroup);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}