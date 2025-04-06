package com.mvukosav.verage.data.model

import com.google.gson.annotations.SerializedName
import com.mvukosav.verage.domain.User

data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("role")
    val role: String
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        name = name,
        password = password,
        lastName = lastName,
        role = role
    )
}