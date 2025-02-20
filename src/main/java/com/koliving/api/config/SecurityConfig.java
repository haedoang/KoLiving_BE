package com.koliving.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koliving.api.auth.CustomExceptionHandlerFilter;
import com.koliving.api.auth.jwt.IJwtService;
import com.koliving.api.auth.jwt.JwtAuthenticationFilter;
import com.koliving.api.auth.jwt.JwtProvider;
import com.koliving.api.auth.login.LoginFailureHandler;
import com.koliving.api.auth.login.LoginFilter;
import com.koliving.api.auth.login.LoginProvider;
import com.koliving.api.auth.login.LoginSuccessHandler;
import com.koliving.api.utils.HttpUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static String[] AUTHENTICATION_WHITELIST = null;
    private static String[] AUTHORIZATION_WHITELIST = null;

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final CustomLocaleResolver localeResolver;
    private final ObjectPostProcessor<Object> objectPostProcessor;
    private final LoginProvider loginProvider;
    private final LocalValidatorFactoryBean validator;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtProvider jwtProvider;
    private final IJwtService jwtService;
    private final SimpleUrlLogoutSuccessHandler logoutSuccessHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final HttpUtils httpUtils;

    @PostConstruct
    private void init() {
        AUTHENTICATION_WHITELIST = new String[]{
            httpUtils.getCurrentVersionPath("auth/**"),
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/api/v1/rooms/search"
        };

        AUTHORIZATION_WHITELIST = new String[]{
            httpUtils.getCurrentVersionPath("login"),
            httpUtils.getCurrentVersionPath("logout"),
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers(PathRequest.toH2Console())
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            .requestMatchers(AUTHENTICATION_WHITELIST);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationManager(loginProvider);

        CustomExceptionHandlerFilter customExceptionHandlerFilter = createExceptionHandlerFilter();
        LoginFilter loginFilter = createLoginFilter(authenticationManager);
        JwtAuthenticationFilter jwtAuthenticationFilter = createJwtAuthenticationFilter();

        http.cors(withDefaults())
            .csrf().disable()
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(req -> {
                req
                    .requestMatchers(AUTHENTICATION_WHITELIST).permitAll()
                    .requestMatchers(AUTHORIZATION_WHITELIST).permitAll()
                    .requestMatchers("/api/v1/management/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/**").hasRole("USER")
                    .anyRequest().authenticated();
            })
            .logout(config -> {
                config.logoutUrl(httpUtils.getCurrentVersionPath("logout"))
                    .logoutSuccessUrl(httpUtils.getCurrentVersionPath("login"))
                    .logoutSuccessHandler(logoutSuccessHandler);
            })
            .exceptionHandling(config -> {
                config.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler);
            })
            .addFilterBefore(customExceptionHandlerFilter, HeaderWriterFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, loginFilter.getClass())
            .addFilterAfter(loginFilter, LogoutFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider... authenticationProviders)
        throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);

        for (AuthenticationProvider provider : authenticationProviders) {
            builder.authenticationProvider(provider);
        }

        return builder.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(
            List.of(
                "http://localhost:3000",
                "http://*.localhost:[*]",
                "https://*.localhost:[*]",
                "http://koliving.kro.kr:[*]",
                "https://koliving.kro.kr:[*]"
            )
        );
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private CustomExceptionHandlerFilter createExceptionHandlerFilter() {
        return new CustomExceptionHandlerFilter(httpUtils, messageSource, localeResolver);
    }

    private LoginFilter createLoginFilter(AuthenticationManager authenticationManager) {
        LoginFilter loginFilter = new LoginFilter(authenticationManager, objectMapper, validator);
        loginFilter.setFilterProcessesUrl(httpUtils.getCurrentVersionPath("login"));
        loginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(loginFailureHandler);
        loginFilter.afterPropertiesSet();

        return loginFilter;
    }

    private JwtAuthenticationFilter createJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, jwtService, httpUtils);
    }
}
