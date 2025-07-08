package org.snomed.cis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class RequestManagerTest {

    private RequestManager requestManager;

    @BeforeEach
    void setUp() throws Exception {
        Constructor<RequestManager> constructor = RequestManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        requestManager = constructor.newInstance();
    }

    @Test
    void testPostRequestWithoutPayload_throwsGenericException() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        assertThrows(CisException.class, () -> {
            requestManager.postRequestWithoutPayload("http://localhost:9999/fake", headers);
        });
    }

    @Test
    void testPostRequest_throwsGenericException() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String payload = "{}";
        assertThrows(CisException.class, () -> {
            requestManager.postRequest("http://localhost:9999/fake", headers, payload);
        });
    }

    @Test
    void testGetRequest_throwsGenericException() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        assertThrows(CisException.class, () -> {
            requestManager.getRequest("http://localhost:9999/fake", headers);
        });
    }

    @Test
    void testPostRequestWithoutPayload_shouldThrowCisException() {
        CisException ex = assertThrows(CisException.class, () -> {
            requestManager.postRequestWithoutPayload("http://localhost:9999/fake", null);
        });
        assertTrue(ex.getMessage().contains("error"));
    }

    @Test
    void testGetRequest_withNullHeaders_shouldThrowException() {
        assertThrows(CisException.class, () -> {
            requestManager.getRequest("http://localhost:9999/fake", null);
        });
    }

    @Test
    void testPostRequest_malformedUrl_shouldThrowCisException() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        assertThrows(CisException.class, () -> {
            requestManager.postRequest("htp://bad-url", headers, "{}");
        });
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<RequestManager> constructor = RequestManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @Test
    void testRealGetRequest_toHttpBin_shouldSucceed() throws CisException {
        ResponseEntity<String> response = requestManager.getRequest("https://httpbin.org/get", null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPostRequest_emptyPayload_shouldThrowException() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        assertThrows(CisException.class, () -> {
            requestManager.postRequest("http://localhost:9999/fake", headers, "");
        });
    }

}
