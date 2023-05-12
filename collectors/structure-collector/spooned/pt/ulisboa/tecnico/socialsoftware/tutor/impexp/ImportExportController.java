package pt.ulisboa.tecnico.socialsoftware.tutor.impexp;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
@org.springframework.stereotype.Controller
@org.springframework.security.access.annotation.Secured({ "ROLE_ADMIN" })
public class ImportExportController {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImportExportController.class);

    @org.springframework.beans.factory.annotation.Autowired
    private pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImpExpService impExpService;

    @org.springframework.beans.factory.annotation.Value("${export.dir}")
    private java.lang.String exportDir;

    @org.springframework.web.bind.annotation.GetMapping("/admin/export")
    public org.springframework.http.ResponseEntity exportAll(javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImportExportController.logger.debug("exportAll");
        java.lang.String filename = impExpService.exportAll();
        java.io.File directory = new java.io.File(exportDir);
        java.io.File file = new java.io.File(directory, filename);
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setHeader("Content-Type", "application/zip");
        java.io.InputStream is = new java.io.FileInputStream(file);
        org.springframework.util.FileCopyUtils.copy(org.apache.commons.io.IOUtils.toByteArray(is), response.getOutputStream());
        response.flushBuffer();
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/admin/import")
    public org.springframework.http.ResponseEntity importAll() {
        pt.ulisboa.tecnico.socialsoftware.tutor.impexp.ImportExportController.logger.debug("importAll");
        impExpService.importAll();
        return org.springframework.http.ResponseEntity.ok().build();
    }
}