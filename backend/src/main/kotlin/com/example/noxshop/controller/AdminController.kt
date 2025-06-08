package com.example.noxshop.controller

import com.example.noxshop.config.AppProperties
import com.example.noxshop.model.Product
import com.example.noxshop.repositories.ProductRepository
import com.example.noxshop.repositories.PurchaseRepository
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val productRepo: ProductRepository,
    private val purchaseRepo: PurchaseRepository,
    val appProperties: AppProperties) {

    /**
     * Admin: Add a new product.
     */
    @PostMapping("/products")
    fun addProduct(@RequestBody product: Product): Product {
        return productRepo.save(product)
    }

    /**
     * Admin: Delete a product by its ID.
     */
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        val product = productRepo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        product.isRemoved = true
        productRepo.save(product)
        return ResponseEntity.noContent().build()
    }

    /**
     * Admin: Update an existing product.
     */
    @PutMapping("/products/{id}")
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
    @GetMapping("/user-spend")
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

    @PostMapping("/upload-image")
    fun uploadImage(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body("File is empty")
        }

        val filename = StringUtils.cleanPath(file.originalFilename ?: "unknown")

        try {
            val uploadPath = Paths.get(appProperties.uploadDir)
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath)
            }

            val filePath = uploadPath.resolve(filename)
            file.inputStream.use { input ->
                Files.copy(input, filePath)
            }

            // Return the relative path or URL of the uploaded image
            val imageUrl = "/uploads/products/$filename"
            return ResponseEntity.ok(imageUrl)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ResponseEntity.internalServerError().body("Failed to upload")
        }
    }
}