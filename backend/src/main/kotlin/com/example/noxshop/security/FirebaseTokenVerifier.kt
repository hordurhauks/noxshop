package com.example.noxshop.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import javax.annotation.PostConstruct

@Component
class FirebaseTokenVerifier {

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
        FirebaseApp.initializeApp(options)
    }

    fun verifyToken(idToken: String): String? {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
        return decodedToken.uid
    }
}
