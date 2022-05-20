package org.snomed.cis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.pojo.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class RequestManager {

    private final Logger logger = LoggerFactory.getLogger(RequestManager.class);

    @Autowired
    Config config;

    private RequestManager() {
    }

    /*
     POST request without payload
     */
    public ResponseEntity<String> postRequestWithoutPayload(String url) throws CisException {
        logger.debug("RequestManager.postRequestWithoutPayload() {}", url);

        try {
            ResponseEntity<String> response = WebClient.builder().baseUrl(url).build().post()
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            logger.info("RequestManager.postRequestWithoutPayload() - Response :: {}", response);
            return response;
        } catch (WebClientResponseException e) {
            throw new CisException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> postRequest(String url, MultiValueMap<String, String> headers, String payload) throws CisException {
        logger.debug("RequestManager.postRequest() {} :: {}", url, payload);

        try {
            ResponseEntity<String> response = WebClient.builder().baseUrl(url).build().post()
                    .headers(httpHeaders -> {
                        Optional.ofNullable(headers).ifPresent(h -> h.forEach(httpHeaders::addAll));
                    })
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            logger.info("RequestManager.postRequest() - Response :: {}", response);
            return response;
        } catch (WebClientResponseException e) {
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> getRequest(String url) throws CisException {
        try {
            ResponseEntity<String> response = WebClient.builder().baseUrl(url).build().get()
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            logger.info("RequestManager.getRequest() - Response :: {}", response);
            return response;
        } catch (WebClientResponseException e) {
            throw new CisException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

}
