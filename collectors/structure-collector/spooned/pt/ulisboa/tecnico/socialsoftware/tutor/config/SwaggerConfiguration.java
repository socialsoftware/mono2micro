package pt.ulisboa.tecnico.socialsoftware.tutor.config;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
@org.springframework.context.annotation.Configuration
@springfox.documentation.swagger2.annotations.EnableSwagger2
class SwaggerConfiguration {
    public static final java.lang.String AUTHORIZATION_HEADER = "Authorization";

    @org.springframework.context.annotation.Bean
    public springfox.documentation.spring.web.plugins.Docket swaggerSpringfoxDocket() {
        return new springfox.documentation.spring.web.plugins.Docket(springfox.documentation.spi.DocumentationType.SWAGGER_2).forCodeGeneration(true).genericModelSubstitutes(org.springframework.http.ResponseEntity.class).ignoredParameterTypes(org.springframework.data.domain.Pageable.class).ignoredParameterTypes(java.sql.Date.class).directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class).directModelSubstitute(java.time.ZonedDateTime.class, java.util.Date.class).directModelSubstitute(java.time.LocalDateTime.class, java.util.Date.class).securityContexts(com.google.common.collect.Lists.newArrayList(securityContext())).securitySchemes(com.google.common.collect.Lists.newArrayList(apiKey())).useDefaultResponseMessages(false);
    }

    private springfox.documentation.service.ApiKey apiKey() {
        return new springfox.documentation.service.ApiKey("JWT", pt.ulisboa.tecnico.socialsoftware.tutor.config.SwaggerConfiguration.AUTHORIZATION_HEADER, "header");
    }

    private springfox.documentation.spi.service.contexts.SecurityContext securityContext() {
        return springfox.documentation.spi.service.contexts.SecurityContext.builder().securityReferences(defaultAuth()).build();
    }

    java.util.List<springfox.documentation.service.SecurityReference> defaultAuth() {
        springfox.documentation.service.AuthorizationScope authorizationScope = new springfox.documentation.service.AuthorizationScope("global", "accessEverything");
        springfox.documentation.service.AuthorizationScope[] authorizationScopes = new springfox.documentation.service.AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return com.google.common.collect.Lists.newArrayList(new springfox.documentation.service.SecurityReference("JWT", authorizationScopes));
    }
}