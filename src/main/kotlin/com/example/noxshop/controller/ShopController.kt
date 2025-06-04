package com.example.noxshop.controller

import com.example.noxshop.dto.PurchaseDTO
import com.example.noxshop.model.Product
import com.example.noxshop.model.Purchase
import com.example.noxshop.repositories.ProductRepository
import com.example.noxshop.repositories.PurchaseRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ShopController(
    private val productRepo: ProductRepository,
    private val purchaseRepo: PurchaseRepository,
) {

    @GetMapping("/products")
    fun getProducts(): List<Product> = productRepo.findAll()

    @PostMapping("/buy")
    fun buy(
        @RequestParam productId: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val userId = jwt.subject // Firebase UID
        val product = productRepo.findById(productId).orElseThrow { RuntimeException("Product not found") }

        val purchase = Purchase(
            userId = userId,
            productId = productId,
            price = product.price
        )
        purchaseRepo.save(purchase)

        return ResponseEntity.ok("Purchase successful!")
    }

    @GetMapping("/purchases")
    fun getPurchases(
        @AuthenticationPrincipal jwt: Jwt
    ): List<PurchaseDTO> {
        val userId = jwt.subject
        val purchases = purchaseRepo.findByUserId(userId)
        return purchases.map { purchase ->
            val product = productRepo.findById(purchase.productId).orElseThrow()
            PurchaseDTO(
                id = purchase.id,
                userId = purchase.userId,
                productId = product.id,
                productName = product.name,
                price = purchase.price,
                timestamp = purchase.timestamp
            )
        }
    }
}
