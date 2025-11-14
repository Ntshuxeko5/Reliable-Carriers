package com.reliablecarriers.Reliable.Carriers.config;

import com.reliablecarriers.Reliable.Carriers.security.JwtAuthenticationFilter;
import com.reliablecarriers.Reliable.Carriers.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    
    @Value("${production.mode:false}")
    private boolean productionMode;

    // @Autowired
    // private RateLimitFilter rateLimitFilter;

    @Autowired
    @Lazy
    private com.reliablecarriers.Reliable.Carriers.config.OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Autowired
    private SessionAuthenticationFilter sessionAuthenticationFilter;
    
    @Autowired(required = false)
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Autowired(required = false)
    private RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Allow sessions for web pages, but use stateless for API requests (handled via JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?timeout=true")
            )
            // Enable CSRF protection (important for production)
            // APIs use JWT/API keys, so exempt from CSRF
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/ws/**") 
                .csrfTokenRepository(new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository())
            )
            .authorizeHttpRequests(authz -> authz
                // Public site pages and static assets
                .requestMatchers("/", "/home", "/about", "/contact", "/services", "/quote", "/track", "/tracking/**").permitAll()
                .requestMatchers("/login", "/register", "/staff-login").permitAll()
                .requestMatchers("/help-center", "/docs/**").permitAll() // Help center and documentation
                // Test user creation endpoint - restricted to development mode only
                .requestMatchers("/create-test-users").access((authenticationSupplier, object) -> {
                    return new AuthorizationDecision(!productionMode);
                })
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()

                // Open API endpoints
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/api/customer/quote").permitAll()  // Allow guests to create quotes
                .requestMatchers("/api/customer/quote/*/create-shipment").permitAll()  // Allow guests to create shipments from quotes
                .requestMatchers("/api/customer/track/**").permitAll()  // Allow guests to track packages
                .requestMatchers("/api/customer/public/track/**").permitAll()  // Allow guests to track packages via public endpoint
                .requestMatchers("/api/customer/packages/email/**").permitAll()  // Allow guests to lookup packages by email
                .requestMatchers("/api/customer/packages/phone/**").permitAll()  // Allow guests to lookup packages by phone

                // Business API endpoints (authenticated via API key)
                .requestMatchers("/api/business/**").permitAll() // API key filter will handle authentication
                
                // Secured API endpoints by role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/staff/**").hasRole("STAFF")
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                .requestMatchers("/api/tracking/**").hasRole("TRACKING_MANAGER")
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                // Shipment assignment allowed for admins and tracking managers only
                .requestMatchers("/api/shipments/*/assign-driver/*").hasAnyRole("ADMIN", "TRACKING_MANAGER")

                // Actuator endpoints - authenticated access only (restricted in production)
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // Swagger/OpenAPI - restrict to ADMIN in production, allow all in development
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html")
                    .access((authenticationSupplier, object) -> {
                        if (!productionMode) {
                            // Allow all in development mode
                            return new AuthorizationDecision(true);
                        } else {
                            // Require ADMIN role in production
                            try {
                                org.springframework.security.core.Authentication auth = authenticationSupplier.get();
                                if (auth == null || !auth.isAuthenticated()) {
                                    return new AuthorizationDecision(false);
                                }
                                boolean hasAdminRole = auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                                return new AuthorizationDecision(hasAdminRole);
                            } catch (Exception e) {
                                return new AuthorizationDecision(false);
                            }
                        }
                    })
                
                // All other non-API requests are server-rendered pages; allow
                .requestMatchers("/**").permitAll()
            )
            .addFilterBefore(rateLimitFilter != null ? rateLimitFilter : new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class);
        
        // Only add API key filter if it's available (not in tests)
        if (apiKeyAuthenticationFilter != null) {
            http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        http.addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Use our custom login page for form login and enable OAuth2 login for social providers
        http.formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        );

        // OAuth2 configuration for Google and Facebook login (only if handler is available)
        if (oauth2LoginSuccessHandler != null) {
            http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(oauth2LoginSuccessHandler)
                .permitAll()
            );
        }

        // Configure logout
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .addLogoutHandler(customLogoutHandler)
            .permitAll()
        );

        // Security headers
        http.headers(headers -> headers
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            .contentTypeOptions(contentType -> contentType.disable())
            .httpStrictTransportSecurity(hsts -> hsts
                .maxAgeInSeconds(31536000)
            )
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
            // Note: permissionsPolicy() is deprecated in Spring Security 6.x
            // If needed, use a custom header writer instead
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}