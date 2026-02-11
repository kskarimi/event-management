package com.kkarimi.eventmanagement.web;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerApiRateLimitFilterTest {

    @Test
    void shouldAllowFirstRequestAndBlockSecondWhenLimitIsOne() throws ServletException, IOException {
        CustomerApiRateLimitFilter filter = new CustomerApiRateLimitFilter(1);

        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/attendees");
        request1.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request1, response1, new MockFilterChain());
        assertEquals(200, response1.getStatus());

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/attendees");
        request2.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request2, response2, new MockFilterChain());
        assertEquals(429, response2.getStatus());
        assertEquals("{\"error\":\"rate_limit_exceeded\"}", response2.getContentAsString());
    }

    @Test
    void shouldBypassRateLimitForNonCustomerApi() throws ServletException, IOException {
        CustomerApiRateLimitFilter filter = new CustomerApiRateLimitFilter(1);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
    }
}
