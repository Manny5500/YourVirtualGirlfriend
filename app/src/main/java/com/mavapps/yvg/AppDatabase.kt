package com.mavapps.yvg

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mavapps.yvg.dao.ChatDao
import com.mavapps.yvg.model.Chat

@Database(entities = [Chat::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.sql.Timestamp? {
        return value?.let { java.sql.Timestamp(it) }
    }

    @TypeConverter
    fun dateToTimestamp(timestamp: java.sql.Timestamp?): Long? {
        return timestamp?.time
    }
}
