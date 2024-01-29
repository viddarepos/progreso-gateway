package com.prime.gateway.gateway;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Hidden
@RestController
public class TestController {

    private final RestTemplate restTemplate;

    @Value("${infrastructure.javaApiUrl}")
    private String javaApiUrl;

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/testJavaApiCommunication")
    public String test() {
        log.info("hello test");

        String uri = javaApiUrl + "/test";

        Object response = restTemplate.getForObject(
                uri, String.class);

        log.info(response.toString());

        return response.toString();
    }

    @GetMapping("/test")
    public String testForExternal() {
        return "Java Gateway OK";
    }
}