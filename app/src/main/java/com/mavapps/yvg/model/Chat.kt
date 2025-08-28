package com.mavapps.yvg.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class Chat(
    @PrimaryKey(autoGenerate = true )
    val chatId : Int = 0,
    val userId : Int,
    val aiId : Int,
    val dateTime : Timestamp,
    val chatType : Int,
    val message : String
)


