package com.example.noxshop.repositories

import com.example.noxshop.model.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository: JpaRepository<Product, Long>