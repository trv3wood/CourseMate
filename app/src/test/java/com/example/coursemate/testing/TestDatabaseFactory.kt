package com.example.coursemate.testing

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.coursemate.local.database.CourseMateDatabase

object TestDatabaseFactory {
    fun create(): CourseMateDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, CourseMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}
