package com.lig.chatty.config;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.collect.Lists;
import com.lig.chatty.controller.adapter.anonymousui.dto.AuthResponseDto;
import org.springframework.core.Ordered;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;

import java.lang.reflect.Type;
import java.util.List;

import static com.lig.chatty.controller.adapter.anonymousui.AuthController.FORM_AUTH_REQUEST_MAPPING;
import static com.lig.chatty.controller.adapter.anonymousui.AuthController.PUBLIC_API_ANT_MATCHER;
import static com.lig.chatty.security.jwt.TokenAuthenticationFilter.AUTHORIZATION_HEADER;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import springfox.documentation.builders.AlternateTypeBuilder;
import springfox.documentation.builders.AlternateTypePropertyBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private static final String USER_API_ANT_MATCHER = "/api/**";

    @Bean
    public Docket publicApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant(PUBLIC_API_ANT_MATCHER))
                .build()
                //.enableUrlTemplating(true)
                .groupName("publicApi");
    }

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant(USER_API_ANT_MATCHER))
                .build()
                //.enableUrlTemplating(true)
                .groupName("userApi")
                .securityContexts(Lists.newArrayList(securityContext()))
                .securitySchemes(Lists.newArrayList(apiKey()));
    }

    private ApiKey apiKey() {
        return new ApiKey("JWT = " + "\"Bearer \" + " + FORM_AUTH_REQUEST_MAPPING + " response." + AuthResponseDto.Fields.accessToken, AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.ant(USER_API_ANT_MATCHER))
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Lists.newArrayList(
                new SecurityReference("JWT = " + "\"Bearer \" + " + FORM_AUTH_REQUEST_MAPPING + " response." + AuthResponseDto.Fields.accessToken, authorizationScopes));
    }

    @Bean
    public AlternateTypeRuleConvention pageableConvention(
            final TypeResolver resolver) {
        return new AlternateTypeRuleConvention() {

            @Override
            public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
            }

            @Override
            public List<AlternateTypeRule> rules() {
                return Arrays.asList(
                        AlternateTypeRules.newRule(resolver.resolve(Pageable.class), resolver.resolve(pageableMixin()))
                );
            }
        };
    }

    private Type pageableMixin() {
        return new AlternateTypeBuilder()
                .fullyQualifiedClassName(
                        String.format("%s.generated.%s",
                                Pageable.class.getPackage().getName(),
                                Pageable.class.getSimpleName()))
                .withProperties(Arrays.asList(
                        property(Integer.class, "page"),
                        property(Integer.class, "size")
                ))
                .build();
    }

    private AlternateTypePropertyBuilder property(Class<?> type, String name) {
        return new AlternateTypePropertyBuilder()
                .withName(name)
                .withType(type)
                .withCanRead(true)
                .withCanWrite(true);
    }
}