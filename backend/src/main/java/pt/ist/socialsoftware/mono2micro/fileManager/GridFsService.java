package pt.ist.socialsoftware.mono2micro.fileManager;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Since imported files might be very large, GridFS is used to store them persistently
 * The file will be divided into chunks
 */
@Service
public class GridFsService {
    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations operations;

    public void saveFile(InputStream fileStream, String fileName) {
        gridFsTemplate.store(fileStream, fileName);
    }

    public InputStream getFile(String fileName) throws IOException {
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(fileName)));
        if (gridFSFile == null)
            throw new RuntimeException("No file called: " + fileName);
        return operations.getResource(gridFSFile).getInputStream();
    }

    public void replaceFile(InputStream fileStream, String fileName) {
        gridFsTemplate.delete(new Query(Criteria.where("filename").is(fileName)));
        gridFsTemplate.store(fileStream, fileName);
    }

    public void deleteFile(String fileName) {
        gridFsTemplate.delete(new Query(Criteria.where("filename").is(fileName)));
    }

    public void deleteFiles(Set<String> fileNames) {
        gridFsTemplate.delete(new Query(Criteria.where("filename").in(fileNames)));
    }
}
