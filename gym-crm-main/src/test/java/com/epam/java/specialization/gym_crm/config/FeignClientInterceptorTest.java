package com.epam.java.specialization.gym_crm.config;

import com.epam.java.specialization.gym_crm.security.JwtService;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignClientInterceptorTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FeignClientInterceptor interceptor;

    @AfterEach
    void clearSecurity() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should attach X-Transaction-Id and Bearer Token to outgoing feign requests")
    void apply_ShouldAddHeaders() {
        MDC.put("transactionId", "tx-test-12345");
        UserDetails userDetails = new User("admin", "pass", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(jwtService.generateToken(userDetails)).thenReturn("generated-jwt-token");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get("X-Transaction-Id")).containsExactly("tx-test-12345");
        assertThat(template.headers().get("Authorization")).containsExactly("Bearer generated-jwt-token");
    }
}