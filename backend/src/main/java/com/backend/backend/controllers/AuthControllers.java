package com.backend.backend.controllers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.backend.dto.UserDTO;
import com.backend.backend.requests.LoginRequest;
import com.backend.backend.requests.SignupRequest;
import com.backend.backend.responses.ApiResponse;
import com.backend.backend.security.JwtService;
import com.backend.backend.services.AuthService.IAuthService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthControllers {
    private final JwtService jwtService;
    private final IAuthService authService;

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse> getCSRFToken(CsrfToken csrfToken) {
        csrfToken.getToken();

        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> userLogin(@RequestBody LoginRequest req) {
        try {
            UserDTO user = authService.login(req.email(), req.password());

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("id", user.getId());
            extraClaims.put("role", user.getRole());
            extraClaims.put("email", user.getEmail());

            String token = jwtService.generateToken(extraClaims, user.getEmail());

            ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new ApiResponse("Login successfull", user));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> userSignup(@RequestBody SignupRequest req) {
        try {
            UserDTO user = authService.signup(req.email(), req.password(), req.department());

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("id", user.getId());
            extraClaims.put("role", user.getRole());
            extraClaims.put("email", user.getEmail());

            String token = jwtService.generateToken(extraClaims, user.getEmail());

            ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
            
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new ApiResponse("Sigenup successfull", user));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }
    

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from("ACCESS_TOKEN", "")
            .httpOnly(true)
            .secure(false)  // NOTE this is true for local dev only and should be set to true for production deployments
            .sameSite("Lax")    // NOTE Matching the login cookie
            .path("/")  // NOTE Must match the login cookie
            .maxAge(Duration.ZERO)
            .build();

        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, expiredCookie.toString()).build();
    }
}