package com.prime.gateway.gateway.infrastructure.exception;

import com.prime.gateway.gateway.infrastructure.util.PropertiesExtractor;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class InvalidUrlPath extends ErrorResponseException {

  public InvalidUrlPath(String fieldValue) {
    super(HttpStatus.NOT_FOUND,
        asProblemDetail(fieldValue + " URL not found.",
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
  }

  private static ProblemDetail asProblemDetail(String message, String problemUrl) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
    problemDetail.setTitle("URL not found");
    problemDetail.setType(URI.create(problemUrl + "url-not-found"));
    return problemDetail;
  }


}
