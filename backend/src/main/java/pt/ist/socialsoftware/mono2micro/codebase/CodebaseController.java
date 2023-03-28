package pt.ist.socialsoftware.mono2micro.codebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.codebase.dto.CodebaseDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDto;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.DecompositionDtoFactory;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/mono2micro")
public class CodebaseController {

    private static final Logger logger = LoggerFactory.getLogger(CodebaseController.class);

	@Autowired
	CodebaseService codebaseService;

	@GetMapping(value = "/codebases")
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

	@GetMapping(value = "/codebase/{codebaseName}")
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

	@GetMapping(value = "/codebase/{codebaseName}/getRepresentations")
	public ResponseEntity<List<RepresentationDto>> getRepresentations(
			@PathVariable String codebaseName
	) {
		logger.debug("getRepresentations");

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

	@GetMapping(value = "/codebase/{codebaseName}/getCodebaseStrategies")
	public ResponseEntity<List<StrategyDto>> getCodebaseStrategies(@PathVariable String codebaseName) {
		logger.debug("getCodebaseStrategies");

		try {
			List<StrategyDto> strategies = codebaseService.getCodebaseStrategies(codebaseName).stream().map(StrategyDto::new).collect(Collectors.toList());
			return new ResponseEntity<>(strategies, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/codebase/{codebaseName}/getAllowableCodebaseStrategyTypes")
	public ResponseEntity<List<String>> getAllowableCodebaseStrategyTypes(@PathVariable String codebaseName) {
		logger.debug("getPossibleCodebaseStrategyTypes");

		try {
			return new ResponseEntity<>(codebaseService.getAllowableCodebaseStrategyTypes(codebaseName), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/codebase/{codebaseName}/getCodebaseDecompositions")
	public ResponseEntity<List<DecompositionDto>> getCodebaseDecompositions(
			@PathVariable String codebaseName
	) {
		logger.debug("getCodebaseDecompositions");

		try {
			List<DecompositionDto> decompositionDtos = DecompositionDtoFactory.getDecompositionDtos(
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

    @DeleteMapping(value = "/codebase/{codebaseName}/delete")
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

    @PostMapping(value = "/codebase/create")
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

	@GetMapping(value = "/codebase/{codebaseName}/getCodebaseRepresentationGroups")
	public ResponseEntity<List<String>> getCodebaseRepresentationGroups(
			@PathVariable String codebaseName
	){
		logger.debug("getCodebaseRepresentationInfoTypes");

		try {
			return new ResponseEntity<>(codebaseService.getCodebaseRepresentationGroups(codebaseName), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/codebase/getRepresentationGroups")
	public ResponseEntity<Map<String, List<String>>> getRepresentationGroups() {
		logger.debug("getRepresentationInfoTypes");

		try {
			return new ResponseEntity<>(codebaseService.getRepresentationGroups(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}