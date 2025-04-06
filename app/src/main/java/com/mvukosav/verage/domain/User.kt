package com.mvukosav.verage.domain

data class User(
    val id: String,
    val email: String,
    val name: String,
    val password: String,
    val lastName: String,
    val role: String
)