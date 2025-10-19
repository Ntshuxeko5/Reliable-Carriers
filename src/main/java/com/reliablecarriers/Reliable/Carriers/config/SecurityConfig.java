package com.reliablecarriers.Reliable.Carriers.config;

import com.reliablecarriers.Reliable.Carriers.security.JwtAuthenticationFilter;
import com.reliablecarriers.Reliable.Carriers.security.RateLimitFilter;
import com.reliablecarriers.Reliable.Carriers.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    // @Autowired
    // private RateLimitFilter rateLimitFilter;

    @Autowired
    @Lazy
    private com.reliablecarriers.Reliable.Carriers.config.OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public site pages and static assets
                .requestMatchers("/", "/home", "/about", "/contact", "/services", "/quote", "/track", "/tracking/**").permitAll()
                .requestMatchers("/login", "/register", "/staff-login", "/create-test-users").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()

                // Open API endpoints
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/api/customer/quote").permitAll()  // Allow guests to create quotes
                .requestMatchers("/api/customer/quote/*/create-shipment").permitAll()  // Allow guests to create shipments from quotes
                .requestMatchers("/api/customer/track/**").permitAll()  // Allow guests to track packages
                .requestMatchers("/api/customer/packages/email/**").permitAll()  // Allow guests to lookup packages by email
                .requestMatchers("/api/customer/packages/phone/**").permitAll()  // Allow guests to lookup packages by phone

                // Secured API endpoints by role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/staff/**").hasRole("STAFF")
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                .requestMatchers("/api/tracking/**").hasRole("TRACKING_MANAGER")
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                // Shipment assignment allowed for admins and tracking managers only
                .requestMatchers("/api/shipments/*/assign-driver/*").hasAnyRole("ADMIN", "TRACKING_MANAGER")

                // All other non-API requests are server-rendered pages; allow
                .requestMatchers("/**").permitAll()
            )
            // .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Use our custom login page for form login and enable OAuth2 login for social providers
        http.formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        );

        http.oauth2Login(oauth -> oauth
            .loginPage("/login")
            .successHandler(oauth2LoginSuccessHandler)
        );

        // Configure logout
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .addLogoutHandler(customLogoutHandler)
            .permitAll()
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