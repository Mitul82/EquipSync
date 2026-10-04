package com.backend.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
public class JwtCookieAuthenticationFilterTest {
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtCookieAuthenticationFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails mockUserDetails;

    private final String TEST_TOKEN = "valid.jwt.token";
    private final String TEST_EMAIL = "mitul@company.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        mockUserDetails = new User(
            TEST_EMAIL, 
            "password", 
            Collections.singletonList(new SimpleGrantedAuthority("Manager"))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilterInternal_WhenValidTokenProvided_SetsSecurityContext() throws Exception {
        request.setCookies(new Cookie("ACCESS_TOKEN", TEST_TOKEN));

        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(mockUserDetails);
        when(jwtService.isTokenValid(TEST_TOKEN, mockUserDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set");
        assertEquals(TEST_EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_WhenNoCookiesExist_SkipsAuthentication() throws Exception {
        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should remain null");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    public void doFilterInternal_WhenJwtIsTamperedOrExpired_SkipsAuthenticationAndContinues() throws Exception {
        request.setCookies(new Cookie("ACCESS_TOKEN", "tampered.token"));
        
        when(jwtService.extractUsername("tampered.token")).thenThrow(new JwtException("Invalid signature"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should remain null if JWT fails");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_WhenUserIsDeletedFromDB_SkipsAuthenticationAndContinues() throws Exception {
        request.setCookies(new Cookie("ACCESS_TOKEN", TEST_TOKEN));

        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn("ghost@company.com");
        when(userDetailsService.loadUserByUsername("ghost@company.com")).thenThrow(new UsernameNotFoundException("Deleted user"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should remain null if user is gone");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_WhenAlreadyAuthenticated_DoesNotOverrideAuthentication() throws Exception {
        request.setCookies(new Cookie("ACCESS_TOKEN", TEST_TOKEN));
        var existing = new UsernamePasswordAuthenticationToken("existing@company.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existing);
    
        jwtFilter.doFilterInternal(request, response, filterChain);
    
        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_WhenTokenIsInvalid_SkipsAuthenticationAndContinues() throws Exception {
        request.setCookies(new Cookie("ACCESS_TOKEN", TEST_TOKEN));

        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(mockUserDetails);
        when(jwtService.isTokenValid(TEST_TOKEN, mockUserDetails)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}