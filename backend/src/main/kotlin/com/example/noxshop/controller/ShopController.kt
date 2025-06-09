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

/**
 * REST controller for handling product listings and purchases in the Nox Shop.
 *
 * This controller supports:
 * - Fetching the list of available products.
 * - Performing a product purchase for the authenticated user.
 * - Retrieving the purchase history of the authenticated user.
 * - Admin operations: add/remove/update products and list users' monthly spend.
 */
@RestController
@RequestMapping("/api")
class ShopController(
    private val productRepo: ProductRepository,
    private val purchaseRepo: PurchaseRepository,
) {

    /**
     * Returns a list of all available products in the shop.
     */
    @GetMapping("/products")
    fun getProducts(): List<Product> = productRepo.findByIsRemovedFalse()

    /**
     * Processes a product purchase for the currently authenticated user.
     */
    @PostMapping("/buy")
    fun buy(
        @RequestParam productId: Long,
        @RequestAttribute("uid") uid: String
    ): ResponseEntity<String> {
        val product = productRepo.findById(productId).orElseThrow { RuntimeException("Product not found") }

        val purchase = Purchase(
            userId = uid,
            productId = productId,
            price = product.price
        )
        purchaseRepo.save(purchase)

        return ResponseEntity.ok("Purchase successful!")
    }

    /**
     * Returns a list of purchases made by the authenticated user.
     */
    @GetMapping("/purchases")
    fun getPurchases(
        @RequestAttribute("uid") uid: String
    ): List<PurchaseDTO> {
        val purchases = purchaseRepo.findByUserId(uid)
        return purchases.mapNotNull { purchase ->
            val productOpt = productRepo.findById(purchase.productId)
            if (productOpt.isPresent) {
                val product = productOpt.get()
                PurchaseDTO(
                    id = purchase.id,
                    userId = purchase.userId,
                    productId = product.id,
                    productName = product.name,
                    price = purchase.price,
                    timestamp = purchase.timestamp
                )
            } else {
                // Product no longer exists, skip this purchase
                null
            }
        }
    }
}
