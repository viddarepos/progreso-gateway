package com.prime.gateway.gateway.infrastructure.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.prime.gateway.gateway.domain.account.entity.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Component
public class WebClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientUtil.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${infrastructure.javaApiUrl}")
    private String javaApiUrl;
    @Value("${infrastructure.nodeJsUrl}")
    private String nodeJsApiUrl;
    @Value("${infrastructure.dotNetUrl}")
    private String dotNetApuUrl;
    @Value(("${server.servlet.context-path}"))
    private String contextPath;

    public WebClientUtil(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Object> send(HttpServletRequest request) {
        System.out.println("In WebClientUtil::send: " + request.getRequestURI());
        String uri = uriBuilder(request);
        System.out.println("In WebClientUtil::send: uri: " + uri);



        try {
            Object o = objectMapper.readValue(request.getInputStream(), Object.class);
            LOGGER.info("{} request with body send to {}", request.getMethod(), uri);
            return webClient.method(HttpMethod.valueOf(request.getMethod())).uri(uri).body(BodyInserters.fromValue(o)).retrieve().toEntity(Object.class).block();

        } catch (IOException e) {
            LOGGER.info("{} request without body send to {}", request.getMethod(), uri);
            return webClient.method(HttpMethod.valueOf(request.getMethod())).uri(uri).retrieve().toEntity(Object.class).block();
        }
    }

    public ResponseEntity<Object> send(HttpServletRequest request, MultipartFile multipartFile) {
        String uri = uriBuilder(request);
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        try {
            if (request.getPart("user") != null) {
                builder.part("user", objectMapper.readValue(request.getPart("user").getInputStream(), Object.class));
            }
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
        if (multipartFile != null) {
            builder.part("file", multipartFile.getResource());
        }

        return webClient.method(httpMethod).uri(uri).body(BodyInserters.fromMultipartData(builder.build())).retrieve().toEntity(Object.class).block();
    }

    public Account send(Long userId) {
        URI uri = URI.create(javaApiUrl + "/account/" + userId.toString());

        return webClient.get().uri(uri).retrieve().bodyToMono(Account.class).block();
    }

    public Account send(String email) {
        URI uri = URI.create(javaApiUrl + "/account/email/" + email);

        return webClient.get().uri(uri).retrieve().bodyToMono(Account.class).block();
    }

    private String uriBuilder(HttpServletRequest request) {
        System.out.println("Request in: " + request.getRequestURI());
        String requestURI = request.getRequestURI().substring(contextPath.length());

        System.out.println("Trimmed request: " + requestURI);

        String serviceURI = javaApiUrl;

        if (isNodeJSServiceUrl(requestURI)) {
            serviceURI = nodeJsApiUrl;
        } else if (isDotNetServiceUrl(requestURI)) {
            serviceURI = dotNetApuUrl;
        }

        var reqest = request.getQueryString() == null ? URI.create(serviceURI + requestURI).toString() : URI.create(serviceURI + requestURI + "?" + request.getQueryString()).toString();
        System.out.println("Final  request: " + requestURI);
        return reqest;
    }

    private boolean isNodeJSServiceUrl(String requestURI) {
        var nodeJsUrls = List.of("/feedback-templates", "/form-fields");

        return nodeJsUrls.stream().anyMatch(requestURI::contains);
    }

    private boolean isDotNetServiceUrl(String requestURI) {
        var dotNetUrls = List.of("/api/activities", "/api/answer-choices", "/api/assignments",
                "/api/bpmn-diagrams", "/api/coding-challenges", "/api/curriculums", "/api/curriculum-items",
                "/api/keyword-descriptions", "/api/keywords", "/api/keywords/results/single-player", "/api/languages",
                "/api/milestones", "/api/notifications/notify-all", "/api/notifications/notify-user",
                "/api/notifications/notify-group", "/api/projects", "/api/questions", "/api/question-categories",
                "/api/quizzes", "/api/quiz-executions","/api/quiz-assignments", "/api/technologies", "/api/test-cases");

        return dotNetUrls.stream().anyMatch(requestURI::contains);
    }

    public ResponseEntity<byte[]> receiveUserPicture(HttpServletRequest request) {
        String uri = uriBuilder(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return webClient.method(HttpMethod.valueOf(request.getMethod()))
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(byte[].class)
                .block();
    }
}
