package com.example.coursemate.di

import android.content.Context
import androidx.room.Room
import com.example.coursemate.datastore.TokenDataStore
import com.example.coursemate.local.database.CourseMateDatabase
import com.example.coursemate.network.retrofit.RetrofitModule
import com.example.coursemate.repository.AuthRepository
import com.example.coursemate.repository.CourseRepository
import com.example.coursemate.repository.HomeworkRepository
import com.example.coursemate.repository.NotificationRepository
import com.example.coursemate.repository.PostRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val tokenDataStore = TokenDataStore(appContext)

    val database: CourseMateDatabase = Room.databaseBuilder(
        appContext,
        CourseMateDatabase::class.java,
        "coursemate.db"
    ).build()

    private val retrofit = RetrofitModule.create(tokenDataStore)

    val authRepository = AuthRepository(
        authApi = retrofit.authApi,
        userApi = retrofit.userApi,
        userDao = database.userDao(),
        tokenDataStore = tokenDataStore
    )

    val courseRepository = CourseRepository(
        courseApi = retrofit.courseApi,
        courseDao = database.courseDao()
    )

    val homeworkRepository = HomeworkRepository(
        homeworkApi = retrofit.homeworkApi,
        homeworkDao = database.homeworkDao()
    )

    val postRepository = PostRepository(
        postApi = retrofit.postApi,
        postDao = database.postDao(),
        replyDao = database.replyDao(),
        database = database
    )

    val notificationRepository = NotificationRepository(
        notificationApi = retrofit.notificationApi
    )
}
