package pt.ist.socialsoftware.mono2micro.source.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.repository.AccessesSourceRepository;

import java.util.HashSet;

@Service
public class AccessesSourceService {
    @Autowired
    AccessesSourceRepository accessesSourceRepository;

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
}
