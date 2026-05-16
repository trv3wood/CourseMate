package com.example.coursemate.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.coursemate.local.dao.CourseDao
import com.example.coursemate.local.dao.HomeworkDao
import com.example.coursemate.local.dao.PostDao
import com.example.coursemate.local.dao.ReplyDao
import com.example.coursemate.local.dao.UserDao
import com.example.coursemate.local.entity.CourseEntity
import com.example.coursemate.local.entity.HomeworkEntity
import com.example.coursemate.local.entity.PostEntity
import com.example.coursemate.local.entity.ReplyEntity
import com.example.coursemate.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        HomeworkEntity::class,
        PostEntity::class,
        ReplyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CourseMateDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun postDao(): PostDao
    abstract fun replyDao(): ReplyDao
}
