package pt.ulisboa.tecnico.socialsoftware.tutor.auth;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.sdk.ApplicationConfiguration;
import org.fenixedu.sdk.FenixEduClientImpl;
import org.fenixedu.sdk.FenixEduUserDetails;
import org.fenixedu.sdk.exception.FenixEduClientException;
public class FenixEduInterface {
    private org.fenixedu.sdk.FenixEduClientImpl client;

    private org.fenixedu.sdk.FenixEduUserDetails userDetails;

    private com.google.gson.JsonObject person;

    private com.google.gson.JsonObject courses;

    public FenixEduInterface(java.lang.String baseUrl, java.lang.String oauthConsumerId, java.lang.String oauthConsumerSecret, java.lang.String callbackUrl) {
        org.fenixedu.sdk.ApplicationConfiguration config = new org.fenixedu.sdk.ApplicationConfiguration(baseUrl, oauthConsumerId, oauthConsumerSecret, callbackUrl);
        try {
            client = new org.fenixedu.sdk.FenixEduClientImpl(config);
        } catch (org.fenixedu.sdk.exception.FenixEduClientException e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.FENIX_CONFIGURATION_ERROR);
        }
    }

    public void authenticate(java.lang.String code) {
        try {
            userDetails = client.getUserDetailsFromCode(code);
        } catch (java.lang.Exception e) {
            throw new pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException(pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.FENIX_ERROR);
        }
        person = client.getPerson(userDetails.getAuthorization());
    }

    public java.lang.String getPersonName() {
        return java.lang.String.valueOf(person.get("name")).replaceAll("^\"|\"$", "");
    }

    public java.lang.String getPersonUsername() {
        return java.lang.String.valueOf(person.get("username")).replaceAll("^\"|\"$", "");
    }

    private com.google.gson.JsonObject getPersonCourses() {
        java.lang.String academicTerm = client.getAbout().get("currentAcademicTerm").getAsString();
        java.util.regex.Matcher currentYearMatcher = java.util.regex.Pattern.compile("([0-9]+/[0-9]+)").matcher(academicTerm);
        currentYearMatcher.find();
        java.lang.String currentYear = currentYearMatcher.group(1);
        return client.getPersonCourses(userDetails.getAuthorization(), currentYear);
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getPersonAttendingCourses() {
        if (courses == null) {
            courses = getPersonCourses();
        }
        return getCourses(courses.get("attending").getAsJsonArray());
    }

    public java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getPersonTeachingCourses() {
        if (courses == null) {
            courses = getPersonCourses();
        }
        return getCourses(courses.get("teaching").getAsJsonArray());
    }

    private java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> getCourses(com.google.gson.JsonArray coursesJson) {
        java.util.List<pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto> result = new java.util.ArrayList<>();
        for (com.google.gson.JsonElement courseJson : coursesJson) {
            result.add(new pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto(courseJson.getAsJsonObject().get("name").getAsString(), courseJson.getAsJsonObject().get("acronym").getAsString(), courseJson.getAsJsonObject().get("academicTerm").getAsString()));
        }
        return result;
    }
}