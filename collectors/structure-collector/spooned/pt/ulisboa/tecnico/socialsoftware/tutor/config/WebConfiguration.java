package pt.ulisboa.tecnico.socialsoftware.tutor.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
@org.springframework.context.annotation.Configuration
public class WebConfiguration extends org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport {
    @org.springframework.beans.factory.annotation.Value("${figures.dir}")
    private java.lang.String figuresDir;

    @java.lang.Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/questions/**").addResourceLocations("file:" + figuresDir);
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    private static final long MAX_AGE_SECS = 3600;

    @java.lang.Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE").maxAge(pt.ulisboa.tecnico.socialsoftware.tutor.config.WebConfiguration.MAX_AGE_SECS);
    }
}