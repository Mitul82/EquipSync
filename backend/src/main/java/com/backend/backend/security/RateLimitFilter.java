package com.backend.backend.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {
    private final RateLimitService rateLimitService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // NOTE:: this is the code snippet if the spring app is being used behind a proxy and has to handle proxy headers as well
        // * String ip = httpRequest.getHeader("X-Forwarded-For");
        // * if (ip == null) {
        // *     ip = httpRequest.getRemoteAddr();
        // * }

        // NOTE:: this is the way to rate limit a IP if the backend is not behing a proxy
        String ip = httpRequest.getRemoteAddr();

        if (rateLimitService.tryConsume(ip)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Too many requests. Please try again later.");
        }
    }
}
