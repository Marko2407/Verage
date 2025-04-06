package com.mvukosav.verage.data

import com.mvukosav.verage.data.model.UserDto
import retrofit2.http.GET

interface RepositoriesApi {
    @GET("/api/v1/login")
    suspend fun getAllUsers(): List<UserDto>
}
