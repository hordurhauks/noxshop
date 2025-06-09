package com.example.noxshop.model

import jakarta.persistence.*

@Entity
data class Account(
    @Id val uid: String,
    val email: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_roles", joinColumns = [JoinColumn(name = "account_uid")])
    @Column(name = "role")
    val roles: Set<String> = setOf("USER")  // default role USER
)
