package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/mono2micro/codebase/{codebaseName}")
public class DendrogramController {

	private static final Logger logger = LoggerFactory.getLogger(DendrogramController.class);

	private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

	@RequestMapping(value = "/dendrograms", method = RequestMethod.GET)
	public ResponseEntity<List<Dendrogram>> getDendrograms(
		@PathVariable String codebaseName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getDendrograms");

		try {
			return new ResponseEntity<>(
				codebaseManager.getCodebaseDendrogramsWithFields(
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

	@RequestMapping(value = "/dendrogram/{dendrogramName}", method = RequestMethod.GET)
	public ResponseEntity<Dendrogram> getDendrogram(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@RequestParam List<String> fieldNames
	) {
		logger.debug("getDendrogram");

		try {
			// FIXME Instead of parsing the whole list of dendrograms, it would be much better to just parse the right one
			// FIXME or the respective dendrogram directory could also have a dendrogram.json
			return new ResponseEntity<>(
				codebaseManager.getCodebaseDendrogramWithFields(
					codebaseName,
					dendrogramName,
					new HashSet<>(fieldNames)
				),
				HttpStatus.OK
			);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/dendrogram/{dendrogramName}/image", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getDendrogramImage(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName
	) {
		logger.debug("getDendrogramImage");

		try {
			return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.body(codebaseManager.getDendrogramImage(codebaseName, dendrogramName));

		} catch (IOException e) {
			System.err.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/dendrogram/{dendrogramName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteDendrogram(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName
	) {
		logger.debug("deleteDendrogram");
		
		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.deleteDendrogram(dendrogramName);
			codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}


	@RequestMapping(value = "/dendrogram/create", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogram(
		@PathVariable String codebaseName,
		@RequestBody Dendrogram dendrogram
	) {
		logger.debug("createDendrogram");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.createDendrogram(dendrogram);

            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}

	@RequestMapping(value = "/dendrogram/create/features", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogramByFeatures(
		@PathVariable String codebaseName,
		@RequestBody Dendrogram dendrogram
	) {
		logger.debug("createDendrogramByFeatures");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.createDendrogramByFeatures(dendrogram);

            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}

	@RequestMapping(value = "/dendrogram/create/entities", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogramByEntities(
			@PathVariable String codebaseName,
			@RequestBody Dendrogram dendrogram
	) {
		logger.debug("createDendrogramByClass");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.createDendrogramByEntities(dendrogram);

			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.CREATED);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/dendrogram/create/class", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createDendrogramByClass(
		@PathVariable String codebaseName,
		@RequestBody Dendrogram dendrogram
	) {
		logger.debug("createDendrogramByClass");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebase.createDendrogramByClass(dendrogram);

            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.CREATED);

		} catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}

	@RequestMapping(value = "/dendrogram/{dendrogramName}/cut", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> cutDendrogram(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@RequestBody Decomposition decomposition
	) {
		logger.debug("cutDendrogram");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"
			Codebase codebase = codebaseManager.getCodebase(codebaseName);

			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);

			// FIXME the graph given to the cut function shouldn't be a Decomposition
			// FIXME The result of the cut function SHOULD be a decomposition
			// FIXME Did not have the patience to code it well

			Decomposition cutDecomposition;

			if (dendrogram.getAnalysisType().equals("static")) {
				cutDecomposition = dendrogram.cut(decomposition);
			} else if (dendrogram.getAnalysisType().equals("feature")) {
				cutDecomposition = dendrogram.cutFeaturesAnalysis(decomposition, dendrogram.getFeatureVectorizationStrategy());
			} else if (dendrogram.getAnalysisType().equals("entities")) {
				cutDecomposition = dendrogram.cutEntitiesAnalysis(decomposition);
			} else {
				cutDecomposition = dendrogram.cutClassesAnalysis(decomposition);
			}

			decomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				dendrogram.getProfile(),
				decomposition.getEntityIDToClusterID()
			));

			cutDecomposition.calculateMetrics(
				codebase,
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTraceType(),
				false
			);


			dendrogram.addDecomposition(cutDecomposition);

            codebaseManager.writeCodebase(codebase);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}

	@RequestMapping(value = "/dendrogram/{dendrogramName}/expertCut", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> createExpertCut(
		@PathVariable String codebaseName,
		@PathVariable String dendrogramName,
		@RequestParam String expertName,
		@RequestParam Optional<MultipartFile> expertFile
	) {
		logger.debug("createExpertCut");

		try {
			// FIXME The whole codebase needs to be fetched because it needs to be written as a whole again
			// FIXME The best solution would be each "dendrogram directory could also have a dendrogram.json"

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			Dendrogram dendrogram = codebase.getDendrogram(dendrogramName);

			Decomposition decomposition = dendrogram.createExpertCut(
            	expertName,
				expertFile
			);

			decomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
				codebase,
				dendrogram.getProfile(),
				decomposition.getEntityIDToClusterID()
			));

			decomposition.calculateMetrics(
				codebase,
				dendrogram.getTracesMaxLimit(),
				dendrogram.getTraceType(),
					false);

			dendrogram.addDecomposition(decomposition);

            codebaseManager.writeCodebase(codebase);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (KeyAlreadyExistsException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
	}
}