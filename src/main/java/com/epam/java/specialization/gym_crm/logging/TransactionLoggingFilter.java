package com.epam.java.specialization.gym_crm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Генерація transactionId для транзакційного рівня логування
        String transactionId = request.getHeader("X-Transaction-Id");
        if (transactionId == null || transactionId.trim().isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID_KEY, transactionId);
        response.setHeader("X-Transaction-Id", transactionId);

        // 2. Логування деталей вхідного REST-дзвінка
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        log.info("Received REST Call: [{}] {}", method, fullPath);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // 3. Логування деталей відповіді сервісу
            if (status >= 400) {
                log.warn("Finished REST Call: [{}] {} with ERROR status {} (Duration: {}ms)",
                        method, uri, status, duration);
            } else {
                log.info("Finished REST Call: [{}] {} with SUCCESS status {} (Duration: {}ms)",
                        method, uri, status, duration);
            }

            MDC.clear();
        }
    }
}