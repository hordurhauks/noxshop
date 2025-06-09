package com.example.noxshop.security

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingFilter : Filter {

    private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpReq = request as HttpServletRequest
        logger.info("Incoming request: ${httpReq.method} ${httpReq.requestURI}")
        chain.doFilter(request, response)
    }
}
