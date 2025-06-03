package com.example.noxshop

import com.example.noxshop.model.Product
import com.example.noxshop.repositories.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {

    @Bean
    fun initDatabase(productRepo: ProductRepository) = CommandLineRunner {
        if (productRepo.count() == 0L) {
            productRepo.saveAll(
                listOf(
                    Product(name = "Coffee", price = 150.0),
                    Product(name = "Energy Drink", price = 200.0),
                    Product(name = "Protein Bar", price = 200.0),
                    Product(name = "Soda", price = 100.0),
                    Product(name = "Sandwich", price = 30.0)
                )
            )
            println("✅ Sample products inserted into database.")
        }
    }
}
