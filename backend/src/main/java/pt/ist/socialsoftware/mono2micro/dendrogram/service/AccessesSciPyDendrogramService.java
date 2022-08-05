package pt.ist.socialsoftware.mono2micro.dendrogram.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.AccessesSciPyDendrogramDto;
import pt.ist.socialsoftware.mono2micro.dendrogram.repository.AccessesSciPyDendrogramRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.AccessesSciPyStrategyRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AccessesSciPyDendrogramService {
    @Autowired
    AccessesSciPyDendrogramRepository dendrogramRepository;

    @Autowired
    AccessesSciPyStrategyRepository strategyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

    @Autowired
    GridFsService gridFsService;

    public void createDendrogram(AccessesSciPyDendrogramDto dendrogramDto) throws Exception {
        AccessesSciPyStrategy strategy = strategyRepository.findByName(dendrogramDto.getStrategyName());
        if (strategy.getDendrograms().stream().anyMatch(dendrogram -> dendrogram.equalsDto(dendrogramDto)))
            return;

        AccessesSciPyDendrogram dendrogram = getNewAccessesSciPyDendrogramForStrategy(strategy, dendrogramDto);
        similarityGenerator.createSimilarityMatrixForSciPy(dendrogram);
        clusteringAlgorithm.createAccessesSciPyDendrogram(dendrogram);
        strategyRepository.save(strategy);
        dendrogramRepository.save(dendrogram);
    }

    public AccessesSciPyDendrogram getNewAccessesSciPyDendrogramForStrategy(AccessesSciPyStrategy strategy, AccessesSciPyDendrogramDto dendrogramDto) {
        AccessesSciPyDendrogram dendrogram = new AccessesSciPyDendrogram(dendrogramDto);
        int i = 0;
        String dendrogramName;
        do {
            dendrogramName = dendrogramDto.getStrategyName() + " D" + ++i;
        } while (dendrogramRepository.existsByName(dendrogramName));
        dendrogram.setName(dendrogramName);
        dendrogram.setStrategy(strategy);
        strategy.addDendrogram(dendrogram);
        return dendrogram;
    }

    public byte[] getDendrogramImage(String dendrogramName) throws IOException {
        AccessesSciPyDendrogram dendrogram = dendrogramRepository.getDendrogramImage(dendrogramName);
        InputStream inputStream = gridFsService.getFile(dendrogram.getImageName());
        return IOUtils.toByteArray(inputStream);
    }

    public void saveSimilarityMatrix(InputStream matrix, String similarityMatrixName) {
        gridFsService.saveFile(matrix, similarityMatrixName);
    }

    public void deleteDendrogramProperties(AccessesSciPyDendrogram dendrogram) { // Used to delete fields that can't otherwise be accessed by abstract Dendrogram
        AccessesSciPyStrategy strategy = strategyRepository.findByName(dendrogram.getStrategy().getName());
        strategy.removeDendrogram(dendrogram.getName());
        strategyRepository.save(strategy);
        gridFsService.deleteFile(dendrogram.getSimilarityMatrixName());
        gridFsService.deleteFile(dendrogram.getImageName());
        gridFsService.deleteFile(dendrogram.getCopheneticDistanceName());
    }
}
