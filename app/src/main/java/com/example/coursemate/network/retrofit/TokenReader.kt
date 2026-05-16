package com.example.coursemate.network.retrofit

interface TokenReader {
    suspend fun currentToken(): String?
}
