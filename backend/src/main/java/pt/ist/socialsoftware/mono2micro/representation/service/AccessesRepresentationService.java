package pt.ist.socialsoftware.mono2micro.representation.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;
import pt.ist.socialsoftware.mono2micro.representation.repository.AccessesRepresentationRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

@Service
public class AccessesRepresentationService {

    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    AccessesRepresentationRepository accessesRepresentationRepository;

    @Autowired
    GridFsService gridFsService;

    public void addAccessesProfile(String representationId, String profile) {
        AccessesRepresentation representation = accessesRepresentationRepository.findById(representationId).orElseThrow(() -> new RuntimeException("No representation " + representationId + " found."));
        representation.addProfile(profile, new HashSet<>());
        accessesRepresentationRepository.save(representation);
    }

    public void moveAccessesFunctionalities(String representationId, String[] functionalities, String targetProfile) {
        AccessesRepresentation representation = accessesRepresentationRepository.findById(representationId).orElseThrow(() -> new RuntimeException("No representation " + representationId + " found."));
        representation.moveFunctionalities(functionalities, targetProfile);
        accessesRepresentationRepository.save(representation);
    }

    public void deleteAccessesProfile(String representationId, String profile) {
        AccessesRepresentation representation = accessesRepresentationRepository.findById(representationId).orElseThrow(() -> new RuntimeException("No representation " + representationId + " found."));
        representation.deleteProfile(profile);
        accessesRepresentationRepository.save(representation);
    }

    public String getIdToEntity(String codebaseName) throws IOException {
        Codebase codebase = codebaseRepository.findByName(codebaseName);
        Representation representation = codebase.getRepresentationByType(ID_TO_ENTITY);
        return IOUtils.toString(gridFsService.getFile(representation.getName()), StandardCharsets.UTF_8);
    }
}
