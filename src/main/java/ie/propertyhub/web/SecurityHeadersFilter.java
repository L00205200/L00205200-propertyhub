package ie.propertyhub.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Adds baseline HTTP security headers (OWASP secure headers recommendations). */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    static final String CSP = "default-src 'self'; img-src 'self' data:; style-src 'self'; "
            + "script-src 'self'; frame-ancestors 'none'; "
            + "form-action 'self'; base-uri 'self'";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
        chain.doFilter(request, response);
    }
}
