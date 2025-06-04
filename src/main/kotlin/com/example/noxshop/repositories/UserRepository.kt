package com.example.noxshop.repositories

import com.example.noxshop.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, String>