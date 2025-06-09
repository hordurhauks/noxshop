package com.example.noxshop.repositories

import com.example.noxshop.model.Purchase
import org.springframework.data.jpa.repository.JpaRepository

interface PurchaseRepository : JpaRepository<Purchase, Long> {
    fun findByUserId(userId: String): List<Purchase>
}