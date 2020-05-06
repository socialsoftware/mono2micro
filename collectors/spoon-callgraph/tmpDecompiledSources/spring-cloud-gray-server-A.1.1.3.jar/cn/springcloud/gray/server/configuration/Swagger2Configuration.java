/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicate
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.ComponentScan
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
 *  org.springframework.web.servlet.config.annotation.ViewControllerRegistry
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 *  springfox.documentation.builders.ApiInfoBuilder
 *  springfox.documentation.builders.PathSelectors
 *  springfox.documentation.builders.ResponseMessageBuilder
 *  springfox.documentation.schema.ModelRef
 *  springfox.documentation.schema.ModelReference
 *  springfox.documentation.service.ApiInfo
 *  springfox.documentation.service.ApiKey
 *  springfox.documentation.service.AuthorizationScope
 *  springfox.documentation.service.ResponseMessage
 *  springfox.documentation.service.SecurityReference
 *  springfox.documentation.spi.DocumentationType
 *  springfox.documentation.spi.service.contexts.SecurityContext
 *  springfox.documentation.spi.service.contexts.SecurityContextBuilder
 *  springfox.documentation.spring.web.plugins.ApiSelectorBuilder
 *  springfox.documentation.spring.web.plugins.Docket
 *  springfox.documentation.swagger2.annotations.EnableSwagger2
 */
package cn.springcloud.gray.server.configuration;

import cn.springcloud.gray.server.configuration.apidoc.PageableParameterAlternateTypeRuleConvention;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spi.service.contexts.SecurityContextBuilder;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan(value={"cn.springcloud.gray.server.resources"})
public class Swagger2Configuration
extends WebMvcConfigurerAdapter {
    @Bean
    public PageableParameterAlternateTypeRuleConvention pageableParameterAlternateTypeRuleConvention(TypeResolver resolver) {
        return new PageableParameterAlternateTypeRuleConvention(resolver);
    }

    public void addViewControllers(ViewControllerRegistry registry) {
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(this.apiInfo()).forCodeGeneration(true).genericModelSubstitutes(new Class[]{ResponseEntity.class}).select().paths(PathSelectors.ant((String)"/gray/**")).build().directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(new Class[]{ResponseEntity.class}).securitySchemes(this.apiKeys()).securityContexts(Arrays.asList(new SecurityContext[]{this.securityContext()}));
        return docket;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("spring cloud gray server\u63a5\u53e3\u5217\u8868").description("\u76f8\u5173\u4fe1\u606f\u8bf7\u5173\u6ce8\uff1ahttps://github.com/SpringCloud/spring-cloud-gray").termsOfServiceUrl("https://github.com/SpringCloud/spring-cloud-gray").build();
    }

    private List<ApiKey> apiKeys() {
        return Arrays.asList(new ApiKey[]{new ApiKey("accessToken", "accessToken", "header"), new ApiKey("Authorization", "authorization", "header")});
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(this.defaultAuth()).forPaths(PathSelectors.regex((String)"^(?!auth).*$")).build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return Arrays.asList(new SecurityReference[]{new SecurityReference("accessToken", authorizationScopes), new SecurityReference("Authorization", authorizationScopes)});
    }

    private List<ResponseMessage> customerResponseMessage() {
        return Arrays.asList(new ResponseMessage[]{new ResponseMessageBuilder().code(500).message("").responseModel((ModelReference)new ModelRef("Error")).build(), new ResponseMessageBuilder().code(401).message("").build()});
    }
}

