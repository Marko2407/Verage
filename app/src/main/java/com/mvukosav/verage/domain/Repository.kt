package com.mvukosav.verage.domain

import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun login(email: String, password: String): Boolean
    suspend fun userLoggedIn(): Flow<Boolean>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun logout()
}