package ru.netology.network.ui.activity.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.network.ui.activity.dao.Converters
import ru.netology.network.ui.activity.dao.EventDao
import ru.netology.network.ui.activity.dao.JobDao
import ru.netology.network.ui.activity.dao.PostDao
import ru.netology.network.ui.activity.dao.UserDao
import ru.netology.network.ui.activity.entity.EventEntity
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserEntity

@Database(
    entities = [PostEntity::class, UserEntity::class, EventEntity::class, JobEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun jobDao(): JobDao
}