package com.epam.java.specialization.gym_crm.config;

import com.epam.java.specialization.gym_crm.security.JwtService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtService jwtService;

    @Override
    public void apply(RequestTemplate template) {
        
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        if (transactionId == null || transactionId.isBlank()) {
            HttpServletRequest currentRequest = getCurrentHttpRequest();
            if (currentRequest != null) {
                transactionId = currentRequest.getHeader(TRANSACTION_ID_HEADER);
            }
        }
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        template.header(TRANSACTION_ID_HEADER, transactionId);

        
        HttpServletRequest request = getCurrentHttpRequest();
        String authHeader = null;
        if (request != null) {
            authHeader = request.getHeader(AUTHORIZATION_HEADER);
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            template.header(AUTHORIZATION_HEADER, authHeader);
        } else {
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                String token = jwtService.generateToken(userDetails);
                template.header(AUTHORIZATION_HEADER, "Bearer " + token);
            }
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}