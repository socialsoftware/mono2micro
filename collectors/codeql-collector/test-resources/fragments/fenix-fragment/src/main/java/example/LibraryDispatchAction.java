package example;

import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public class LibraryDispatchAction extends FenixDispatchAction {

    protected Set<Author> getAuthors(HttpServletRequest request) {
        return getAuthorsSet();
    }

    private Set<Author> getAuthorsSet() {
        DomainRoot root = FenixFramework.getDomainRoot();
        return root.getAuthorsSet();
    }

}
