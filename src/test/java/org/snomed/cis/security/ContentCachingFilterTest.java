package org.snomed.cis.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

class ContentCachingFilterTest {

    private ContentCachingFilter filter;
    private FilterChain mockFilterChain;
    private HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        filter = new ContentCachingFilter();
        mockFilterChain = mock(FilterChain.class);
        mockResponse = new MockHttpServletResponse();
    }

    @Test
    void testDoFilterInternal_shouldWrapRequestAndPassToChain() throws ServletException, IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent("test-body".getBytes());
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
        verify(mockFilterChain, times(1)).doFilter(any(CachedBodyHttpServletRequest.class), eq(mockResponse));
    }

    @Test
    void testDoFilterInternal_withEmptyBody_shouldStillWork() throws ServletException, IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(new byte[0]);

        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(any(CachedBodyHttpServletRequest.class), eq(mockResponse));
    }
}
