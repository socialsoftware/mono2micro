package example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

import java.util.Set;

@RestController
public class LibraryController {

    @RequestMapping(value = "/authors", method = RequestMethod.GET)
    public Set<Author> getAllAuthors() {
        DomainRoot root = FenixFramework.getDomainRoot();
        return root.getAuthorsSet();
    }

}
