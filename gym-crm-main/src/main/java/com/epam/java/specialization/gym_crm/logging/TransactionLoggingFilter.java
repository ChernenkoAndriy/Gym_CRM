package com.epam.java.specialization.gym_crm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.trim().isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = wrappedResponse.getStatus();

            String requestBody = getPayload(wrappedRequest.getContentAsByteArray());
            String responseBody = getPayload(wrappedResponse.getContentAsByteArray());

            if (status >= 400) {
                log.warn("Finished REST Call: [{}] {} | Status: {} | Duration: {}ms | Request Payload: {} | Error Response: {}",
                        method, fullPath, status, duration, requestBody, responseBody);
            } else {
                log.info("Finished REST Call: [{}] {} | Status: {} | Duration: {}ms | Request Payload: {} | Response Payload: {}",
                        method, fullPath, status, duration, requestBody, responseBody);
            }

            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    private String getPayload(byte[] buf) {
        if (buf == null || buf.length == 0) {
            return "[EMPTY]";
        }
        return new String(buf, 0, buf.length, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
    }
}