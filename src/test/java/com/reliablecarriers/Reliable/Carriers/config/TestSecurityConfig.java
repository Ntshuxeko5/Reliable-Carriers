package com.reliablecarriers.Reliable.Carriers.config;

import com.reliablecarriers.Reliable.Carriers.service.ApiKeyService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@TestPropertySource(properties = {
    "sms.api.key=test-sms-api-key",
    "sms.api.secret=test-sms-api-secret",
    "sms.from.number=+1234567890"
})
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        @SuppressWarnings("deprecation")
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
    
    @Bean
    @Primary
    public JwtTokenUtil testJwtTokenUtil() {
        return new JwtTokenUtil();
    }
    
    @Bean
    @Primary
    public JavaMailSender testJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(1025); // Test port
        mailSender.setUsername("test");
        mailSender.setPassword("test");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
    
    @Bean
    @Primary
    public ApiKeyService testApiKeyService() {
        ApiKeyService mockApiKeyService = mock(ApiKeyService.class);
        // Mock validateApiKey to return null (no API key authentication in tests)
        when(mockApiKeyService.validateApiKey(anyString())).thenReturn(null);
        return mockApiKeyService;
    }
    
    @Bean
    @Primary
    public ClientRegistrationRepository testClientRegistrationRepository() {
        // Return a mock repository that returns null for any registration lookup
        // This prevents OAuth2 configuration issues in tests
        return new ClientRegistrationRepository() {
            @Override
            public ClientRegistration findByRegistrationId(String registrationId) {
                return null;
            }
        };
    }
}