package com.mvukosav.verage.data.repository

import com.mvukosav.verage.data.RepositoriesApi
import com.mvukosav.verage.data.UserPreferences
import com.mvukosav.verage.domain.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val api: RepositoriesApi,
    private val userPreferences: UserPreferences
) : Repository {

    override suspend fun login(email: String, password: String): Boolean {
        val users = api.getAllUsers()
        val user = users.find { it.email == email && it.password == password }
        val isValid = user != null
        userPreferences.setLoggedIn(isValid)
        return isValid
    }

    override suspend fun userLoggedIn(): Flow<Boolean> {
        return userPreferences.isLoggedIn()
    }

    override suspend fun isUserLoggedIn(): Boolean {
       return userPreferences.isLoggedIn().first()
    }

    override suspend fun logout() {
        userPreferences.setLoggedIn(false)
    }
}