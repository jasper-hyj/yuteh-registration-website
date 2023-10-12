package com.yuteh.register.security;

import com.yuteh.register.security.security.OAuth2ProviderProperties;
import com.yuteh.register.security.security.OAuth2RegistrationProperties;
import com.yuteh.register.security.security.SecurityProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


// Security configuration for spring security
@Configuration
// Enable the spring security
@EnableWebSecurity
// Default method for spring security
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final SecurityProperties securityProperties;
    private final OAuth2ProviderProperties providerProperties;
    private final OAuth2RegistrationProperties registrationProperties;
    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityProperties securityProperties,
                          OAuth2ProviderProperties providerProperties,
                          OAuth2RegistrationProperties registrationProperties,
                          SecurityFilter securityFilter) {
        this.securityProperties = securityProperties;
        this.providerProperties = providerProperties;
        this.registrationProperties = registrationProperties;
        this.securityFilter = securityFilter;
    }

    /**
     * Set up the configuration source for security properties
     *
     * @return configuration source
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getAllowedOrigins());
        configuration.setAllowedMethods(securityProperties.getAllowedMethods());
        configuration.setAllowedHeaders(securityProperties.getAllowedHeaders());
        configuration.setAllowCredentials(securityProperties.isAllowCredentials());
        configuration.setExposedHeaders(securityProperties.getExposedHeaders());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(getRegistration());
    }

    /**
     * Get provide of oauth2 according to the client request
     *
     * @return commonOAuth2Provider
     */
    private ClientRegistration getRegistration() {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(registrationProperties.getClientId())
                .clientSecret(registrationProperties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(registrationProperties.getScope().split(", "))
                .authorizationUri(providerProperties.getAuthorizationUri())
                .tokenUri(providerProperties.getTokenUri())
                .userInfoUri(providerProperties.getUserInfoUri())
                .redirectUri(registrationProperties.getRedirectUriTemplate())
                .userNameAttributeName(providerProperties.getUserNameAttribute())
                .jwkSetUri(providerProperties.getJwkSetUri()).clientName(registrationProperties.getClientName())
                .build();
    }

    /**
     * Configuration of spring security
     *
     * @param http http security
     * @throws Exception any exception
     */
    @Override
    protected void configure(@NotNull HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();
        http
                /* Set up corsConfigurationSource for browser request */
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                /* Set up authorize request */
                .authorizeRequests()
                /* Set allow apis */
                .antMatchers(securityProperties.getAllowedPublicApis().toArray(String[]::new)).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                /* authenticate all other request */
                .anyRequest().authenticated();


        http
                /* Set up authentication filter for custom use attribute */
                .addFilterAfter(securityFilter, UsernamePasswordAuthenticationFilter.class)
                /* Set logout process and url */
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .and()
                /* Set oauth2 login setting */
                .oauth2Login()
                .defaultSuccessUrl("/admin")
                .loginPage("/oauth2/authorization/google");
    }
}
