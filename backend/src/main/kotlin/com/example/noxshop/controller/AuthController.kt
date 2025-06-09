package com.example.noxshop.controller

import com.example.noxshop.model.Account
import com.example.noxshop.repositories.AccountRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val accountRepo: AccountRepository) {

    @PostMapping("/login")
    fun login(
        @RequestAttribute("uid") uid: String,
        @RequestAttribute("email") email: String
    ): Account {
        val existing = accountRepo.findById(uid).orElse(null)
        return if (existing != null) {
            if (existing.email != email) {
                val updated = existing.copy(email = email)
                accountRepo.save(updated)
            } else {
                existing
            }
        } else {
            val newAccount = Account(uid = uid, email = email, roles = setOf("USER"))
            accountRepo.save(newAccount)
        }
    }

}
