package pt.ist.socialsoftware.mono2micro.services;

import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

@Service
public class DendrogramService {

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

    public Dendrogram createDendrogramByFeatures(
            String codebaseName,
            Dendrogram dendrogram
    ) {
        try {
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            codebase.createDendrogramByFeatures(dendrogram, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dendrogram;
    }
}
