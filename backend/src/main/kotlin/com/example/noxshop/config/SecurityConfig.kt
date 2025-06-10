package com.example.noxshop.config

import com.example.noxshop.security.FirebaseAuthFilter
import com.example.noxshop.security.LoggingFilter
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val loggingFilter: LoggingFilter, private val firebaseAuthFilter: FirebaseAuthFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }

            val isDocker = System.getenv("IS_DOCKER") == "true"
            if (!isDocker) {
                cors { }
            }

            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll) // preflight
                authorize("/api/products", permitAll)
                authorize("/api/**", authenticated)
                authorize(anyRequest, permitAll)
            }

            httpBasic { disable() }
            formLogin { disable() }
        }

        // Register FirebaseAuthFilter BEFORE Spring's default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        http.addFilterBefore(loggingFilter, FirebaseAuthFilter::class.java)
        return http.build()
    }
}