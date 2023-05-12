package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
public class UsersXmlExport {
    public java.lang.String export(java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.user.User> users) {
        org.jdom2.Element element = createHeader();
        exportUsers(element, users);
        org.jdom2.output.XMLOutputter xml = new org.jdom2.output.XMLOutputter();
        xml.setFormat(org.jdom2.output.Format.getPrettyFormat());
        return xml.outputString(element);
    }

    public org.jdom2.Element createHeader() {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        org.jdom2.Element rootElement = new org.jdom2.Element("users");
        jdomDoc.setRootElement(rootElement);
        return rootElement;
    }

    private void exportUsers(org.jdom2.Element element, java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.user.User> users) {
        for (pt.ulisboa.tecnico.socialsoftware.tutor.user.User user : users) {
            exportUser(element, user);
        }
    }

    private void exportUser(org.jdom2.Element element, pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        org.jdom2.Element userElement = new org.jdom2.Element("user");
        userElement.setAttribute("key", java.lang.String.valueOf(user.getKey()));
        if (user.getUsername() != null) {
            userElement.setAttribute("username", user.getUsername());
        }
        userElement.setAttribute("name", user.getName());
        if (user.getRole() != null) {
            userElement.setAttribute("role", user.getRole().name());
        }
        exportUserCourseExecutions(userElement, user.getCourseExecutions());
        element.addContent(userElement);
    }

    private void exportUserCourseExecutions(org.jdom2.Element userElement, java.util.Set<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution> courseExecutions) {
        org.jdom2.Element courseExecutionsElement = new org.jdom2.Element("courseExecutions");
        for (pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution courseExecution : courseExecutions) {
            org.jdom2.Element courseExecutionElement = new org.jdom2.Element("courseExecution");
            courseExecutionElement.setAttribute("executionId", courseExecution.getId().toString());
            courseExecutionsElement.addContent(courseExecutionElement);
        }
        userElement.addContent(courseExecutionsElement);
    }
}