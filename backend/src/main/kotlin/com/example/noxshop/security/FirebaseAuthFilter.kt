package com.example.noxshop.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.FileInputStream
import javax.annotation.PostConstruct

@Component
class FirebaseAuthFilter : OncePerRequestFilter() {

    private lateinit var firebaseAuth: FirebaseAuth

    data class TokenInfo(val uid: String, val email: String)

    @PostConstruct
    fun init() {
        val isDocker = System.getenv("IS_DOCKER") == "true"
        val path = if (isDocker) {
            "/app/firebase/serviceAccountKey.json"
        } else {
            "firebase/serviceAccountKey.json"
        }
        val serviceAccount = FileInputStream(path)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        // Initialize Firebase only if not already initialized
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // 1. No token? Just continue without setting auth.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val idToken = authHeader.removePrefix("Bearer ").trim()

        try {
            val decoded = firebaseAuth.verifyIdToken(idToken)
            val uid = decoded.uid
            val email = decoded.email

            // 2. Set attributes for controllers
            request.setAttribute("uid", uid)
            request.setAttribute("email", email)

            // 3. Set Spring Security context
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            val auth = UsernamePasswordAuthenticationToken(uid, null, authorities)
            SecurityContextHolder.getContext().authentication = auth

        } catch (e: Exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Firebase token")
            return
        }

        // 5. Always continue
        filterChain.doFilter(request, response)
    }
}
