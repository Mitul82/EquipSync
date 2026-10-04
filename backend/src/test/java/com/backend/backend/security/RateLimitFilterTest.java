package com.backend.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
public class RateLimitFilterTest {
    @Mock private RateLimitService rateLimitService;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    private final String TEST_IP = "192.168.1.100";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        request.setRemoteAddr(TEST_IP);
    }

    @Test
    public void doFilter_WhenLimitNotExceeded_ContinuesFilterChain() throws IOException, ServletException {
        when(rateLimitService.tryConsume(TEST_IP)).thenReturn(true);

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus()); 
    }

    @Test
    public void doFilter_WhenLimitExceeded_BlocksRequestAndReturns429() throws IOException, ServletException {
        when(rateLimitService.tryConsume(TEST_IP)).thenReturn(false);

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatus());
        assertEquals("Too many requests. Please try again later.", response.getContentAsString());
    }
}