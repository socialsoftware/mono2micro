package pt.ist.socialsoftware.mono2micro.source.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.domain.SourceFactory;
import pt.ist.socialsoftware.mono2micro.source.repository.SourceRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SourceService {

    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    GridFsService gridFsService;

    public void addSources(String codebaseName, List<String> sourceTypes, List<Object> sources) throws Exception {

        if (sourceTypes.size() != sources.size())
            throw new RuntimeException("Number of sources is different from the number of source types.");

        Codebase codebase = codebaseRepository.findByName(codebaseName);
        List<String> availableSourceTypes = codebase.getSources().stream().map(Source::getType).collect(Collectors.toList());

        for (String sourceType : sourceTypes)
            if (availableSourceTypes.contains(sourceType))
                throw new RuntimeException("Re-sending sources is not allowed.");

        for(int i = 0; i < sourceTypes.size(); i++) {
            String sourceType = sourceTypes.get(i);
            byte[] sourceFileStream = ((MultipartFile) sources.get(i)).getBytes();
            Source source = SourceFactory.getFactory().getSource(sourceType);
            String fileName = source.init(codebase, sourceFileStream);
            codebase.addSource(source);
            gridFsService.saveFile(new ByteArrayInputStream(sourceFileStream), fileName);
            sourceRepository.save(source);
        }
        codebaseRepository.save(codebase);
    }

    public Source getCodebaseSource(String codebaseName, String sourceType) {
        Codebase codebase = codebaseRepository.findByName(codebaseName);
        return codebase.getSources().stream().filter(source -> source.getType().equals(sourceType)).findFirst()
                .orElseThrow(() -> new RuntimeException("No source with type" + sourceType));
    }

    public Source getSource(String sourceId) {
        return sourceRepository.findById(sourceId).orElseThrow(() -> new RuntimeException("No source " + sourceId + " found."));
    }

    public InputStream getSourceFileAsInputStream(String sourceName) throws IOException {
        return gridFsService.getFile(sourceName);
    }

    public void deleteSource(String sourceId) {
        Source source = sourceRepository.findById(sourceId).orElseThrow(() -> new RuntimeException("No source with id " + sourceId));
        Codebase codebase = source.getCodebase();
        codebase.removeSource(sourceId);
        gridFsService.deleteFile(source.getName());
        codebaseRepository.save(codebase);
        sourceRepository.deleteById(sourceId);
    }
}