package com.example.noxshop.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class User(
    @Id val uid: String,
    val email: String
)
