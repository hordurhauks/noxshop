package com.example.noxshop.repositories

import com.example.noxshop.model.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, String>