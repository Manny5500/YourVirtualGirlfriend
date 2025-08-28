package com.mavapps.yvg.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mavapps.yvg.model.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat)

    @Query("SELECT * FROM chat WHERE aiId = :aiId AND userId = :userId ORDER BY dateTime ASC")
    fun getByAIandUser(aiId: Int, userId: Int): Flow<List<Chat>>


}