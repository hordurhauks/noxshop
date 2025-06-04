package com.example.noxshop.dto

data class PurchaseDTO(
    val id: Long,
    val userId: String,
    val productId: Long,
    val productName: String,
    val price: Double,
    val timestamp: Long
)
