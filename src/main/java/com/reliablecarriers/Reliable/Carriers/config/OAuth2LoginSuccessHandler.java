package com.reliablecarriers.Reliable.Carriers.config;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, AuthService authService, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication == null) {
            response.sendRedirect("/oauth2/success");
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User)) {
            response.sendRedirect("/oauth2/success");
            return;
        }

        OAuth2User oauthUser = (OAuth2User) principal;
        String email = (String) oauthUser.getAttributes().getOrDefault("email", oauthUser.getAttribute("email"));
        if (email == null) {
            // no email found — redirect to success page without token
            response.sendRedirect("/oauth2/success");
            return;
        }

        // Find existing user by email
        User user = userRepository.findByEmail(email).orElse(null);

        // Extract provider info
        String provider = extractProviderFromRequest(request);
        String providerId = extractProviderId(oauthUser);

        if (user == null) {
            // No local account exists; create one and link provider info
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName((String) oauthUser.getAttributes().getOrDefault("given_name", ""));
            newUser.setLastName((String) oauthUser.getAttributes().getOrDefault("family_name", ""));
            newUser.setRole(UserRole.CUSTOMER); // default social logins as customers
            newUser.setPassword(java.util.UUID.randomUUID().toString());
            newUser.setOauthProvider(provider);
            newUser.setOauthProviderId(providerId);
            user = authService.registerUser(newUser);
        } else {
            // Local user exists. If provider not linked, link it now and save.
            if (user.getOauthProvider() == null || user.getOauthProvider().isEmpty()) {
                user.setOauthProvider(provider);
                user.setOauthProviderId(providerId);
                userRepository.save(user);
            }
        }

        // Social logins bypass 2FA as per requirement — issue JWT and redirect with token
        final String token = jwtTokenUtil.generateToken(new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), java.util.Collections.emptyList()));

        String url = "/oauth2/success?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8) + "&role=" + URLEncoder.encode(user.getRole().name(), StandardCharsets.UTF_8);
        response.sendRedirect(url);
    }

    private String extractProviderFromRequest(HttpServletRequest request) {
        // Spring sets the registrationId as part of the request URI in many setups: /oauth2/authorization/{registrationId}
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("oauth2")) {
            // naive extraction: look for last path segment
            String[] parts = uri.split("/");
            if (parts.length > 0) {
                String last = parts[parts.length - 1];
                return last;
            }
        }
        // fallback to provider attribute if available
        return (String) request.getParameter("provider");
    }

    private String extractProviderId(OAuth2User oauthUser) {
        Object id = oauthUser.getAttributes().getOrDefault("sub", oauthUser.getAttributes().get("id"));
        return id != null ? id.toString() : null;
    }
}
