package com.prime.gateway.gateway.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String MAX_IMAGE_SIZE;

    @Value("${java-api.problem-definitions-url}")
    private String problemsUrl;

    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleSizeLimitException() {
        Violation violation = new Violation(null, "Max image size is " + MAX_IMAGE_SIZE,
            LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED,
            getError("Max image size is " + MAX_IMAGE_SIZE));
        problemDetail.setTitle("Size Limit Exceeded");
        problemDetail.setType(URI.create(problemsUrl + "size-limit-exceeded"));
        problemDetail.setProperty("violations", violation);

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(problemDetail);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ProblemDetail> handleDateTimeParseExceptions(JsonMappingException e) {
        var references = e.getPath();

        var violation = new ViolationInvalidFormat(references.get(0).getFieldName(),
            getViolationMessage(getError(e.getCause().toString())), LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, getError(e.getMessage()));
        problemDetail.setTitle("Json Mapping");
        problemDetail.setType(URI.create(problemsUrl + "json-mapping"));
        problemDetail.setProperty("violations", violation);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(RuntimeException e) {
        Violation violation = new Violation(null, e.getMessage(), LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, getError(e.getMessage()));
        problemDetail.setTitle("Bad Credentials");
        problemDetail.setType(URI.create(problemsUrl + "bad-credential"));
        problemDetail.setProperty("violations", violation);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public final ResponseEntity<ProblemDetail> handleWrongParameterExceptions(
        WebClientResponseException ex) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode node = new ObjectMapper().readTree(ex.getResponseBodyAsString());

        ProblemDetail problemDetail = setProblemDetails(node, objectMapper, ex);

        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatusCode().value())).body(problemDetail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e) {
        Violation violation = new Violation(null, e.getMessage(), LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, getError(e.getMessage()));
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create(problemsUrl + "access-denied"));
        problemDetail.setProperty("violations", violation);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(problemDetail);
    }

    private record Violation(@JsonProperty("field") String field,
                             @JsonProperty("message") String message,
                             @JsonProperty("timestamp") LocalDateTime timestamp) {

    }

    private record ViolationInvalidFormat(@JsonProperty("field") String field,
                                          @JsonProperty("error") Map<String, String> error,
                                          @JsonProperty("timestamp") LocalDateTime timestamp) {

    }

    private Map<String, String> getViolationMessage(String error) {
        Map<String, String> message = new HashMap<>();
        message.put("message", error);
        return message;
    }

    private String getError(String error) {
        if (error.contains("escape")) {
            return "Invalid character used";
        }
        return "Invalid format, please compare your request to application documentation";
    }

    private ProblemDetail setProblemDetails(JsonNode node, ObjectMapper objectMapper, WebClientResponseException ex)
        throws JsonProcessingException {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(ex.getStatusCode().value()), getError(ex.getMessage()));
        problemDetail.setTitle("Web Client Response");
        problemDetail.setType(URI.create(problemsUrl + "web-client-response"));

        if (node.get("violations") == null && node.get("timestamp") == null) {
            return objectMapper.readValue(ex.getResponseBodyAsString(), ProblemDetail.class);
        }

        if (node.get("timestamp") != null) {
            problemDetail.setProperty("timestamp", node.get("timestamp"));

            return problemDetail;
        }

        problemDetail.setProperty("violations", node.get("violations"));

        return problemDetail;
    }
}
