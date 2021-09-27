package com.redhat.naps.process;

import javax.annotation.PostConstruct;

import com.redhat.naps.process.commands.GetObservationsSignalEventCommand;
import com.redhat.naps.process.util.FHIRUtil;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.HttpMessageConverter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


@Configuration("kieServerSecurity")
@EnableWebSecurity
public class KeycloakWebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    private final static Logger log = LoggerFactory.getLogger(KeycloakWebSecurityConfig.class);

    @Value("${com.redhat.naps.sso.available.groups}")
    private String availableGroups;

    @Value("${fhir.server.url}")
    private String fhirUrl;

    @PostConstruct
    public void init() {
        // System property gets picked up by this project's KeycloakUserGroupCallback
        System.setProperty(FHIRUtil.AVAILABLE_SSO_GROUPS, availableGroups);

        // System property gets picked up by this project's GetObservationsSignalEventCommand
        System.setProperty(FHIRUtil.FHIR_SERVER_URL, fhirUrl);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
        .cors().and()
        .csrf().disable()
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();

        // Change the Granted Authority Mapper which by default in Spring Security, roles are prefixed with ROLE.
        // This could be changed in our Realm configuration but it could be confusing for other applications that do not know this convention
        // So here we assign a SimpleAuthorityMapper that will make sure no prefix is added
        mapper.setPrefix("");
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(mapper);

        log.info("configureGlobal() setting the following provider = "+keycloakAuthenticationProvider);

        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    public KeycloakConfigResolver KeycloakConfigResolver() {
       return new KeycloakSpringBootConfigResolver();
    }

    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /*
      NOTE:  Even when using keycloak, these CORS related springboot configs are still needed .
             There doesn't appear to be an equivalent way to set CORS via "enable-cors=true" property in a keycloak.json
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.HEAD.name(),
                                                          HttpMethod.POST.name(), HttpMethod.DELETE.name(),
                                                          HttpMethod.PUT.name(), HttpMethod.OPTIONS.name()));
        corsConfiguration.applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean(name = "userGroupCallback")
    public UserGroupCallback userGroupCallback(IdentityProvider provider) {
        return new KeycloakUserGroupCallback(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    RestTemplate getRestTemplate() {
        RestTemplate restTemplate = null;
        try {
            TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            restTemplate = new RestTemplate(requestFactory);
            List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
            messageConverters.add(mappingJackson2HttpMessageConverter());
        }catch(Exception e) {
            e.printStackTrace();
        }
        return restTemplate;
    }


    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converter.setObjectMapper(objectMapper);
        MediaType fhir = new MediaType("application", "fhir+json");
        List<MediaType> jacksonTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        jacksonTypes.add(fhir);
        converter.setSupportedMediaTypes(jacksonTypes);

        return converter;
    }
}
