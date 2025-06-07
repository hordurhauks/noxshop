package com.example.noxshop.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }

            cors { }

            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll) // preflight
                authorize("/", permitAll)
                authorize("/index.html", permitAll)
                authorize("/main.js", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize("/api/products", permitAll)
                authorize("/api/**", authenticated)
            }

            oauth2ResourceServer {
                jwt { }
            }

            httpBasic { disable() }
            formLogin { disable() }
        }

        return http.build()
    }
}