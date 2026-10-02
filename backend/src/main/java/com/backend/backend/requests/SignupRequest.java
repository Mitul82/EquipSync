package com.backend.backend.requests;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank(message="Email is required") String email, 
    @NotBlank(message="Password is required") String password, 
    @NotBlank(message="Departement is required") String department
) {

}