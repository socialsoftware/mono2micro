package pt.ulisboa.tecnico.socialsoftware.tutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@org.springframework.context.annotation.PropertySource({ "classpath:application.properties" })
@org.springframework.data.jpa.repository.config.EnableJpaRepositories
@org.springframework.transaction.annotation.EnableTransactionManagement
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
@org.springframework.boot.autoconfigure.SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class TutorApplication extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer implements org.springframework.beans.factory.InitializingBean {
    public static void main(java.lang.String[] args) {
        org.springframework.boot.SpringApplication.run(pt.ulisboa.tecnico.socialsoftware.tutor.TutorApplication.class, args);
    }

    @java.lang.Override
    public void afterPropertiesSet() throws java.lang.Exception {
        // Run on startup
    }
}