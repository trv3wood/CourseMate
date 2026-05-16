package com.example.coursemate.network.retrofit

import com.example.coursemate.BuildConfig
import com.example.coursemate.network.api.AuthApi
import com.example.coursemate.network.api.CourseApi
import com.example.coursemate.network.api.HomeworkApi
import com.example.coursemate.network.api.NotificationApi
import com.example.coursemate.network.api.PostApi
import com.example.coursemate.network.api.UserApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitModule {
    fun create(tokenReader: TokenReader): ApiServices {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenReader))
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.COURSE_MATE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return ApiServices(
            authApi = retrofit.create(AuthApi::class.java),
            userApi = retrofit.create(UserApi::class.java),
            courseApi = retrofit.create(CourseApi::class.java),
            homeworkApi = retrofit.create(HomeworkApi::class.java),
            postApi = retrofit.create(PostApi::class.java),
            notificationApi = retrofit.create(NotificationApi::class.java)
        )
    }
}

data class ApiServices(
    val authApi: AuthApi,
    val userApi: UserApi,
    val courseApi: CourseApi,
    val homeworkApi: HomeworkApi,
    val postApi: PostApi,
    val notificationApi: NotificationApi
)
