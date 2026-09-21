package org.snomed.cis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class RequestManager {

    private final Logger logger = LoggerFactory.getLogger(RequestManager.class);

    private RequestManager() {
    }

    /*
     POST request without payload
     */
    public ResponseEntity<String> postRequestWithoutPayload(String url, HttpHeaders headers) throws CisException {

        try {
            ResponseEntity<String> response = WebClient.builder().baseUrl(url).build().post()
                    .headers(httpHeaders -> Optional.ofNullable(headers).ifPresent(httpHeaders::addAll))
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            return response;
        } catch (WebClientResponseException e) {
            throw new CisException(HttpStatus.valueOf(e.getStatusCode().value()), "client request error");
        } catch (Exception e) {
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error at server");
        }
    }

    public ResponseEntity<String> postRequest(String url, HttpHeaders headers, String payload) throws CisException {

        try {
            ResponseEntity<String> response = WebClient.builder().baseUrl(url).build().post()
                    .headers(httpHeaders -> Optional.ofNullable(headers).ifPresent(httpHeaders::addAll))
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            return response;
        } catch (WebClientResponseException e) {
            throw new CisException(HttpStatus.valueOf(e.getStatusCode().value()), "client request error");
        } catch (Exception e) {
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error at server");
        }
    }

    public ResponseEntity<String> getRequest(String url, HttpHeaders headers) throws CisException {
        try {
            ResponseEntity<String> response = WebClient.builder().baseUrl(url).build().get()
                    .headers(httpHeaders -> Optional.ofNullable(headers).ifPresent(httpHeaders::addAll))
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            return response;
        } catch (WebClientResponseException e) {
            throw new CisException(HttpStatus.valueOf(e.getStatusCode().value()), "client request error");
        } catch (Exception e) {
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error at server");
        }
    }

}

