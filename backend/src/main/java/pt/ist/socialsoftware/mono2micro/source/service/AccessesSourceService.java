package pt.ist.socialsoftware.mono2micro.source.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.repository.AccessesSourceRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static pt.ist.socialsoftware.mono2micro.source.domain.TranslationSource.TRANSLATION;

@Service
public class AccessesSourceService {

    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    AccessesSourceRepository accessesSourceRepository;

    @Autowired
    GridFsService gridFsService;

    public void addAccessesProfile(String sourceId, String profile) {
        AccessesSource source = accessesSourceRepository.findById(sourceId).orElseThrow(() -> new RuntimeException("No source " + sourceId + " found."));
        source.addProfile(profile, new HashSet<>());
        accessesSourceRepository.save(source);
    }

    public void moveAccessesFunctionalities(String sourceId, String[] functionalities, String targetProfile) {
        AccessesSource source = accessesSourceRepository.findById(sourceId).orElseThrow(() -> new RuntimeException("No source " + sourceId + " found."));
        source.moveFunctionalities(functionalities, targetProfile);
        accessesSourceRepository.save(source);
    }

    public void deleteAccessesProfile(String sourceId, String profile) {
        AccessesSource source = accessesSourceRepository.findById(sourceId).orElseThrow(() -> new RuntimeException("No source " + sourceId + " found."));
        source.deleteProfile(profile);
        accessesSourceRepository.save(source);
    }

    public String getIdToEntity(String codebaseName) throws IOException {
        Codebase codebase = codebaseRepository.findByName(codebaseName);
        Source source = codebase.getSourceByType(TRANSLATION);
        return IOUtils.toString(gridFsService.getFile(source.getName()), StandardCharsets.UTF_8);
    }
}
