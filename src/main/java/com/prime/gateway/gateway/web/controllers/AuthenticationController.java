package com.prime.gateway.gateway.web.controllers;

import com.prime.gateway.gateway.domain.account.entity.Account;
import com.prime.gateway.gateway.infrastructure.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authenticate")
@Tag(description = "Resource for receiving Jwt authentication token",
        name = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Receive Jwt token")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = Account.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get JWT token",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "jwtToken: string"))}),
            @ApiResponse(responseCode = "401", description = "Incorrect email or password",
                    content = @Content)})
    @PostMapping
    public ResponseEntity<Object> createJwtToken(@RequestBody HashMap<String,String> auth) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(auth.get("email")
                            , auth.get("password")));

        } catch (RuntimeException ex) {
            LoggerFactory.getLogger(AuthenticationController.class).info("Exception occurred while trying to authenticate", ex);
            throw new BadCredentialsException("Incorrect email or password", ex);
        }

        final String jwtToken = jwtUtil.createJwtToken(authentication);
        Map<String,String> jwt = new HashMap<>();
        jwt.putIfAbsent("jwtToken",jwtToken);

        return ResponseEntity.ok(jwt);
    }
}
