package pt.ist.socialsoftware.mono2micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.source.Source;
import pt.ist.socialsoftware.mono2micro.domain.source.SourceFactory;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
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

	@RequestMapping(value = "/codebase/{codebaseName}/strategies", method = RequestMethod.GET)
	public ResponseEntity<List<Strategy>> getCodebaseStrategies(
			@PathVariable String codebaseName,
			@RequestParam(required = false) List<String> strategyTypes
	) {
		logger.debug("getCodebaseStrategies");

		try {
			return new ResponseEntity<>(codebaseManager.getCodebaseStrategies(codebaseName, strategyTypes), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/sources", method = RequestMethod.GET)
	public ResponseEntity<List<Source>> getCodebaseSources(@PathVariable String codebaseName) {
		logger.debug("getCodebaseSources");

		try {
			return new ResponseEntity<>(codebaseManager.getCodebaseSources(codebaseName), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/decompositions", method = RequestMethod.GET)
	public ResponseEntity<List<Decomposition>> getCodebaseDecompositions(
		@PathVariable String codebaseName,
		@RequestParam(required = false, defaultValue = "") String strategyType
	) {
		logger.debug("getCodebaseDecompositions");

		try {
			return new ResponseEntity<>(
				codebaseManager.getCodebaseDecompositions(
					codebaseName,
					strategyType
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

    @RequestMapping(value = "/codebase/create", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> createCodebase(
        @RequestParam String codebaseName
    ){
        logger.debug("createCodebase");

        try {
            Codebase codebase = codebaseManager.createCodebase(codebaseName);

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

	@RequestMapping(value = "/codebase/{codebaseName}/addCollector", method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> addCollector(
		@PathVariable String codebaseName,
		@RequestParam String collectorName,
		@RequestParam List<String> sourceTypes,
		@RequestParam List<Object> sources
	){
		logger.debug("addCollector");

		try {
			if (sourceTypes.size() != sources.size())
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			if (codebase.getCollectors().contains(collectorName))
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

			for(int i = 0; i < sourceTypes.size(); i++) {
				String sourceType = sourceTypes.get(i);
				Object inputFile = sources.get(i);
				Source source = SourceFactory.getFactory().getSource(sourceType);
				source.init(codebaseName, inputFile);
				codebaseManager.writeSource(codebaseName, sourceType, source);
			}
			codebase.addCollector(collectorName);
			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.CREATED);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/collector/{collectorType}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCollector(
		@PathVariable String codebaseName,
		@PathVariable String collectorType,
		@RequestParam List<String> sources,
		@RequestParam List<String> possibleStrategies
	) {
		logger.debug("deleteCollector");

		try {
			Codebase codebase = codebaseManager.getCodebase(codebaseName);
			codebaseManager.deleteSources(codebaseName, sources);
			codebaseManager.deleteCodebaseStrategies(codebaseName, possibleStrategies);
			codebase.removeCollector(collectorType);
			codebaseManager.writeCodebase(codebase);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}