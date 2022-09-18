package pt.ist.socialsoftware.mono2micro.codebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.dto.CodebaseDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDtoFactory;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/mono2micro")
public class CodebaseController {

    private static final Logger logger = LoggerFactory.getLogger(CodebaseController.class);

	@Autowired
	CodebaseService codebaseService;

	@RequestMapping(value = "/codebases", method = RequestMethod.GET)
	public ResponseEntity<List<CodebaseDto>> getCodebases() {
		logger.debug("getCodebases");

		try {
			return new ResponseEntity<>(
					codebaseService.getCodebases().stream().map(CodebaseDto::new).collect(Collectors.toList()),
					HttpStatus.OK
			);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}", method = RequestMethod.GET)
	public ResponseEntity<CodebaseDto> getCodebase(
			@PathVariable String codebaseName
	) {
		logger.debug("getCodebase");

		try {
			return new ResponseEntity<>(
					new CodebaseDto(codebaseService.getCodebase(codebaseName)),
					HttpStatus.OK
			);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/getRepresentationTypes", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getRepresentationTypes(
			@PathVariable String codebaseName
	) {
		logger.debug("getRepresentationTypes");

		try {
			return new ResponseEntity<>(
					codebaseService.getRepresentationTypes(codebaseName),
					HttpStatus.OK
			);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/getCodebaseStrategies", method = RequestMethod.GET)
	public ResponseEntity<List<StrategyDto>> getCodebaseStrategies(
			@PathVariable String codebaseName
	) {
		logger.debug("getCodebaseStrategies");

		try {
			List<StrategyDto> strategies = StrategyDtoFactory.getFactory().getStrategyDtos(codebaseService.getCodebaseStrategies(codebaseName));
			return new ResponseEntity<>(strategies, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/codebase/{codebaseName}/getCodebaseDecompositions", method = RequestMethod.GET)
	public ResponseEntity<List<DecompositionDto>> getCodebaseDecompositions(
			@PathVariable String codebaseName
	) {
		logger.debug("getCodebaseDecompositions");

		try {
			List<DecompositionDto> decompositionDtos = DecompositionDtoFactory.getFactory().getDecompositionDtos(
					codebaseService.getCodebaseStrategies(codebaseName).stream()
							.map(Strategy::getDecompositions)
							.flatMap(Collection::stream)
							.collect(Collectors.toList())
			);
			return new ResponseEntity<>(decompositionDtos, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

    @RequestMapping(value = "/codebase/{codebaseName}/delete", method = RequestMethod.DELETE)
	public ResponseEntity<HttpStatus> deleteCodebase(@PathVariable String codebaseName) {
		logger.debug("deleteCodebase");

        try {
			codebaseService.deleteCodebase(codebaseName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
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
			codebaseService.createCodebase(codebaseName);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (KeyAlreadyExistsException e) {
        	e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}