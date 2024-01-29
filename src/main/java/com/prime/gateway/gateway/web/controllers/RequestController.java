package com.prime.gateway.gateway.web.controllers;


import com.prime.gateway.gateway.domain.account.service.AccountService;
import com.prime.gateway.gateway.infrastructure.util.WebClientUtil;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Hidden
public class RequestController {

    private final WebClientUtil webClient;
    private final AccountService accountService;

    public RequestController(WebClientUtil webClient, AccountService accountService) {
        this.webClient = webClient;
        this.accountService = accountService;
    }

    @RequestMapping(value = {"/**"},
            method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.PUT})
    public ResponseEntity<?> send(HttpServletRequest request) {
        ResponseEntity<Object> response = webClient.send(request);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @RequestMapping(value = "/users/**", method = RequestMethod.PATCH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> users(HttpServletRequest request, MultipartFile file) {
        ResponseEntity<Object> response = webClient.send(request, file);

        ResponseEntity<Object> redisResponse = accountService.updateRedis(request, response);
        if (redisResponse != null) return ResponseEntity.status(redisResponse.getStatusCode()).body(redisResponse.getBody());

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @RequestMapping(value = "/users/{userId}/**",
            method = RequestMethod.DELETE)
    public ResponseEntity<?> users(HttpServletRequest request, @PathVariable Long userId) {

        String email = accountService.getAccountEmail(userId);
        ResponseEntity<Object> response = webClient.send(request);

        if(response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            accountService.deleteRedis(response, email);
        }

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @RequestMapping(value = "/users/**",
            method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PATCH})
    public ResponseEntity<?> users(HttpServletRequest request) {
        ResponseEntity<Object> response = webClient.send(request);

        if (response.getBody() != null) {
            ResponseEntity<Object> redisResponse = accountService.updateRedis(request, response);
            if (redisResponse != null)
                return ResponseEntity.status(redisResponse.getStatusCode()).body(redisResponse.getBody());
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @RequestMapping(value = "/technologies/**",
            method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> technologies(HttpServletRequest request) {

        ResponseEntity<Object> response = webClient.send(request);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping(value = "/profile_pictures/**",
            produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> userPicture(HttpServletRequest request) {
        ResponseEntity<byte[]> response = webClient.receiveUserPicture(request);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}

