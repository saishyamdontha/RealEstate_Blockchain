package com.example.RealEstate2.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void generatesId_putsItInMdcDuringRequest_andClearsAfter() throws Exception {
        var req = new MockHttpServletRequest("GET", "/api/user/properties/for-sale");
        var res = new MockHttpServletResponse();
        AtomicReference<String> seenInMdc = new AtomicReference<>();

        filter.doFilter(req, res, (rq, rs) -> seenInMdc.set(MDC.get("rid")));

        String rid = res.getHeader("X-Request-Id");
        assertThat(rid).isNotBlank();
        assertThat(seenInMdc.get()).isEqualTo(rid);
        assertThat(MDC.get("rid")).isNull();
    }

    @Test
    void reusesSafeIncomingId() throws Exception {
        var req = new MockHttpServletRequest("GET", "/x");
        req.addHeader("X-Request-Id", "smoke-123");
        var res = new MockHttpServletResponse();

        filter.doFilter(req, res, (rq, rs) -> {});

        assertThat(res.getHeader("X-Request-Id")).isEqualTo("smoke-123");
    }

    @Test
    void replacesUnsafeIncomingId() throws Exception {
        var req = new MockHttpServletRequest("GET", "/x");
        req.addHeader("X-Request-Id", "bad id\nINJECTED LOG LINE");
        var res = new MockHttpServletResponse();

        filter.doFilter(req, res, (rq, rs) -> {});

        assertThat(res.getHeader("X-Request-Id")).doesNotContain("INJECTED").hasSize(8);
    }

    @Test
    void rethrowsUnhandledException_andStillClearsMdc() {
        var req = new MockHttpServletRequest("POST", "/ledger/1/finalize");
        var res = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(req, res, (rq, rs) -> {
            throw new ServletException(new IllegalStateException("chain down"));
        })).isInstanceOf(ServletException.class);

        assertThat(MDC.get("rid")).isNull();
    }

    @Test
    void rootCause_returnsDeepestCause() {
        var ex = new RuntimeException("outer", new IllegalStateException("Connection refused"));
        assertThat(RequestLoggingFilter.rootCause(ex)).isEqualTo("IllegalStateException: Connection refused");
    }
}
