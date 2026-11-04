package com.example.RealEstate2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Gives every HTTP request an ID and logs one summary line per request.
 *
 * - Reuses an incoming X-Request-Id header (so a client or another service
 *   can correlate), otherwise generates a short random one.
 * - Puts it in the MDC under "rid", so EVERY log line written while handling
 *   the request carries it (see logging.pattern.level in application.properties).
 * - Echoes it back in the X-Request-Id response header.
 * - Logs: method, path, status, duration. 5xx -> ERROR, 4xx -> WARN, else INFO.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "rid";
    private static final Logger log = LoggerFactory.getLogger("http");
    // Only accept sane client-supplied IDs, never raw user input into logs.
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        String rid = (incoming != null && SAFE_ID.matcher(incoming).matches())
                ? incoming
                : UUID.randomUUID().toString().substring(0, 8);

        MDC.put(MDC_KEY, rid);
        response.setHeader(HEADER, rid);
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
            logSummary(request, response.getStatus(), start, null);
        } catch (IOException | ServletException | RuntimeException ex) {
            // Unhandled exception escaped the controller: the container will
            // turn it into a 500, so log it as one with the root cause.
            logSummary(request, 500, start, ex);
            throw ex;
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private void logSummary(HttpServletRequest req, int status, long startNanos, Throwable ex) {
        long ms = (System.nanoTime() - startNanos) / 1_000_000;
        String line = String.format("%s %s -> %d (%d ms)", req.getMethod(), req.getRequestURI(), status, ms);
        if (ex != null) {
            log.error("{} cause={}", line, rootCause(ex));
        } else if (status >= 500) {
            log.error(line);
        } else if (status >= 400) {
            log.warn(line);
        } else {
            log.info(line);
        }
    }

    static String rootCause(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        return t.getClass().getSimpleName() + ": " + t.getMessage();
    }
}
