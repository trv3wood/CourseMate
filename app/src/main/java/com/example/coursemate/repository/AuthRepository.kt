package com.example.coursemate.repository

import com.example.coursemate.datastore.TokenDataStore
import com.example.coursemate.local.dao.UserDao
import com.example.coursemate.model.User
import com.example.coursemate.network.api.AuthApi
import com.example.coursemate.network.api.UserApi
import com.example.coursemate.network.dto.LoginRequestDto
import com.example.coursemate.network.dto.RegisterRequestDto
import com.example.coursemate.utils.AppResult
import com.example.coursemate.utils.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val tokenDataStore: TokenDataStore
) {
    val currentUser: Flow<User?> = userDao.observeCurrentUser().map { it?.toModel() }
    val authToken: Flow<String?> = tokenDataStore.token

    suspend fun login(username: String, password: String): AppResult<User> = safeCall {
        val token = authApi.login(LoginRequestDto(username = username, password = password))
        tokenDataStore.saveToken(token.accessToken)
        val user = userApi.currentUser()
        userDao.upsert(user.toEntity())
        user.toEntity().toModel()
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        role: String = "student"
    ): AppResult<User> = safeCall {
        val user = authApi.register(
            RegisterRequestDto(
                username = username,
                email = email,
                password = password,
                role = role
            )
        )
        userDao.upsert(user.toEntity())
        user.toEntity().toModel()
    }

    suspend fun refreshCurrentUser(): AppResult<User> = safeCall {
        val user = userApi.currentUser()
        userDao.upsert(user.toEntity())
        user.toEntity().toModel()
    }

    suspend fun logout() {
        tokenDataStore.clearToken()
        userDao.clear()
    }
}
