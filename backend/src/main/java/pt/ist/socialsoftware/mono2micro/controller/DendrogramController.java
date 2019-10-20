package pt.ist.socialsoftware.mono2micro.controller;

import java.io.IOException;
import java.util.List;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Graph;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class DendrogramController {

	private static Logger logger = LoggerFactory.getLogger(DendrogramController.class);

	private CodebaseManager codebaseManager = CodebaseManager.getInstance();



	@RequestMapping(value = "/dendrograms", method = RequestMethod.GET)
	public ResponseEntity<List<Dendrogram>> getDendrograms(@PathVariable String codebaseName) {
		logger.debug("getDendrograms");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrograms(), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> getDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("getDendrogram");

		return new ResponseEntity<>(codebaseManager.getCodebase(codebaseName).getDendrogram(dendrogramName), HttpStatus.OK);
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getDendrogramImage(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("getDendrogramImage");

		try {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(codebaseManager.getDendrogramImage(codebaseName, dendrogramName));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName) {
		logger.debug("deleteDendrogram");
		
		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.deleteDendrogram(dendrogramName);
			codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}


	@RequestMapping(value = "/dendrogram/create", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogram(@PathVariable String codebaseName, @RequestBody Dendrogram dendrogram) {
		logger.debug("createDendrogram");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.createDendrogram(dendrogram);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/cut", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> cutDendrogram(@PathVariable String codebaseName, @PathVariable String dendrogramName, @RequestBody Graph graph) {
		logger.debug("cutDendrogram");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.getDendrogram(dendrogramName).cut(graph);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}


	@RequestMapping(value = "/dendrogram/{dendrogramName}/expertCut", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createExpertCut(@PathVariable String codebaseName, @PathVariable String dendrogramName, @RequestBody Graph graph) {
		logger.debug("createExpertCut");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.getDendrogram(dendrogramName).createExpertCut(graph);
            codebaseManager.writeCodebase(codebaseName, codebase);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (KeyAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}
}