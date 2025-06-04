package com.example.noxshop.security

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Component
import java.io.FileInputStream
import javax.annotation.PostConstruct

@Component
class FirebaseTokenVerifier {

    @PostConstruct
    fun init() {
        val serviceAccount = FileInputStream("firebase/serviceAccountKey.json")
        val options = FirebaseOptions.builder()
            .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
            .build()
        FirebaseApp.initializeApp(options)
    }

    fun verifyToken(idToken: String): String? {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
        return decodedToken.uid
    }
}
