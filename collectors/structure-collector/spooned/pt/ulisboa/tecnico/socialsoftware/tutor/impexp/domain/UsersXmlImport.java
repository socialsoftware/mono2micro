package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.stereotype.Component;
@org.springframework.stereotype.Component
public class UsersXmlImport {
    private pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService userService;

    public void importUsers(java.io.InputStream inputStream, pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService userService) {
        this.userService = userService;
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        org.jdom2.Document doc;
        try {
            java.io.Reader reader = new java.io.InputStreamReader(inputStream, java.nio.charset.Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (java.io.FileNotFoundException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERS_IMPORT_ERROR, "File not found");
        } catch (org.jdom2.JDOMException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERS_IMPORT_ERROR, "Coding problem");
        } catch (java.io.IOException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERS_IMPORT_ERROR, "File type or format");
        }
        if (doc == null) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERS_IMPORT_ERROR, "File not found ot format error");
        }
        importUsers(doc);
    }

    public void importUsers(java.lang.String usersXml, pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService userService) {
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        java.io.InputStream stream = new java.io.ByteArrayInputStream(usersXml.getBytes());
        importUsers(stream, userService);
    }

    private void importUsers(org.jdom2.Document doc) {
        org.jdom2.xpath.XPathFactory xpfac = org.jdom2.xpath.XPathFactory.instance();
        org.jdom2.xpath.XPathExpression<org.jdom2.Element> xp = xpfac.compile("//users/user", org.jdom2.filter.Filters.element());
        for (org.jdom2.Element element : xp.evaluate(doc)) {
            java.lang.Integer key = java.lang.Integer.valueOf(element.getAttributeValue("key"));
            if (userService.findByKey(key) == null) {
                java.lang.String name = element.getAttributeValue("name");
                java.lang.String username = element.getAttributeValue("username");
                pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role = null;
                if (element.getAttributeValue("role") != null) {
                    role = pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role.valueOf(element.getAttributeValue("role"));
                }
                pt.ulisboa.tecnico.socialsoftware.tutor.user.User user = userService.createUser(name, username, role);
                user.setKey(key);
                importCourseExecutions(element.getChild("courseExecutions"), user);
            }
        }
    }

    private void importCourseExecutions(org.jdom2.Element courseExecutions, pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        for (org.jdom2.Element courseExecutionElement : courseExecutions.getChildren("courseExecution")) {
            java.lang.Integer executionId = java.lang.Integer.valueOf(courseExecutionElement.getAttributeValue("executionId"));
            userService.addCourseExecution(user.getUsername(), executionId);
        }
    }
}