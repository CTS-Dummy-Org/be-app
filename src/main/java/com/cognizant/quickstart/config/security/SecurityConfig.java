package com.cognizant.quickstart.config.security;

import com.cognizant.quickstart.config.ConfigProperties;
import com.cognizant.quickstart.config.filter.AuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {
    @Autowired
    ConfigProperties configProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder)
        throws Exception {
        return builder.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        AuthenticationManager manager =
            (AuthenticationManager) applicationContext.getBean("authenticationManager");
        AuthenticationFilter customFilter =
            new AuthenticationFilter(manager, configProperties);
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(
            (requests) -> requests
                .requestMatchers("/api/v1/identityConfig", "/actuator/health",
                    "/healthcheck")
                .permitAll()
                .anyRequest()
                .authenticated())
            .addFilterBefore(customFilter, BasicAuthenticationFilter.class)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
            .cors(cors -> cors.configure(http))
            .httpBasic(withDefaults());
        return http.build();
    }
}
