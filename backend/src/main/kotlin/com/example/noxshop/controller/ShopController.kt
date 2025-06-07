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
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<String> {
        val userId = jwt.subject
        val product = productRepo.findById(productId).orElseThrow { RuntimeException("Product not found") }

        val purchase = Purchase(
            userId = userId,
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
        @AuthenticationPrincipal jwt: Jwt
    ): List<PurchaseDTO> {
        val userId = jwt.subject
        val purchases = purchaseRepo.findByUserId(userId)
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

    /**
     * Admin: Add a new product.
     */
    @PostMapping("/admin/products")
    fun addProduct(@RequestBody product: Product): Product {
        return productRepo.save(product)
    }

    /**
     * Admin: Delete a product by its ID.
     */
    @DeleteMapping("/admin/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        val product = productRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        product.isRemoved = true
        productRepo.save(product)
        return ResponseEntity.noContent().build()
    }

    /**
     * Admin: Update an existing product.
     */
    @PutMapping("/admin/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody updatedProduct: Product
    ): ResponseEntity<Product> {
        val product = productRepo.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val saved = productRepo.save(
            product.copy(
                name = updatedProduct.name,
                price = updatedProduct.price
            )
        )
        return ResponseEntity.ok(saved)
    }

    /**
     * Admin: List all users and how much they have spent this month.
     */
    @GetMapping("/admin/user-spend")
    fun userMonthlySpend(): Map<String, Double> {
        val currentMonth = java.time.YearMonth.now()
        val startOfMonth = currentMonth.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant()

        val allPurchases = purchaseRepo.findAll()
            .filter {
                val ts = java.time.Instant.ofEpochMilli(it.timestamp)
                ts.isAfter(startOfMonth) && ts.isBefore(endOfMonth)
            }

        return allPurchases
            .groupBy { it.userId }
            .mapValues { (_, purchases) -> purchases.sumOf { it.price } }
    }
}
