package net.aroder.TripTracker.security;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Configuration class for Spring Security.
 */
@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


    private final String allowedOrigins;

    public SecurityConfig(final @Value("${security.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }


    /**
     * Configures the security filter chain for HTTP requests.
     * Sets up security rules and policies for the application.
     *
     * @param http the HttpSecurity object used to configure the security filter chain
     * @return the configured security filter chain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors-> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf->csrf.disable())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Specify paths where public access is allowed
                        .requestMatchers("/api/v1/authenticate").permitAll()
                        .requestMatchers("/api/v1/authenticate/refresh").permitAll()
                        .requestMatchers("/api/v1/authenticate/forgot-password").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer((oauth2)-> oauth2.jwt((jwt)-> jwt.jwtAuthenticationConverter(jwtRoleAuthenticationConverter())));
        return http.build();
    }

    /**
     * Configures the JWT authentication converter.
     * Converts JWT claims to authorities.
     *
     * @return the configured JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtRoleAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Use roles claim as authorities

        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        // Add the ROLE_ prefix - for hasRole
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * Creates a RestTemplate bean.
     *
     * @param builder the RestTemplateBuilder used to build the RestTemplate
     * @return the RestTemplate bean
     */
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * Configures the CORS (Cross-Origin Resource Sharing) configuration.
     *
     * @return the configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOriginsList = Arrays.stream(allowedOrigins.split(";")).map(origin -> origin.replace(";","")).toList();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(allowedOriginsList);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("authorization", "withCredentials", "content-type",
                "x-auth-token", "Access-Control-Allow-Credentials", "access-control-allow-origin",
                "Access-Control-Allow-headers"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        configuration.setMaxAge(Duration.ofSeconds(5000));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
